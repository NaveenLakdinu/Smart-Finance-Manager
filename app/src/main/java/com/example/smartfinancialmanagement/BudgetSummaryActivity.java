package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Calendar;
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

    @Override
    protected void onResume() {
        super.onResume();
        if (model != null) populateData();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBackContainer);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        loadUserData();
    }

    private void loadUserData() {
        TextView txtGreeting = findViewById(R.id.txtGreeting);
        if (txtGreeting == null) return;

        String baseGreeting = getGreeting();
        
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            txtGreeting.setText(baseGreeting + " 👋");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    String name = documentSnapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        String firstName = name.trim().split("\\s+")[0];
                        txtGreeting.setText(baseGreeting + ", " + firstName + " 👋");
                    } else {
                        txtGreeting.setText(baseGreeting + " 👋");
                    }
                } else {
                    txtGreeting.setText(baseGreeting + " 👋");
                }
            })
            .addOnFailureListener(e -> {
                txtGreeting.setText(baseGreeting + " 👋");
            });
    }

    private String getGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            return "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    private void populateData() {
        TextView txtTotalIncome = findViewById(R.id.txtTotalIncome);
        if (txtTotalIncome != null) txtTotalIncome.setText(CurrencyHelper.formatMoney(this, model.getSemesterIncome()));

        TextView txtDailyLimit = findViewById(R.id.txtDailyLimit);
        if (txtDailyLimit != null) txtDailyLimit.setText(CurrencyHelper.formatMoney(this, model.getDailyBudget()));

        TextView txtWeeklyLimit = findViewById(R.id.txtWeeklyLimit);
        if (txtWeeklyLimit != null) txtWeeklyLimit.setText(CurrencyHelper.formatMoney(this, model.getWeeklyBudget()));

        TextView txtMonthlyLimit = findViewById(R.id.txtMonthlyLimit);
        if (txtMonthlyLimit != null) txtMonthlyLimit.setText(CurrencyHelper.formatMoney(this, model.getMonthlyBudget()));

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
        if (txtSemesterDate != null) txtSemesterDate.setText("Period: " + model.getSemesterStart() + " – " + model.getSemesterEnd());

        TextView txtSemesterStatus = findViewById(R.id.txtSemesterStatus);
        if (txtSemesterStatus != null) {
            int months = (int) Math.round(model.getDuration() / 30.0);
            txtSemesterStatus.setText(months + " months remaining");
        }
    }


}
