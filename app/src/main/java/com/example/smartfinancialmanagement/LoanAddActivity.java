package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

/**
 * Activity to add a new loan to the user's profile.
 * Includes real-time EMI calculation and field validation.
 */
public class LoanAddActivity extends AppCompatActivity {

    // UI elements
    private ImageView btnBack;
    private EditText etLoanName, etPrincipalAmount, etInterestRate, etDuration;
    private TextView estimatedMonthlyPayment, txtTitle;
    private MaterialButton btnAddLoan;

    private Loan existingLoan;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_add_loan);

        // Check if we are in edit mode
        if (getIntent().hasExtra("loan")) {
            existingLoan = (Loan) getIntent().getSerializableExtra("loan");
            isEditMode = true;
        }

        // 1. Initialize all UI components
        initViews();

        // 2. Setup button click listeners
        setupListeners();

        // 3. Setup real-time EMI calculation whenever input changes
        setupCalculationLogic();

        if (isEditMode && existingLoan != null) {
            populateFields();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etLoanName = findViewById(R.id.etLoanName);
        etPrincipalAmount = findViewById(R.id.etPrincipalAmount);
        etInterestRate = findViewById(R.id.etInterestRate);
        etDuration = findViewById(R.id.etDuration);
        estimatedMonthlyPayment = findViewById(R.id.estimatedMonthlyPayment);
        btnAddLoan = findViewById(R.id.btnAddLoan);
        txtTitle = findViewById(R.id.txtTitle);

        if (isEditMode) {
            txtTitle.setText("Edit Loan");
            btnAddLoan.setText("Update Loan");
        }
    }

    private void populateFields() {
        etLoanName.setText(existingLoan.getLoanName());
        etPrincipalAmount.setText(String.valueOf(existingLoan.getPrincipalAmount()));
        etInterestRate.setText(String.valueOf(existingLoan.getInterestRate()));
        etDuration.setText(String.valueOf(existingLoan.getDurationMonths()));
        calculateEMI();
    }

    private void setupListeners() {
        // Back button to return to the previous screen
        btnBack.setOnClickListener(v -> finish());

        // Validate and save loan data when 'Add New Loan' is clicked
        btnAddLoan.setOnClickListener(v -> {
            if (validateInputs()) {
                saveLoanToFirestore();
            }
        });
    }

    /**
     * Saves the loan data to Firebase Firestore under the current user's document.
     */
    private void saveLoanToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to add a loan", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String name = etLoanName.getText().toString().trim();
        double principal = Double.parseDouble(etPrincipalAmount.getText().toString().trim());
        double rate = Double.parseDouble(etInterestRate.getText().toString().trim());
        int duration = Integer.parseInt(etDuration.getText().toString().trim());
        String emiText = estimatedMonthlyPayment.getText().toString().replaceAll("[^0-9.]", "");
        double emi = 0;
        if (!emiText.isEmpty()) {
            emi = Double.parseDouble(emiText);
        }

        // Disable button to prevent multiple clicks
        btnAddLoan.setEnabled(false);
        btnAddLoan.setText(isEditMode ? "Updating..." : "Saving...");

        // Create loan object
        java.util.Map<String, Object> loanData = new java.util.HashMap<>();
        loanData.put("loanName", name);
        loanData.put("principalAmount", principal);
        loanData.put("interestRate", rate);
        loanData.put("durationMonths", duration);
        loanData.put("monthlyEmi", emi);
        if (!isEditMode) {
            loanData.put("createdAt", System.currentTimeMillis());
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (isEditMode && existingLoan != null) {
            // Update existing loan
            db.collection("users").document(uid).collection("loans")
                    .document(existingLoan.getId())
                    .update(loanData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Loan updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnAddLoan.setEnabled(true);
                        btnAddLoan.setText("Update Loan");
                        Toast.makeText(this, "Failed to update loan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // Add new loan
            db.collection("users").document(uid).collection("loans")
                    .add(loanData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Loan '" + name + "' added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnAddLoan.setEnabled(true);
                        btnAddLoan.setText("Add New Loan");
                        Toast.makeText(this, "Failed to add loan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    /**
     * Validates that all input fields are filled correctly.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        String name = etLoanName.getText().toString().trim();
        String amount = etPrincipalAmount.getText().toString().trim();
        String rate = etInterestRate.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();

        if (name.isEmpty()) {
            etLoanName.setError("Please enter a loan name");
            etLoanName.requestFocus();
            return false;
        }

        if (amount.isEmpty() || Double.parseDouble(amount) <= 0) {
            etPrincipalAmount.setError("Please enter a valid principal amount");
            etPrincipalAmount.requestFocus();
            return false;
        }

        if (rate.isEmpty() || Double.parseDouble(rate) <= 0) {
            etInterestRate.setError("Please enter a valid interest rate");
            etInterestRate.requestFocus();
            return false;
        }

        if (duration.isEmpty() || Integer.parseInt(duration) <= 0) {
            etDuration.setError("Please enter a valid duration in months");
            etDuration.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Adds TextWatchers to numerical fields to update the EMI calculation in real-time.
     */
    private void setupCalculationLogic() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateEMI();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPrincipalAmount.addTextChangedListener(watcher);
        etInterestRate.addTextChangedListener(watcher);
        etDuration.addTextChangedListener(watcher);
    }

    /**
     * Calculates the Equated Monthly Installment (EMI) using the formula:
     * EMI = [P * r * (1 + r)^n] / [((1 + r)^n) - 1]
     */
    private void calculateEMI() {
        try {
            String amountStr = etPrincipalAmount.getText().toString().trim();
            String rateStr = etInterestRate.getText().toString().trim();
            String durationStr = etDuration.getText().toString().trim();

            if (!amountStr.isEmpty() && !rateStr.isEmpty() && !durationStr.isEmpty()) {
                double p = Double.parseDouble(amountStr);
                double annualRate = Double.parseDouble(rateStr);
                int n = Integer.parseInt(durationStr);

                // Monthly interest rate
                double r = annualRate / (12 * 100);

                if (r == 0) {
                    double emi = p / n;
                    estimatedMonthlyPayment.setText(String.format(Locale.US, "LKR %.2f", emi));
                } else {
                    double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                    estimatedMonthlyPayment.setText(String.format(Locale.US, "LKR %.2f", emi));
                }
            } else {
                estimatedMonthlyPayment.setText("LKR 0.00");
            }
        } catch (Exception e) {
            estimatedMonthlyPayment.setText("LKR 0.00");
        }
    }
}
