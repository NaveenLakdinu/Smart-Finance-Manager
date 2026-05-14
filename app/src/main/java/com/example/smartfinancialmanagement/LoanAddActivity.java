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
import java.util.Locale;

/**
 * Activity to add a new loan to the user's profile.
 * Includes real-time EMI calculation and field validation.
 */
public class LoanAddActivity extends AppCompatActivity {

    // UI elements
    private ImageView btnBack;
    private EditText etLoanName, etPrincipalAmount, etInterestRate, etDuration;
    private TextView estimatedMonthlyPayment;
    private MaterialButton btnAddLoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_add_loan);

        // 1. Initialize all UI components
        initViews();

        // 2. Setup button click listeners
        setupListeners();

        // 3. Setup real-time EMI calculation whenever input changes
        setupCalculationLogic();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etLoanName = findViewById(R.id.etLoanName);
        etPrincipalAmount = findViewById(R.id.etPrincipalAmount);
        etInterestRate = findViewById(R.id.etInterestRate);
        etDuration = findViewById(R.id.etDuration);
        estimatedMonthlyPayment = findViewById(R.id.estimatedMonthlyPayment);
        btnAddLoan = findViewById(R.id.btnAddLoan);
    }

    private void setupListeners() {
        // Back button to return to the previous screen
        btnBack.setOnClickListener(v -> finish());

        // Validate and save loan data when 'Add New Loan' is clicked
        btnAddLoan.setOnClickListener(v -> {
            if (validateInputs()) {
                // TODO: Logic to save loan data to Firebase or Local DB
                Toast.makeText(this, "Loan '" + etLoanName.getText().toString() + "' added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
                    estimatedMonthlyPayment.setText(String.format(Locale.US, "$%.2f", emi));
                } else {
                    double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                    estimatedMonthlyPayment.setText(String.format(Locale.US, "$%.2f", emi));
                }
            } else {
                estimatedMonthlyPayment.setText("$0.00");
            }
        } catch (Exception e) {
            estimatedMonthlyPayment.setText("$0.00");
        }
    }
}
