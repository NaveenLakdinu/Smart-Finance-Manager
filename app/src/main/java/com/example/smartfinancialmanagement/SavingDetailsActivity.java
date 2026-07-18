package com.example.smartfinancialmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SavingDetailsActivity — Goal Progress / Detail screen.
 * Mirrors Compose GoalProgressScreen.
 *
 * Celebration logic:
 *  - Detects completion (pct >= 100%) exactly once per "completion event".
 *  - If user later drops below 100% (e.g. editing) the flag resets so it can
 *    fire again when re-completed (matching the Compose LaunchedEffect(isCompleted) behavior).
 *  - Shows: confetti burst (ConfettiView) + centered banner card.
 *  - Auto-dismisses after CELEBRATION_DURATION_MS (~2.6s).
 *  - Log entries persisted in Firestore subcollection: savings/{id}/log
 */
public class SavingDetailsActivity extends AppCompatActivity {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final long CELEBRATION_DURATION_MS = 2600L;

    // ── Firebase ─────────────────────────────────────────────────────────────
    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private String userId   = "test_user";
    private String savingId;

    private SavingModel savingModel;
    private ListenerRegistration goalListener;
    private ListenerRegistration logListener;

    // ── Celebration state ────────────────────────────────────────────────────
    /**
     * True while the goal is at 100% and we've already triggered the celebration.
     * Resets to false whenever pct drops below 100% so re-completion re-fires.
     */
    private boolean celebrationFired = false;
    private final Handler celebrationHandler = new Handler(Looper.getMainLooper());
    private Runnable dismissRunnable;

    // ── UI refs: main detail ─────────────────────────────────────────────────
    private JarView  jarView;
    private View     progressDetail;
    private TextView tvGoalTitle, tvGoalDates, tvStatusBadge;
    private TextView tvCurrentSaved, tvTargetAmount;
    private TextView tvRemaining, tvMonthlyNeed;
    private TextView tvStartDate, tvTargetDate;
    private LinearLayout layoutAddForm, containerLog;
    private TextView tvEmptyLog, tvWarning;
    private EditText etAmount, etNote;
    private boolean  showAddForm = false;

    // ── UI refs: celebration overlay ─────────────────────────────────────────
    private FrameLayout   celebrationOverlay;
    private ConfettiView  confettiView;
    private LinearLayout  celebrationBanner;
    private TextView      tvCelebrationGoalName;

