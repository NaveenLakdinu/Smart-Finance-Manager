package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoanDetailsActivity extends AppCompatActivity {

    // UI Variables
    private EditText etLoanAmount, etMonthlyInstallment, etMonthsPaid;
    private Spinner spinnerPaymentMethod;
    private Button btnNext;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_loan_details);

        // 1. with xml id connect java variables
        etLoanAmount = findViewById(R.id.etLoanAmount);
        etMonthlyInstallment = findViewById(R.id.etMonthlyInstallment);
        etMonthsPaid = findViewById(R.id.etMonthsPaid);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnNext = findViewById(R.id.btnNextLoan);
        backButton = findViewById(R.id.backButton);


        loadExistingData();


        backButton.setOnClickListener(v -> {

            UserRegistrationData.getInstance().hasLoan = false;
            finish();
        });


        btnNext.setOnClickListener(v -> {
            saveLoanData();
        });
    }

    private void saveLoanData() {

        String amount = etLoanAmount.getText().toString().trim();
        String installment = etMonthlyInstallment.getText().toString().trim();
        String months = etMonthsPaid.getText().toString().trim();


        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();
        int selectedPosition = spinnerPaymentMethod.getSelectedItemPosition();


        if (amount.isEmpty() || installment.isEmpty() || months.isEmpty()) {
            Toast.makeText(this, "Please fill all loan details", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDATION
        if (selectedPosition == 0) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        // SINGLETON input data
        UserRegistrationData data = UserRegistrationData.getInstance();
        data.hasLoan = true;
        data.loanAmount = amount;
        data.monthlyInstallment = installment;
        data.monthsPaid = months;
        data.paymentMethod = paymentMethod;

        Toast.makeText(this, "Loan details updated!", Toast.LENGTH_SHORT).show();

        // RegisterActivity back to
        finish();
    }

    private void loadExistingData() {
        UserRegistrationData data = UserRegistrationData.getInstance();

        if (data.hasLoan) {
            etLoanAmount.setText(data.loanAmount);
            etMonthlyInstallment.setText(data.monthlyInstallment);
            etMonthsPaid.setText(data.monthsPaid);


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        UserRegistrationData.getInstance().hasLoan = false;
    }
}