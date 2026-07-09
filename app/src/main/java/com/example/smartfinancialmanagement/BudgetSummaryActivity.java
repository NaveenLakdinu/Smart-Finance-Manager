package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.NumberFormat;
import java.util.Locale;

public class BudgetSummaryActivity extends AppCompatActivity {

    private BudgetModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_budget);

        if (getIntent() != null && getIntent().hasExtra("budgetModel")) {
            model = getIntent().getParcelableExtra("budgetModel");
        }

        if (model == null) {
            finish();
            return;
        }

        // Adjust UI for summary mode
        MaterialCardView cardIncomeDetails = findViewById(R.id.cardIncomeDetails);
        if (cardIncomeDetails != null) cardIncomeDetails.setVisibility(View.GONE);
        
        LinearLayout layoutResults = findViewById(R.id.layoutResults);
        if (layoutResults != null) layoutResults.setVisibility(View.VISIBLE);

        initViews();
        populateData();

    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBackContainer);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void populateData() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        currencyFormat.setMaximumFractionDigits(0);
        
        TextView txtTotalIncome = findViewById(R.id.txtTotalIncome);
        if (txtTotalIncome != null) txtTotalIncome.setText(currencyFormat.format(model.getSemesterIncome()));

        TextView txtDailyLimit = findViewById(R.id.txtDailyLimit);
        if (txtDailyLimit != null) txtDailyLimit.setText(currencyFormat.format(model.getDailyBudget()));

        TextView txtWeeklyLimit = findViewById(R.id.txtWeeklyLimit);
        if (txtWeeklyLimit != null) txtWeeklyLimit.setText(currencyFormat.format(model.getWeeklyBudget()));

        TextView txtMonthlyLimit = findViewById(R.id.txtMonthlyLimit);
        if (txtMonthlyLimit != null) txtMonthlyLimit.setText(currencyFormat.format(model.getMonthlyBudget()));

        CircularProgressIndicator progressHealthScore = findViewById(R.id.progressHealthScore);
        if (progressHealthScore != null) progressHealthScore.setProgress(model.getFinancialScore());

        TextView txtHealthScore = findViewById(R.id.txtHealthScore);
        if (txtHealthScore != null) txtHealthScore.setText(String.valueOf(model.getFinancialScore()));

        TextView txtHealthStatus = findViewById(R.id.txtHealthStatus);
        if (txtHealthStatus != null) txtHealthStatus.setText(model.getHealthStatus());

        TextView txtInsight1 = findViewById(R.id.txtInsight1);
        if (txtInsight1 != null) txtInsight1.setText(model.getInsight1());

        TextView txtInsight2 = findViewById(R.id.txtInsight2);
        if (txtInsight2 != null) txtInsight2.setText(model.getInsight2());

        TextView txtInsight3 = findViewById(R.id.txtInsight3);
        if (txtInsight3 != null) txtInsight3.setText(model.getInsight3());

        TextView txtSemesterDate = findViewById(R.id.txtSemesterDate);
        if (txtSemesterDate != null) txtSemesterDate.setText("Semester: " + model.getSemesterStart() + " – " + model.getSemesterEnd());

        TextView txtSemesterStatus = findViewById(R.id.txtSemesterStatus);
        if (txtSemesterStatus != null) txtSemesterStatus.setText(model.getDuration() + " months remaining");
    }


}
