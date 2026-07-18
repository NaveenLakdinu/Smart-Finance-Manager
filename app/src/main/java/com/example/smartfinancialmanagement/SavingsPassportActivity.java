package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SavingsPassportActivity — Achievements + Goals Passport screen.
 * Mirrors Compose SavingsPassportScreen + SavingsPassportActivity.kt
 * All achievement logic from AchievementManager.kt is inlined here in Java.
 */
public class SavingsPassportActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private ListenerRegistration listener;

    // UI refs
    private TextView tvLevel, tvEarnedCount, tvEarnedLabel, tvLockedLabel;
    private LinearLayout containerEarned, containerLocked, containerGoals, streakSegments;

    // Achievement data model
    private static class Achievement {
        String id, title, subtitle, emoji;
        int medalColor;
        boolean earned;
        int progressPercent;

        Achievement(String id, String title, String subtitle, String emoji,
                    int medalColor, boolean earned, int progressPercent) {
            this.id = id; this.title = title; this.subtitle = subtitle;
            this.emoji = emoji; this.medalColor = medalColor;
            this.earned = earned; this.progressPercent = progressPercent;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_passport);

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "test_user";

        tvLevel          = findViewById(R.id.tvLevel);
        tvEarnedCount    = findViewById(R.id.tvEarnedCount);
        tvEarnedLabel    = findViewById(R.id.tvEarnedLabel);
        tvLockedLabel    = findViewById(R.id.tvLockedLabel);
        containerEarned  = findViewById(R.id.containerEarned);
        containerLocked  = findViewById(R.id.containerLocked);
        containerGoals   = findViewById(R.id.containerGoals);
        streakSegments   = findViewById(R.id.streakSegments);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Add Goal button
        findViewById(R.id.btnAddGoal).setOnClickListener(v ->
                startActivity(new Intent(this, SavingAddGoalActivity.class)));

        buildStreakSegments(1, 6); // 1 out of 6 months (hardcoded like Compose)

        listener = db.collection("users").document(userId).collection("savings")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load goals", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<SavingModel> savings = new ArrayList<>();
                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            SavingModel model = doc.toObject(SavingModel.class);
                            if (model != null) savings.add(model);
                        }
                    }
                    updateUI(savings);
                });
    }

    private void updateUI(List<SavingModel> savings) {
        double totalSavings = 0;
        int completedCount = 0;
        for (SavingModel m : savings) {
            totalSavings += m.getCurrentAmount();
            if (m.getCurrentAmount() >= m.getTargetAmount() && m.getTargetAmount() > 0) completedCount++;
        }

        // Level
        String level = getSavingsLevel(totalSavings);
        tvLevel.setText(level);

        // Achievements
        List<Achievement> all = evaluateAchievements(totalSavings, completedCount);
        List<Achievement> earned = new ArrayList<>();
        List<Achievement> locked = new ArrayList<>();
        for (Achievement a : all) { if (a.earned) earned.add(a); else locked.add(a); }

        tvEarnedCount.setText(earned.size() + " earned");

        // Build earned grid
        if (!earned.isEmpty()) {
            tvEarnedLabel.setVisibility(View.VISIBLE);
            containerEarned.setVisibility(View.VISIBLE);
            buildBadgeGrid(containerEarned, earned, true);
        } else {
            tvEarnedLabel.setVisibility(View.GONE);
            containerEarned.setVisibility(View.GONE);
        }

        // Build locked grid
        if (!locked.isEmpty()) {
            tvLockedLabel.setVisibility(View.VISIBLE);
            containerLocked.setVisibility(View.VISIBLE);
            buildBadgeGrid(containerLocked, locked, false);
        } else {
            tvLockedLabel.setVisibility(View.GONE);
            containerLocked.setVisibility(View.GONE);
        }

        // Goals list
        buildGoalsList(savings);
    }

    private void buildBadgeGrid(LinearLayout container, List<Achievement> badges, boolean isEarned) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        // Render 2 per row
        for (int i = 0; i < badges.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 0, 0, dp(10));
            row.setLayoutParams(rowLp);

            bindBadge(row, inflater, badges.get(i), isEarned);
            if (i + 1 < badges.size()) {
                View spacer = new View(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(10), 1));
                row.addView(spacer);
                bindBadge(row, inflater, badges.get(i + 1), isEarned);
            } else {
                // Spacer to balance grid
                View empty = new View(this);
                LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, 1, 1f);
                empty.setLayoutParams(ep);
                row.addView(empty);
            }
            container.addView(row);
        }
    }

    private void bindBadge(LinearLayout row, LayoutInflater inflater, Achievement badge, boolean isEarned) {
        View card = inflater.inflate(R.layout.item_achievement, row, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        card.setLayoutParams(lp);

        ((TextView) card.findViewById(R.id.tvBadgeEmoji)).setText(badge.emoji);
        ((TextView) card.findViewById(R.id.tvBadgeTitle)).setText(badge.title);
        ((TextView) card.findViewById(R.id.tvBadgeSubtitle)).setText(badge.subtitle);

        // Set emoji circle background color
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        gd.setColor(badge.medalColor);
        card.findViewById(R.id.tvBadgeEmoji).setBackground(gd);

        if (isEarned) {
            card.setBackgroundColor(Color.parseColor("#FFFCF3"));
            card.findViewById(R.id.layoutStamped).setVisibility(View.VISIBLE);
            card.findViewById(R.id.layoutLocked).setVisibility(View.GONE);
            TextView corner = card.findViewById(R.id.tvCornerBadge);
            corner.setText("✓");
            android.graphics.drawable.GradientDrawable cornerBg = new android.graphics.drawable.GradientDrawable();
            cornerBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            cornerBg.setColor(Color.parseColor("#1F8A5F"));
            corner.setBackground(cornerBg);
        } else {
            card.setBackgroundColor(Color.parseColor("#F4F2EC"));
            card.findViewById(R.id.layoutStamped).setVisibility(View.GONE);
            card.findViewById(R.id.layoutLocked).setVisibility(View.VISIBLE);
            ((TextView) card.findViewById(R.id.tvBadgeProgressPct)).setText(badge.progressPercent + "%");
            // Progress bar width
            View pb = card.findViewById(R.id.vBadgeProgress);
            pb.post(() -> {
                View parent = (View) pb.getParent();
                ViewGroup.LayoutParams pbLp = pb.getLayoutParams();
                pbLp.width = Math.max(0, (int) (parent.getWidth() * (badge.progressPercent / 100f)));
                pb.setLayoutParams(pbLp);
            });
            // Lock icon in corner
            TextView corner = card.findViewById(R.id.tvCornerBadge);
            corner.setText("🔒");
            corner.setTextSize(8);
            android.graphics.drawable.GradientDrawable cornerBg = new android.graphics.drawable.GradientDrawable();
            cornerBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            cornerBg.setColor(Color.parseColor("#E3DFD3"));
            corner.setBackground(cornerBg);
        }

        row.addView(card);
    }

    private void buildStreakSegments(int lit, int total) {
        streakSegments.removeAllViews();
        for (int i = 0; i < total; i++) {
            View seg = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(5), 1f);
            lp.setMarginEnd(i < total - 1 ? dp(5) : 0);
            seg.setLayoutParams(lp);
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setCornerRadius(dp(4));
            gd.setColor(i < lit ? Color.parseColor("#E8C766") : Color.parseColor("#2EFFFFFF"));
            seg.setBackground(gd);
            streakSegments.addView(seg);
        }
    }

    private void buildGoalsList(List<SavingModel> savings) {
        containerGoals.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (SavingModel model : savings) {
            View card = inflater.inflate(R.layout.item_saving_goal_compact, containerGoals, false);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
            lp.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(lp);

            double pct = model.getTargetAmount() > 0
                    ? Math.min(100.0, (model.getCurrentAmount() / model.getTargetAmount()) * 100)
                    : 0;

            ((TextView) card.findViewById(R.id.tvGoalName)).setText(model.getSavingTitle());
            ((TextView) card.findViewById(R.id.tvTarget)).setText("Target: " + money(model.getTargetAmount()));
            ((TextView) card.findViewById(R.id.tvPercent)).setText((int) pct + "%");

            // progress bar
            View pb = card.findViewById(R.id.progressBar);
            double finalPct = pct;
            pb.post(() -> {
                View parent = (View) pb.getParent();
                ViewGroup.LayoutParams pbLp = pb.getLayoutParams();
                pbLp.width = Math.max(8, (int) (parent.getWidth() * (finalPct / 100.0)));
                pb.setLayoutParams(pbLp);
            });

            containerGoals.addView(card);
        }
    }

    // ── Achievement logic (ported from AchievementManager.kt) ──────────────

    private List<Achievement> evaluateAchievements(double totalSavings, int completedCount) {
        List<Achievement> list = new ArrayList<>();

        int bronzePct = (int) Math.min(100, totalSavings / 5000.0 * 100);
        list.add(new Achievement("bronze_saver", "Bronze Saver", "Save Rs. 5,000", "🥉",
                Color.parseColor("#C97B45"), totalSavings >= 5000, bronzePct));

        int silverPct = (int) Math.min(100, totalSavings / 25000.0 * 100);
        list.add(new Achievement("silver_saver", "Silver Saver", "Save Rs. 25,000", "🥈",
                Color.parseColor("#B9BDC4"), totalSavings >= 25000, silverPct));

        int goldPct = (int) Math.min(100, totalSavings / 50000.0 * 100);
        list.add(new Achievement("gold_saver", "Gold Saver", "Save Rs. 50,000", "🥇",
                Color.parseColor("#C9A227"), totalSavings >= 50000, goldPct));

        int masterPct = (int) Math.min(100, completedCount / 3.0 * 100);
        list.add(new Achievement("goal_master", "Goal Master", "Complete 3 saving goals", "🏆",
                Color.parseColor("#9C7A16"), completedCount >= 3, masterPct));

        list.add(new Achievement("consistent_saver", "Consistent Saver", "Save every month for 6 months", "💧",
                Color.parseColor("#1F8A5F"), false, 16));

        return list;
    }

    private String getSavingsLevel(double totalSavings) {
        if (totalSavings >= 50000) return "Gold Saver";
        if (totalSavings >= 25000) return "Silver Saver";
        if (totalSavings >= 5000)  return "Bronze Saver";
        return "Starter";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    private String money(double amount) {
        return CurrencyHelper.formatMoney(this, amount);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
