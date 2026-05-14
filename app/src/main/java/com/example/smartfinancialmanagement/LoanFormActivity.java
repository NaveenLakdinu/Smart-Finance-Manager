package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class LoanFormActivity extends AppCompatActivity {

    private LinearLayout btnNewLoan, btnCompareLoans, btnLoanReport;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_form);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        btnNewLoan = findViewById(R.id.btnNewLoan);
        btnCompareLoans = findViewById(R.id.btnCompareLoans);
        btnLoanReport = findViewById(R.id.btnLoanReport);

        // Set Click Listeners
        btnBack.setOnClickListener(v -> finish());
        btnNewLoan.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanAddActivity.class);
            startActivity(intent);
        });

        btnCompareLoans.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanCompareActivity.class);
            startActivity(intent);
        });

        btnLoanReport.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanReportActivity.class);
            startActivity(intent);
        });
    }
}
