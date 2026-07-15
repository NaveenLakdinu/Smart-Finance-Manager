package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class UtilityManagerActivity extends AppCompatActivity {

    private View btnRegisterNewBill;
    private View btnGetReport;
    private View btnViewBill;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_manager);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnRegisterNewBill = findViewById(R.id.btnRegisterNewBill);
        btnGetReport = findViewById(R.id.btnGetReport);
        btnViewBill = findViewById(R.id.btnViewBill);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        // Open Register Screen
        btnRegisterNewBill.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, RegisterBillActivity.class);
            startActivity(intent);
        });

        // Open Utility Report Screen
        btnGetReport.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, UtilityReportActivity.class);
            startActivity(intent);
        });

        // Open View All Utility Bills Screen
        btnViewBill.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
        
        // See All Recent
        View txtSeeAllRecent = findViewById(R.id.txtSeeAllRecent);
        if (txtSeeAllRecent != null) {
            txtSeeAllRecent.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                startActivity(intent);
            });
        }
    }
}
