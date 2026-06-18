package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Header / Profile Info
    private TextView txtProfileLetter;
    private TextView txtGreeting;
    private TextView txtUserEmail;
    private TextView btnNotifications;

    // Summary State Fields
    private TextView txtTotalCount;
    private TextView txtSubMessage;

    // Feature Modules Grid
    private MaterialCardView cardManageLoan;
    private MaterialCardView cardManageSubscription;
    private MaterialCardView cardManageUtility;
    private MaterialCardView cardB2BInvoice;
    private MaterialCardView cardAnalytics;

    // Bottom Feed Lists
    private View recentSection;
    private TextView txtViewAll;
    private RecyclerView SampleRecycler;

    // Account Actions
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard); // Pointing directly to your updated layout container

        initializeViews();
        setupClickListeners();
        configureRecentListsFeed();
    }

    /**
     * Maps and initializes layout components safely.
     */
    private void initializeViews() {
        // Header Binds
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnNotifications = findViewById(R.id.btnNotifications);

        // Core Balance Summary Info
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);

        // Grid Actions
        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardB2BInvoice = findViewById(R.id.B2BInvoice);
        cardAnalytics = findViewById(R.id.cardAnalytics);

        // Secondary List Containers
        recentSection = findViewById(R.id.recentSection);
        txtViewAll = findViewById(R.id.txtViewAll);
        SampleRecycler = findViewById(R.id.recyclerRecent);

        // Bottom Operations Terminal
        btnLogout = findViewById(R.id.btnLogout);
    }

    /**
     * Chains UI component event loop triggers using optimal Java lambda blocks.
     */
    private void setupClickListeners() {
        // Notification Alert Bell Interaction Handler
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Opening corporate notification stream...", Toast.LENGTH_SHORT).show()
        );

        // Grid Component Intercept Vectors
        cardManageLoan.setOnClickListener(v -> showModuleToast("Loan Management"));
        cardManageUtility.setOnClickListener(v -> showModuleToast("Utility Bill Tracker"));

        cardManageSubscription.setOnClickListener(v -> {
            // Flips visibility flags to expose preview adapters conditionally
            if (recentSection.getVisibility() == View.GONE) {
                recentSection.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Showing Subscriptions feed preview below", Toast.LENGTH_SHORT).show();
            } else {
                recentSection.setVisibility(View.GONE);
            }
        });

        // Direct Core Component Activations
        cardB2BInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, InvoiceHubActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });

        txtViewAll.setOnClickListener(v -> showModuleToast("All System Subscriptions"));

        // Global Sign-Out Logic Route
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Signing out secure workspace session...", Toast.LENGTH_LONG).show();
            // Optional: redirect to a login portal activity screen
            finish();
        });
    }

    /**
     * Initializes structural linear state layouts safely for the recent view.
     */
    private void configureRecentListsFeed() {
        SampleRecycler.setLayoutManager(new LinearLayoutManager(this));
        // Recycler row rendering elements link here via standard layout templates when adapter data models are populated.
    }

    /**
     * Standardized message printer shortcut.
     */
    private void showModuleToast(String moduleName) {
        Toast.makeText(this, moduleName + " module integration coming soon!", Toast.LENGTH_SHORT).show();
    }
}