package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
 * SavingListActivity — Searchable + filterable goal list.
 * Mirrors Compose SavingGoalsListScreen + SavingListActivity.kt
 */
public class SavingListActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId = "test_user";

    private final List<SavingGoalAdapter.GoalItem> allGoals = new ArrayList<>();
    private SavingGoalAdapter adapter;
    private String currentFilter = "All"; // All / Ongoing / Completed / Incomplete
    private String currentQuery = "";

    private RecyclerView rvGoals;
    private TextView tvEmpty;
    private LinearLayout filterChipsContainer;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final String[] FILTERS = {"All", "Ongoing", "Completed", "Incomplete"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_list);

        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "test_user";

        rvGoals = findViewById(R.id.rvGoals);
        tvEmpty = findViewById(R.id.tvEmpty);
        filterChipsContainer = findViewById(R.id.filterChipsContainer);

        adapter = new SavingGoalAdapter(this, new ArrayList<>(), new SavingGoalAdapter.Listener() {
            @Override public void onGoalClick(SavingGoalAdapter.GoalItem goal) {
                Intent i = new Intent(SavingListActivity.this, SavingDetailsActivity.class);
                i.putExtra("SAVING_ID", goal.id);
                startActivity(i);
            }
            @Override public void onEdit(SavingGoalAdapter.GoalItem goal) {
                Intent i = new Intent(SavingListActivity.this, SavingUpdateGoalActivity.class);
                i.putExtra("SAVING_ID", goal.id);
                startActivity(i);
            }
            @Override public void onDelete(SavingGoalAdapter.GoalItem goal) {
                showDeleteDialog(goal.id);
            }
        });

        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setAdapter(adapter);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // FAB
        findViewById(R.id.fabAddGoal).setOnClickListener(v ->
                startActivity(new Intent(this, SavingAddGoalActivity.class)));

        // Search
        ((EditText) findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        buildFilterChips();
        fetchSavingsData();
    }

    private void buildFilterChips() {
        filterChipsContainer.removeAllViews();
        for (String filter : FILTERS) {
            TextView chip = new TextView(this);
            chip.setText(filter);
            chip.setTextSize(12.5f);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setPadding(dp(16), dp(9), dp(16), dp(9));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            chip.setLayoutParams(lp);

            updateChipStyle(chip, filter.equals(currentFilter));

            chip.setOnClickListener(v -> {
                currentFilter = filter;
                buildFilterChips();
                applyFilter();
            });
            filterChipsContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundColor(Color.parseColor("#2FE0AC"));
            chip.setTextColor(Color.parseColor("#04241B"));
        } else {
            chip.setBackgroundColor(Color.parseColor("#131A2E"));
            chip.setTextColor(Color.parseColor("#8A92B2"));
        }
        chip.setBackground(chip.getBackground()); // trigger redraw
        // Use a rounded background via GradientDrawable
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(dp(50));
        gd.setColor(selected ? Color.parseColor("#2FE0AC") : Color.parseColor("#131A2E"));
        gd.setStroke(dp(1), selected ? Color.parseColor("#2FE0AC") : Color.parseColor("#232D4D"));
        chip.setBackground(gd);
        chip.setTextColor(selected ? Color.parseColor("#04241B") : Color.parseColor("#8A92B2"));
    }

    private void applyFilter() {
        List<SavingGoalAdapter.GoalItem> filtered = new ArrayList<>();
        for (SavingGoalAdapter.GoalItem goal : allGoals) {
            boolean matchesFilter = currentFilter.equals("All") ||
                    (currentFilter.equals("Ongoing") && goal.status == SavingGoalAdapter.GoalStatus.ONGOING) ||
                    (currentFilter.equals("Completed") && goal.status == SavingGoalAdapter.GoalStatus.COMPLETED) ||
                    (currentFilter.equals("Incomplete") && goal.status == SavingGoalAdapter.GoalStatus.INCOMPLETE);
            boolean matchesQuery = currentQuery.isEmpty() ||
                    goal.name.toLowerCase(Locale.ROOT).contains(currentQuery.toLowerCase(Locale.ROOT));
            if (matchesFilter && matchesQuery) filtered.add(goal);
        }
        adapter.setGoals(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvGoals.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void fetchSavingsData() {
        db.collection("users").document(userId).collection("savings")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    allGoals.clear();
                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            SavingModel saving = doc.toObject(SavingModel.class);
                            if (saving == null) continue;

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

                            allGoals.add(new SavingGoalAdapter.GoalItem(
                                    saving.getSavingId(),
                                    saving.getSavingTitle(),
                                    saving.getTargetAmount(),
                                    saving.getCurrentAmount(),
                                    status));
                        }
                    }
                    applyFilter();
                });
    }

    private void showDeleteDialog(String savingId) {
        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this saving goal?")
                .setPositiveButton("Delete", (d, w) -> deleteGoal(savingId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoal(String savingId) {
        db.collection("users").document(userId).collection("savings")
                .document(savingId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
