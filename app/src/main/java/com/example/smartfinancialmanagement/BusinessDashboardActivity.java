package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Profile Area Views
    private TextView btnNotifications;

    // Business Feature Cards
    private MaterialCardView cardManageLoan;
    private MaterialCardView cardManageSubscription;
    private MaterialCardView cardManageUtility;
    private MaterialCardView cardB2BInvoice;
    private MaterialCardView cardAnalytics;

    // Account Actions
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard); // Clean mapping to your updated business XML layout

        // 1. Initialize Interactive Views
        initViews();

        // 2. Setup Click Listeners
        setupClickListeners();
    }

    /**
     * Finds and binds the interactive views from your updated layout.
     */
    private void initViews() {
        // Top Layout Interactions
        btnNotifications = findViewById(R.id.btnNotifications);

        // Updated Feature Grid Material Cards
        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardB2BInvoice = findViewById(R.id.cardB2BInvoice); // Triggers B2B Invoice Hub
        cardAnalytics = findViewById(R.id.cardAnalytics);

        // Exit Navigation
        btnLogout = findViewById(R.id.btnLogout);
    }

    /**
     * Configures click actions for your operational layout assets.
     */
    private void setupClickListeners() {
        // Notification Hub View
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Notifications clicked");
            }
        });

        // Credit & Loan Features
        cardManageLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BusinessDashboardActivity.this, "Paused Subscriptions - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        // Recurring Plans Tracker
        cardManageSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BusinessDashboardActivity.this, "Paused Subscriptions - Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        // Fixed Utility Costs
        cardManageUtility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this, UtilityManagerActivity.class);
                startActivity(intent);
            }
        });

        // New Feature Workflow: Handles your Accounts Receivable
        cardB2BInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantly opens Screen 1: The main overview invoice portal
                Intent intent = new Intent(BusinessDashboardActivity.this, InvoiceHubActivity.class);
                startActivity(intent);
            }
        });

        // Operational Financial Reports
        cardAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this, AnalyticsActivity.class);
                startActivity(intent);
            }
        });

        // Terminate Session Loop
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Signing out...");
                // Add your real authorization session clear logic here if necessary
                finish();
            }
        });
    }

    /**
     * Helper method to easily display a toast message when an item is tapped.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}