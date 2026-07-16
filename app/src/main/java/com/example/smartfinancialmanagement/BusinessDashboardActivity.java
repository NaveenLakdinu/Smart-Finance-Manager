package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Header / Profile Info
    private TextView txtProfileLetter;
    private TextView txtGreeting;
    private TextView txtUserEmail;
    private TextView btnNotifications;
    private View btnTopLogout;

    // Summary State Fields
    private TextView txtTotalCount;
    private TextView txtSubMessage;

    // Feature Modules Grid
    private MaterialCardView cardManageLoan;
    private MaterialCardView cardManageSubscription;
    private MaterialCardView cardManageUtility;
    private MaterialCardView cardSavingManager;
    private MaterialCardView cardB2BInvoice;
    private MaterialCardView cardAnalytics;

    // Bottom Feed Lists
    private View recentSection;
    private TextView txtViewAll;
    private RecyclerView SampleRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard);

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
        btnTopLogout = findViewById(R.id.btnTopLogout);

        // Core Balance Summary Info
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);

        // Grid Actions
        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardB2BInvoice = findViewById(R.id.B2BInvoice);
        cardAnalytics = findViewById(R.id.cardAnalytics);

        // Secondary List Containers
        recentSection = findViewById(R.id.recentSection);
        txtViewAll = findViewById(R.id.txtViewAll);
        SampleRecycler = findViewById(R.id.recyclerRecent);
    }

    /**
     * Chains UI component event loop triggers using optimal Java lambda blocks.
     */
    private void setupClickListeners() {
        // Notification Alert Bell Interaction Handler
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Opening corporate notification stream...", Toast.LENGTH_SHORT).show()
        );

        // Global Sign-Out Logic Route
        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Grid Component Intercept Vectors
        cardManageLoan.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, LoanFormActivity.class);
            startActivity(intent);
        });

        cardManageUtility.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, UtilityManagerActivity.class);
            startActivity(intent);
        });
        
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SavingManagerActivity.class);
            startActivity(intent);
        });

        cardManageSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
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

        txtViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Initializes structural linear state layouts safely for the recent view.
     */
    private void configureRecentListsFeed() {
        SampleRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Standardized message printer shortcut.
     */
    private void showModuleToast(String moduleName) {
        Toast.makeText(this, moduleName + " module integration coming soon!", Toast.LENGTH_SHORT).show();
    }
}