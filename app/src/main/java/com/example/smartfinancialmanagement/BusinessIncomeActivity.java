package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BusinessIncomeActivity extends AppCompatActivity {

    private EditText etBusinessName, etRegNumber, etIndustryType;
    private EditText etMonthlyRevenue, etBusinessExpenses, etProfitMargin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_income);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        initViews();
        loadExistingData();
        setupListeners();
    }

    private void initViews() {
        etBusinessName = findViewById(R.id.etBusinessName);
        etRegNumber = findViewById(R.id.etRegNumber);
        etIndustryType = findViewById(R.id.etIndustryType);
        etMonthlyRevenue = findViewById(R.id.etMonthlyRevenue);
        etBusinessExpenses = findViewById(R.id.etBusinessExpenses);
        etProfitMargin = findViewById(R.id.etProfitMargin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadExistingData() {
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getUid())
            .collection("business_profile").document("profile_data").get()
            .addOnSuccessListener(documentSnapshot -> {
                progressBar.setVisibility(View.GONE);

                if (documentSnapshot.exists()) {
                    String bizName = documentSnapshot.getString("businessName");
                    String regNumber = documentSnapshot.getString("regNumber");
                    String industryType = documentSnapshot.getString("industryType");
                    Double monthlyRevenue = documentSnapshot.getDouble("averageMonthlyRevenue");
                    Double businessExpenses = documentSnapshot.getDouble("businessExpenses");
                    Double profitMargin = documentSnapshot.getDouble("profitMargin");

                    if (bizName != null && !bizName.isEmpty()) etBusinessName.setText(bizName);
                    if (regNumber != null && !regNumber.isEmpty()) etRegNumber.setText(regNumber);
                    if (industryType != null && !industryType.isEmpty()) etIndustryType.setText(industryType);
                    if (monthlyRevenue != null && monthlyRevenue > 0) etMonthlyRevenue.setText(String.format(Locale.US, "%.2f", monthlyRevenue));
                    if (businessExpenses != null && businessExpenses > 0) etBusinessExpenses.setText(String.format(Locale.US, "%.2f", businessExpenses));
                    if (profitMargin != null) etProfitMargin.setText(String.format(Locale.US, "%.2f", profitMargin));
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveRevenueData());
    }

    private void saveRevenueData() {
        String bizName = etBusinessName.getText().toString().trim();
        String regNumber = etRegNumber.getText().toString().trim();
        String industryType = etIndustryType.getText().toString().trim();
        String revenueStr = etMonthlyRevenue.getText().toString().trim();
        String expensesStr = etBusinessExpenses.getText().toString().trim();
        String profitStr = etProfitMargin.getText().toString().trim();

        double monthlyRevenue = 0;
        double businessExpenses = 0;
        double profitMargin = 0;

        if (!revenueStr.isEmpty()) {
            try {
                monthlyRevenue = Double.parseDouble(revenueStr);
            } catch (NumberFormatException e) {
                etMonthlyRevenue.setError("Invalid number");
                etMonthlyRevenue.requestFocus();
                return;
            }
        }

        if (!expensesStr.isEmpty()) {
            try {
                businessExpenses = Double.parseDouble(expensesStr);
            } catch (NumberFormatException e) {
                etBusinessExpenses.setError("Invalid number");
                etBusinessExpenses.requestFocus();
                return;
            }
        }

        if (!profitStr.isEmpty()) {
            try {
                profitMargin = Double.parseDouble(profitStr);
            } catch (NumberFormatException e) {
                etProfitMargin.setError("Invalid number");
                etProfitMargin.requestFocus();
                return;
            }
        }

        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSave).setEnabled(false);

        Map<String, Object> revenueData = new HashMap<>();
        revenueData.put("businessName", bizName);
        revenueData.put("regNumber", regNumber);
        revenueData.put("industryType", industryType);
        revenueData.put("averageMonthlyRevenue", monthlyRevenue);
        revenueData.put("businessExpenses", businessExpenses);
        revenueData.put("profitMargin", profitMargin);

        db.collection("users").document(user.getUid())
            .collection("business_profile").document("profile_data")
            .update(revenueData)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.btnSave).setEnabled(true);
                Toast.makeText(this, "Revenue details saved!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                db.collection("users").document(user.getUid())
                    .collection("business_profile").document("profile_data")
                    .set(revenueData)
                    .addOnSuccessListener(aVoid2 -> {
                        progressBar.setVisibility(View.GONE);
                        findViewById(R.id.btnSave).setEnabled(true);
                        Toast.makeText(this, "Revenue details saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e2 -> {
                        progressBar.setVisibility(View.GONE);
                        findViewById(R.id.btnSave).setEnabled(true);
                        Toast.makeText(this, "Failed to save: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
    }
}
