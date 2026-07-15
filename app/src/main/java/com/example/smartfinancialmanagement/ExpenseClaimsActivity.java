package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ExpenseClaimsActivity extends AppCompatActivity {

    private View btnNewClaim, btnClaimHistory, btnClaimReport;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claims_manager);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnNewClaim = findViewById(R.id.btnNewClaim);
        btnClaimHistory = findViewById(R.id.btnClaimHistory);
        btnClaimReport = findViewById(R.id.btnClaimReport);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnNewClaim.setOnClickListener(v -> 
            Toast.makeText(this, "New Expense Claim - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        btnClaimHistory.setOnClickListener(v -> 
            Toast.makeText(this, "Claim History - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        btnClaimReport.setOnClickListener(v -> 
            Toast.makeText(this, "Generating Expense Report...", Toast.LENGTH_SHORT).show()
        );
    }
}