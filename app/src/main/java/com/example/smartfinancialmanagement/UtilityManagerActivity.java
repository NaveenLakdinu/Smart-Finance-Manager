package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class UtilityManagerActivity extends AppCompatActivity {

    private Button btnRegisterNewBill;
    private Button btnGetReport;
    private Button btnViewBill;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_manager);

        // Initialize management buttons from layout text elements
        btnRegisterNewBill = findViewById(R.id.btnRegisterNewBill); // Add android:id="@+id/btnRegisterNewBill" to your layout button if not present
        btnGetReport = findViewById(R.id.btnGetReport);           // Add android:id="@+id/btnGetReport" to your layout button if not present
        btnViewBill = findViewById(R.id.btnViewBill);             // Add android:id="@+id/btnViewBill" to your layout button if not present
        backButton    = findViewById(R.id.backButton);
        // Open Register Screen
        btnRegisterNewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UtilityManagerActivity.this, RegisterBillActivity.class);
                startActivity(intent);
            }
        });

        // Open Get Report Input Form Screen
        btnGetReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UtilityManagerActivity.this, GetReportActivity.class);
                startActivity(intent);
            }
        });

        // Open View All Utility Bills Screen
        btnViewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                startActivity(intent);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this screen and goes back
            }
        });
    }
}
