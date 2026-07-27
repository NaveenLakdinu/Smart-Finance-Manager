package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkerIncomeActivity extends AppCompatActivity {

    private EditText etCompanyName, etDesignation, etMonthlySalary, etBonusAmount, etOvertimeRate;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_income);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        initViews();
        loadExistingData();
        setupListeners();
    }

    private void initViews() {
        etCompanyName = findViewById(R.id.etCompanyName);
        etDesignation = findViewById(R.id.etDesignation);
        etMonthlySalary = findViewById(R.id.etMonthlySalary);
        etBonusAmount = findViewById(R.id.etBonusAmount);
        etOvertimeRate = findViewById(R.id.etOvertimeRate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadExistingData() {
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getUid())
            .collection("worker_profile").document("profile_data").get()
            .addOnSuccessListener(documentSnapshot -> {
                progressBar.setVisibility(View.GONE);

                if (documentSnapshot.exists()) {
                    String companyName = documentSnapshot.getString("companyName");
                    String designation = documentSnapshot.getString("designation");
                    Double monthlySalary = documentSnapshot.getDouble("monthlySalary");
                    Double bonusAmount = documentSnapshot.getDouble("bonusAmount");
                    Double overtimeRate = documentSnapshot.getDouble("overtimeRate");

                    if (companyName != null && !companyName.isEmpty()) {
                        etCompanyName.setText(companyName);
                    }
                    if (designation != null && !designation.isEmpty()) {
                        etDesignation.setText(designation);
                    }
                    if (monthlySalary != null && monthlySalary > 0) {
                        etMonthlySalary.setText(String.format(Locale.US, "%.2f", monthlySalary));
                    }
                    if (bonusAmount != null && bonusAmount > 0) {
                        etBonusAmount.setText(String.format(Locale.US, "%.2f", bonusAmount));
                    }
                    if (overtimeRate != null && overtimeRate > 0) {
                        etOvertimeRate.setText(String.format(Locale.US, "%.2f", overtimeRate));
                    }
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSave).setOnClickListener(v -> saveIncomeData());
    }

    private void saveIncomeData() {
        String companyName = etCompanyName.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        String salaryStr = etMonthlySalary.getText().toString().trim();
        String bonusStr = etBonusAmount.getText().toString().trim();
        String overtimeStr = etOvertimeRate.getText().toString().trim();

        double monthlySalary = 0;
        double bonusAmount = 0;
        double overtimeRate = 0;

        if (!salaryStr.isEmpty()) {
            try {
                monthlySalary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException e) {
                etMonthlySalary.setError("Invalid number");
                etMonthlySalary.requestFocus();
                return;
            }
        }

        if (!bonusStr.isEmpty()) {
            try {
                bonusAmount = Double.parseDouble(bonusStr);
            } catch (NumberFormatException e) {
                etBonusAmount.setError("Invalid number");
                etBonusAmount.requestFocus();
                return;
            }
        }

        if (!overtimeStr.isEmpty()) {
            try {
                overtimeRate = Double.parseDouble(overtimeStr);
            } catch (NumberFormatException e) {
                etOvertimeRate.setError("Invalid number");
                etOvertimeRate.requestFocus();
                return;
            }
        }

        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSave).setEnabled(false);

        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("companyName", companyName);
        incomeData.put("designation", designation);
        incomeData.put("monthlySalary", monthlySalary);
        incomeData.put("bonusAmount", bonusAmount);
        incomeData.put("overtimeRate", overtimeRate);

        db.collection("users").document(user.getUid())
            .collection("worker_profile").document("profile_data")
            .update(incomeData)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.btnSave).setEnabled(true);
                Toast.makeText(this, "Income details saved!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                db.collection("users").document(user.getUid())
                    .collection("worker_profile").document("profile_data")
                    .set(incomeData)
                    .addOnSuccessListener(aVoid2 -> {
                        progressBar.setVisibility(View.GONE);
                        findViewById(R.id.btnSave).setEnabled(true);
                        Toast.makeText(this, "Income details saved!", Toast.LENGTH_SHORT).show();
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