    // ── Log data (Firestore-backed) ───────────────────────────────────────────
    private final List<Map<String, Object>> logEntries = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_details);

        userId   = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "test_user";
        savingId = getIntent().getStringExtra("SAVING_ID");

        // Main detail views
        jarView               = findViewById(R.id.jarView);
        progressDetail        = findViewById(R.id.progressDetail);
        tvGoalTitle           = findViewById(R.id.tvGoalTitle);
        tvGoalDates           = findViewById(R.id.tvGoalDates);
        tvStatusBadge         = findViewById(R.id.tvStatusBadge);
        tvCurrentSaved        = findViewById(R.id.tvCurrentSaved);
        tvTargetAmount        = findViewById(R.id.tvTargetAmount);
        tvRemaining           = findViewById(R.id.tvRemaining);
        tvMonthlyNeed         = findViewById(R.id.tvMonthlyNeed);
        tvStartDate           = findViewById(R.id.tvStartDate);
        tvTargetDate          = findViewById(R.id.tvTargetDate);
        layoutAddForm         = findViewById(R.id.layoutAddForm);
        containerLog          = findViewById(R.id.containerLog);
        tvEmptyLog            = findViewById(R.id.tvEmptyLog);
        tvWarning             = findViewById(R.id.tvWarning);
        etAmount              = findViewById(R.id.etAmount);
        etNote                = findViewById(R.id.etNote);

        // Celebration overlay views
        celebrationOverlay    = findViewById(R.id.celebrationOverlay);
        confettiView          = findViewById(R.id.confettiView);
        celebrationBanner     = findViewById(R.id.celebrationBanner);
        tvCelebrationGoalName = findViewById(R.id.tvCelebrationGoalName);

        // Pre-build particles (random values, independent of view size)
        confettiView.buildParticles();

        // ── Listeners ────────────────────────────────────────────────────────
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddSavings).setOnClickListener(v -> toggleAddForm());
        findViewById(R.id.btnCancelEntry).setOnClickListener(v -> closeAddForm());
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { checkWarning(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        findViewById(R.id.btnSaveEntry).setOnClickListener(v -> saveEntry());
        findViewById(R.id.btnDeleteGoal).setOnClickListener(v -> showDeleteDialog());
        findViewById(R.id.btnEditGoal).setOnClickListener(v -> {
            Intent i = new Intent(this, SavingUpdateGoalActivity.class);
            i.putExtra("SAVING_ID", savingId);
            startActivity(i);
        });

        startListening();
        startLogListening();
    }

    // ─── Firestore listeners ──────────────────────────────────────────────────

    private void startListening() {
        goalListener = db.collection("users").document(userId)
                .collection("savings").document(savingId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap != null && snap.exists()) {
                        savingModel = snap.toObject(SavingModel.class);
                        updateUI();
                    }
                });
    }

    private void startLogListening() {
        logListener = db.collection("users").document(userId)
                .collection("savings").document(savingId)
                .collection("log")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) return;
                    logEntries.clear();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Map<String, Object> e = doc.getData();
                            if (e != null) logEntries.add(e);
                        }
                    }
                    refreshLog();
                });
    }

    // ─── UI update ────────────────────────────────────────────────────────────

    private void updateUI() {
        if (savingModel == null) return;

        double current   = savingModel.getCurrentAmount();
        double target    = savingModel.getTargetAmount();
        double monthly   = savingModel.getMonthlySavingAmount();
        double remaining = Math.max(0.0, target - current);
        double pct       = target > 0 ? Math.min(100.0, (current / target) * 100.0) : 0.0;
        boolean isCompleted = pct >= 100.0;

        tvGoalTitle.setText(savingModel.getSavingTitle() != null ? savingModel.getSavingTitle() : "Goal");

        String start = nvl(savingModel.getStartDate(), "--");
        String end   = nvl(savingModel.getTargetDate(), "--");
        tvGoalDates.setText("Started " + (start.length() >= 5 ? start.substring(0, 5) : start) + " — due " + end);

        if (isCompleted) {
            tvStatusBadge.setText("★ Completed");
            tvStatusBadge.setTextColor(color("#FFC857"));
        } else {
            tvStatusBadge.setText("● Active");
            tvStatusBadge.setTextColor(color("#2FE0AC"));
        }

        tvCurrentSaved.setText(money(current));
        tvTargetAmount.setText(money(target));
        tvRemaining.setText(money(remaining));
        tvMonthlyNeed.setText(money(monthly));
        tvStartDate.setText(start);
        tvTargetDate.setText(end);

        jarView.setPercent((float) pct);

        progressDetail.post(() -> {
            View parent = (View) progressDetail.getParent();
            ViewGroup.LayoutParams lp = progressDetail.getLayoutParams();
            lp.width = Math.max(8, (int) (parent.getWidth() * (pct / 100.0)));
            progressDetail.setLayoutParams(lp);
        });

        // ── Celebration trigger logic ──────────────────────────────────────
        // Matches: LaunchedEffect(isCompleted) — fires when completion state changes
        if (isCompleted && !celebrationFired) {
            // Reached 100% for the first time (or after a reset)
            celebrationFired = true;
            triggerCelebration(savingModel.getSavingTitle());
        } else if (!isCompleted) {
            // Dropped back below 100% — allow celebration to fire again if re-completed
            celebrationFired = false;
        }
    }

    // ─── Celebration sequence ─────────────────────────────────────────────────

    /**
     * Show the full-screen celebration: confetti burst + banner card.
     * Auto-dismisses after {@link #CELEBRATION_DURATION_MS}.
     */
    private void triggerCelebration(String goalName) {
        // Set goal name in banner
        tvCelebrationGoalName.setText(goalName != null ? goalName : "");

        // Show overlay
        celebrationOverlay.setVisibility(View.VISIBLE);
        celebrationOverlay.setAlpha(0f);

        // Fade-in the overlay (200ms)
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(celebrationOverlay, "alpha", 0f, 1f);
        fadeIn.setDuration(200);
        fadeIn.start();

        // Animate banner: scale-up from 0 with bounce
        celebrationBanner.setScaleX(0.3f);
        celebrationBanner.setScaleY(0.3f);
        celebrationBanner.setAlpha(0f);
        celebrationBanner.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(350)
                .setInterpolator(new android.view.animation.OvershootInterpolator(2.5f))
                .start();

        // Burst the confetti
        confettiView.burst(() -> {
            // When confetti finishes, start fade-out
            // (already scheduled via Handler below, so nothing extra needed here)
        });

        // Schedule auto-dismiss
        if (dismissRunnable != null) celebrationHandler.removeCallbacks(dismissRunnable);
        dismissRunnable = this::hideCelebration;
        celebrationHandler.postDelayed(dismissRunnable, CELEBRATION_DURATION_MS);
    }

    /** Fade out and hide the celebration overlay. */
    private void hideCelebration() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(celebrationOverlay, "alpha", 1f, 0f);
        fadeOut.setDuration(350);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                celebrationOverlay.setVisibility(View.GONE);
                confettiView.stop();
            }
        });
        fadeOut.start();

        // Also fade out banner
        celebrationBanner.animate()
                .scaleX(0.85f).scaleY(0.85f).alpha(0f)
                .setDuration(300)
                .start();
    }

    // ─── Savings Log ──────────────────────────────────────────────────────────

    private void refreshLog() {
        containerLog.removeAllViews();
        if (logEntries.isEmpty()) {
            tvEmptyLog.setVisibility(View.VISIBLE);
        } else {
            tvEmptyLog.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Map<String, Object> entry : logEntries) {
                View row = inflater.inflate(R.layout.item_saving_log, containerLog, false);

                Object amtObj  = entry.get("amount");
                double amtVal  = amtObj instanceof Number ? ((Number) amtObj).doubleValue() : 0;
                ((TextView) row.findViewById(R.id.tvLogAmount)).setText(money(amtVal));

                Object noteObj = entry.get("note");
                ((TextView) row.findViewById(R.id.tvLogNote)).setText(
                        noteObj != null ? noteObj.toString() : "Savings entry");

                Object dateObj = entry.get("dateLabel");
                ((TextView) row.findViewById(R.id.tvLogDate)).setText(
                        dateObj != null ? dateObj.toString() : "");

                row.findViewById(R.id.btnLogEdit).setOnClickListener(v ->
                        Toast.makeText(this, "Edit not supported in this version", Toast.LENGTH_SHORT).show());

                containerLog.addView(row);
            }
        }
    }

    // ─── Add savings form ─────────────────────────────────────────────────────

    private void toggleAddForm() {
        showAddForm = !showAddForm;
        layoutAddForm.setVisibility(showAddForm ? View.VISIBLE : View.GONE);
        if (!showAddForm) clearForm();
    }

    private void closeAddForm() {
        showAddForm = false;
        layoutAddForm.setVisibility(View.GONE);
        clearForm();
    }

    private void clearForm() {
        etAmount.setText("");
        etNote.setText("");
        tvWarning.setVisibility(View.GONE);
    }

    private void checkWarning() {
        if (savingModel == null) return;
        try {
            double amount  = Double.parseDouble(etAmount.getText().toString());
            double current = savingModel.getCurrentAmount();
            double target  = savingModel.getTargetAmount();
            tvWarning.setVisibility(
                    (target > 0 && current + amount > target) ? View.VISIBLE : View.GONE);
        } catch (NumberFormatException e) {
            tvWarning.setVisibility(View.GONE);
        }
    }

    private void saveEntry() {
        String amtStr = etAmount.getText().toString().trim();
        double value;
        try {
            value = Double.parseDouble(amtStr);
            if (value <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String note      = etNote.getText().toString().trim();
        String dateLabel = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date());

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("amount",    value);
        logEntry.put("note",      note.isEmpty() ? "Savings entry" : note);
        logEntry.put("dateLabel", dateLabel);
        logEntry.put("timestamp", System.currentTimeMillis());

        double newTotal = (savingModel != null ? savingModel.getCurrentAmount() : 0.0) + value;

        // Write log entry to Firestore subcollection
        db.collection("users").document(userId)
                .collection("savings").document(savingId)
                .collection("log")
                .add(logEntry)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save log entry", Toast.LENGTH_SHORT).show());

        // Update currentAmount on saving document
        db.collection("users").document(userId)
                .collection("savings").document(savingId)
                .update("currentAmount", newTotal)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update amount", Toast.LENGTH_SHORT).show());

        closeAddForm();
    }

    // ─── Delete goal ──────────────────────────────────────────────────────────

    private void showDeleteDialog() {
        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this saving goal?")
                .setPositiveButton("Delete", (d, w) -> deleteGoal())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoal() {
        db.collection("users").document(userId)
                .collection("savings").document(savingId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Saving goal deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (goalListener   != null) goalListener.remove();
        if (logListener    != null) logListener.remove();
        if (dismissRunnable != null) celebrationHandler.removeCallbacks(dismissRunnable);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String money(double n) {
        return CurrencyHelper.formatMoney(this, n);
    }

    private String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    private int color(String hex) {
        return android.graphics.Color.parseColor(hex);
    }
}
