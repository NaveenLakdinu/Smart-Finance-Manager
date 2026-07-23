package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.example.smartfinancialmanagement.CurrencyHelper;

public class SavingReportResultActivity extends AppCompatActivity {

    private ImageView btnBack, btnExport;
    private TextView tvReportPeriod, tvHealthScore, tvHealthScoreValue;
    private TextView tvTotalGoals, tvActiveGoals, tvCompletedGoals;
    private TextView tvTotalTarget, tvTotalSaved, tvTotalRemaining, tvAvgProgress;
    private ProgressBar pbAvgProgress;

    private CollectionReference databaseReference;
    private String userId;
    private String reportType, monthYear, startDateStr, endDateStr;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_report_result);

        reportType = getIntent().getStringExtra("REPORT_TYPE");
        monthYear = getIntent().getStringExtra("MONTH_YEAR");
        startDateStr = getIntent().getStringExtra("START_DATE");
        endDateStr = getIntent().getStringExtra("END_DATE");

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupFirebase();
        setupListeners();
        setReportPeriod();
        fetchDataAndCalculate();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnExport = findViewById(R.id.btnExport);
        tvReportPeriod = findViewById(R.id.tvReportPeriod);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvHealthScoreValue = findViewById(R.id.tvHealthScoreValue);
        tvTotalGoals = findViewById(R.id.tvTotalGoals);
        tvActiveGoals = findViewById(R.id.tvActiveGoals);
        tvCompletedGoals = findViewById(R.id.tvCompletedGoals);
        tvTotalTarget = findViewById(R.id.tvTotalTarget);
        tvTotalSaved = findViewById(R.id.tvTotalSaved);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvAvgProgress = findViewById(R.id.tvAvgProgress);
        pbAvgProgress = findViewById(R.id.pbAvgProgress);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "test_user";
        }
        databaseReference = FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setReportPeriod() {
        if ("MONTH".equals(reportType)) {
            if (monthYear == null || monthYear.isEmpty() || monthYear.equals("MM/YYYY")) {
                tvReportPeriod.setText("Period: All Time");
            } else {
                tvReportPeriod.setText("Period: " + monthYear);
            }
        } else if ("CUSTOM".equals(reportType)) {
            if (startDateStr == null || startDateStr.isEmpty() || endDateStr == null || endDateStr.isEmpty()) {
                tvReportPeriod.setText("Period: All Time");
            } else {
                tvReportPeriod.setText("Period: " + startDateStr + " to " + endDateStr);
            }
        } else {
            tvReportPeriod.setText("Period: All Time");
        }
    }

    private void fetchDataAndCalculate() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int totalGoals = 0;
                int activeGoals = 0;
                int completedGoals = 0;
                double totalTarget = 0;
                double totalSaved = 0;
                double totalProgressSum = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    SavingModel saving = document.toObject(SavingModel.class);
                    if (saving != null && isWithinDateRange(saving.getStartDate(), saving.getTargetDate())) {
                        totalGoals++;
                        
                        String status = saving.getStatus() != null ? saving.getStatus() : "Active";
                        if ("Completed".equalsIgnoreCase(status) || saving.getCurrentAmount() >= saving.getTargetAmount()) {
                            completedGoals++;
                        } else {
                            activeGoals++;
                        }

                        totalTarget += saving.getTargetAmount();
                        totalSaved += saving.getCurrentAmount();

                        double progress = 0;
                        if (saving.getTargetAmount() > 0) {
                            progress = (saving.getCurrentAmount() / saving.getTargetAmount()) * 100;
                        }
                        if (progress > 100) progress = 100;
                        totalProgressSum += progress;
                    }
                }

                double avgProgress = totalGoals > 0 ? (totalProgressSum / totalGoals) : 0;
                double completedGoalsPercent = totalGoals > 0 ? ((double) completedGoals / totalGoals) * 100 : 0;

                double healthScore = (avgProgress * 0.7) + (completedGoalsPercent * 0.3);

                updateUI(totalGoals, activeGoals, completedGoals, totalTarget, totalSaved, avgProgress, healthScore);
            } else {
                Toast.makeText(SavingReportResultActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithinDateRange(String start, String end) {
        if ("MONTH".equals(reportType)) {
            // For simplicity, if the monthYear is present in start or end date
            return (start != null && start.contains(monthYear)) || (end != null && end.contains(monthYear));
        } else if ("CUSTOM".equals(reportType)) {
            try {
                Date rStart = dateFormat.parse(startDateStr);
                Date rEnd = dateFormat.parse(endDateStr);
                Date sStart = dateFormat.parse(start);
                Date sEnd = dateFormat.parse(end);
                
                // Goal overlaps with the report period if goal_start <= report_end AND goal_end >= report_start
                if (rStart != null && rEnd != null && sStart != null && sEnd != null) {
                    return !sStart.after(rEnd) && !sEnd.before(rStart);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void updateUI(int totalGoals, int activeGoals, int completedGoals, double totalTarget, double totalSaved, double avgProgress, double healthScore) {
        tvTotalGoals.setText(String.valueOf(totalGoals));
        tvActiveGoals.setText(String.valueOf(activeGoals));
        tvCompletedGoals.setText(String.valueOf(completedGoals));
        
        tvTotalTarget.setText(CurrencyHelper.formatMoney(this, totalTarget));
        tvTotalSaved.setText(CurrencyHelper.formatMoney(this, totalSaved));
        
        double remaining = totalTarget - totalSaved;
        if (remaining < 0) remaining = 0;
        tvTotalRemaining.setText(CurrencyHelper.formatMoney(this, remaining));
        
        tvAvgProgress.setText(String.format(Locale.getDefault(), "%.0f%%", avgProgress));
        pbAvgProgress.setProgress((int) avgProgress);
        
        tvHealthScoreValue.setText(String.format(Locale.getDefault(), "Score: %.0f/100", healthScore));
        
        if (healthScore >= 80) {
            tvHealthScore.setText("Excellent");
            tvHealthScore.setTextColor(getResources().getColor(R.color.pill_positive_text, null));
        } else if (healthScore >= 60) {
            tvHealthScore.setText("Good");
            tvHealthScore.setTextColor(getResources().getColor(R.color.qa_secondary_icon, null));
        } else {
            tvHealthScore.setText("Needs Improvement");
            tvHealthScore.setTextColor(getResources().getColor(R.color.danger_text, null));
        }
    }
}
