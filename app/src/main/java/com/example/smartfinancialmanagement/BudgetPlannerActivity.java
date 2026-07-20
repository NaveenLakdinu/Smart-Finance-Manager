package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class BudgetPlannerActivity extends AppCompatActivity {

    private EditText editMonthlyAllowance;
    private EditText editScholarshipAmount;
    private EditText editPartTimeIncome;
    
    private TextView btnDuration3M;
    private TextView btnDuration4M;
    private TextView btnDuration6M;
    private TextView btnDuration12M;
    
    private int selectedDuration = 0; // Default none
    private BudgetViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_budget);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Hide results layout
        LinearLayout layoutResults = findViewById(R.id.layoutResults);
        if (layoutResults != null) {
            layoutResults.setVisibility(View.GONE);
        }
        
        // Hide semester summary card on the planner input view
        MaterialCardView cardSemesterSummary = findViewById(R.id.cardSemesterSummary);
        if (cardSemesterSummary != null) {
            cardSemesterSummary.setVisibility(View.GONE);
        }
        
        // Show income card just in case it was hidden
        MaterialCardView cardIncomeDetails = findViewById(R.id.cardIncomeDetails);
        if (cardIncomeDetails != null) {
            cardIncomeDetails.setVisibility(View.VISIBLE);
        }

        initViews();

        setupObservers();
    }

    private void initViews() {
        editMonthlyAllowance = findViewById(R.id.editMonthlyAllowance);
        editScholarshipAmount = findViewById(R.id.editScholarshipAmount);
        editPartTimeIncome = findViewById(R.id.editPartTimeIncome);

        btnDuration3M = findViewById(R.id.btnDuration3M);
        btnDuration4M = findViewById(R.id.btnDuration4M);
        btnDuration6M = findViewById(R.id.btnDuration6M);
        btnDuration12M = findViewById(R.id.btnDuration12M);

        View.OnClickListener durationListener = v -> {
            resetDurationButtons();
            v.setBackgroundResource(R.drawable.bg_segment_active);
            ((TextView) v).setTextColor(Color.parseColor("#FFFFFF"));

            int id = v.getId();
            if (id == R.id.btnDuration3M) selectedDuration = 3;
            else if (id == R.id.btnDuration4M) selectedDuration = 4;
            else if (id == R.id.btnDuration6M) selectedDuration = 6;
            else if (id == R.id.btnDuration12M) selectedDuration = 12;
        };

        btnDuration3M.setOnClickListener(durationListener);
        btnDuration4M.setOnClickListener(durationListener);
        btnDuration6M.setOnClickListener(durationListener);
        btnDuration12M.setOnClickListener(durationListener);

        // Reset initially (since layout might have 6M active by default)
        resetDurationButtons();
        selectedDuration = 0;

        MaterialButton btnCalculateBudget = findViewById(R.id.btnCalculateBudget);
        btnCalculateBudget.setOnClickListener(v -> validateAndCalculate());
        
        View btnBack = findViewById(R.id.btnBackContainer);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        loadUserData();
    }

    private void resetDurationButtons() {
        TextView[] buttons = {btnDuration3M, btnDuration4M, btnDuration6M, btnDuration12M};
        for (TextView btn : buttons) {
            btn.setBackgroundResource(R.drawable.bg_segment_inactive);
            btn.setTextColor(Color.parseColor("#5A6470"));
        }
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

    private void validateAndCalculate() {
        String allowanceStr = editMonthlyAllowance.getText().toString().trim();
        String scholarshipStr = editScholarshipAmount.getText().toString().trim();
        String partTimeStr = editPartTimeIncome.getText().toString().trim();

        if (TextUtils.isEmpty(allowanceStr)) {
            editMonthlyAllowance.setError("Monthly allowance required");
            return;
        }

        double allowance, scholarship = 0, partTime = 0;
        try {
            allowance = Double.parseDouble(allowanceStr);
            if (allowance < 0) {
                editMonthlyAllowance.setError("Cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            editMonthlyAllowance.setError("Invalid number");
            return;
        }

        if (!TextUtils.isEmpty(scholarshipStr)) {
            try {
                scholarship = Double.parseDouble(scholarshipStr);
                if (scholarship < 0) scholarship = 0;
            } catch (NumberFormatException e) {
                editScholarshipAmount.setError("Invalid number");
                return;
            }
        }

        if (!TextUtils.isEmpty(partTimeStr)) {
            try {
                partTime = Double.parseDouble(partTimeStr);
                if (partTime < 0) partTime = 0;
            } catch (NumberFormatException e) {
                editPartTimeIncome.setError("Invalid number");
                return;
            }
        }

        if (selectedDuration == 0) {
            Toast.makeText(this, "Please select semester duration", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.calculateAndSaveBudget(allowance, scholarship, partTime, selectedDuration);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            MaterialButton btnCalculateBudget = findViewById(R.id.btnCalculateBudget);
            if (isLoading) {
                btnCalculateBudget.setEnabled(false);
                btnCalculateBudget.setText("Calculating...");
            } else {
                btnCalculateBudget.setEnabled(true);
                btnCalculateBudget.setText("Calculate Budget");
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCalculationSuccess().observe(this, model -> {
            if (model != null) {
                Intent intent = new Intent(this, BudgetSummaryActivity.class);
                intent.putExtra("budgetModel", model);
                startActivity(intent);
            }
        });
    }


}
