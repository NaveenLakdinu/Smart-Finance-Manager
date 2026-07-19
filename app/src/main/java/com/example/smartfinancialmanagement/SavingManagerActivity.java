package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SavingManagerActivity — Dashboard for the Saving Manager feature.
 * Mirrors the Compose SavingManagerScreen + SavingManagerActivity.kt
 */
public class SavingManagerActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId = "test_user";

    private final List<SavingGoalAdapter.GoalItem> goalsList = new ArrayList<>();
    private double totalSavingsGoal = 0.0;

    private TextView tvTotalGoal;
    private View progressTotal;
    private LinearLayout containerActiveGoals;
    private TextView tvEmptyGoals;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_manager);

        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "test_user";

        tvTotalGoal = findViewById(R.id.tvTotalGoal);
        progressTotal = findViewById(R.id.progressTotal);
        containerActiveGoals = findViewById(R.id.containerActiveGoals);
        tvEmptyGoals = findViewById(R.id.tvEmptyGoals);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Wallet button
        View btnWallet = findViewById(R.id.btnWallet);
        if (btnWallet != null) {
            btnWallet.setOnClickListener(v -> startActivity(new Intent(this, AddIncomeActivity.class)));
        }

        // Action card: Add Goal
        findViewById(R.id.cardAddGoal).setOnClickListener(v ->
                startActivity(new Intent(this, SavingAddGoalActivity.class)));

        // Action card: Report
        findViewById(R.id.cardReport).setOnClickListener(v ->
                startActivity(new Intent(this, SavingGenerateReportActivity.class)));

        // See all → active goals
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, SavingListActivity.class)));

        fetchSavingsData();
    }

    private void fetchSavingsData() {
        db.collection("users").document(userId).collection("savings")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    goalsList.clear();
                    double totalTarget = 0.0;
                    double totalCurrent = 0.0;

                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            SavingModel saving = doc.toObject(SavingModel.class);
                            if (saving == null) continue;

                            totalTarget += saving.getTargetAmount();
                            totalCurrent += saving.getCurrentAmount();

                            boolean isCompleted = saving.getCurrentAmount() >= saving.getTargetAmount();
                            boolean isPassedDate = false;
                            try {
                                Date tDate = dateFormat.parse(saving.getTargetDate());
                                if (tDate != null && tDate.before(new Date())) isPassedDate = true;
                            } catch (Exception ignored) {}

                            SavingGoalAdapter.GoalStatus status;
                            if (isCompleted) status = SavingGoalAdapter.GoalStatus.COMPLETED;
                            else if (isPassedDate) status = SavingGoalAdapter.GoalStatus.INCOMPLETE;
                            else status = SavingGoalAdapter.GoalStatus.ONGOING;

                            goalsList.add(new SavingGoalAdapter.GoalItem(
                                    saving.getSavingId(),
                                    saving.getSavingTitle(),
                                    saving.getTargetAmount(),
                                    saving.getCurrentAmount(),
                                    status));
                        }
                    }
                    totalSavingsGoal = totalTarget;
                    updateUI(totalTarget, totalCurrent);
                });
    }

    private void updateUI(double totalTarget, double totalCurrent) {
        tvTotalGoal.setText(money(totalTarget));

        // Update progress bar width
        progressTotal.post(() -> {
            View parent = (View) progressTotal.getParent();
            int parentW = parent.getWidth();
            float pct = totalTarget > 0 ? (float) Math.min(1.0, totalCurrent / totalTarget) : 0f;
            android.view.ViewGroup.LayoutParams lp = progressTotal.getLayoutParams();
            lp.width = Math.max(8, (int) (parentW * pct));
            progressTotal.setLayoutParams(lp);
        });

        // Rebuild active goals container
        containerActiveGoals.removeAllViews();
        List<SavingGoalAdapter.GoalItem> active = new ArrayList<>();
        for (SavingGoalAdapter.GoalItem g : goalsList) {
            if (g.status == SavingGoalAdapter.GoalStatus.ONGOING) active.add(g);
        }

        if (active.isEmpty()) {
            tvEmptyGoals.setVisibility(View.VISIBLE);
        } else {
            tvEmptyGoals.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (SavingGoalAdapter.GoalItem goal : active) {
                View card = inflater.inflate(R.layout.item_saving_goal_compact, containerActiveGoals, false);
                bindActiveGoalCard(card, goal);
                containerActiveGoals.addView(card);
            }
        }
    }

    private void bindActiveGoalCard(View card, SavingGoalAdapter.GoalItem goal) {
        ((TextView) card.findViewById(R.id.tvGoalName)).setText(goal.name);
        ((TextView) card.findViewById(R.id.tvTarget)).setText("Target: " + money(goal.target));
        ((TextView) card.findViewById(R.id.tvPercent)).setText(goal.percent() + "%");

        // Progress
        View pb = card.findViewById(R.id.progressBar);
        pb.post(() -> {
            View parentV = (View) pb.getParent();
            android.view.ViewGroup.LayoutParams lp = pb.getLayoutParams();
            lp.width = Math.max(8, (int) (parentV.getWidth() * (goal.percent() / 100f)));
            pb.setLayoutParams(lp);
        });

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingDetailsActivity.class);
            intent.putExtra("SAVING_ID", goal.id);
            startActivity(intent);
        });
    }

    private String money(double n) {
        return CurrencyHelper.formatMoney(this, n);
    }
}
