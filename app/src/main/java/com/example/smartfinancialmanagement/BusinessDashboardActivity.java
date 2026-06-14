package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Profile Area Views
    private TextView txtNotificationBell;
    private View btnTopLogout;

    // Business Feature Cards
    private CardView cardRevenue;
    private CardView cardExpense;
    private CardView cardInventory;
    private CardView cardBudget;
    private CardView cardProfit;
    private CardView cardAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard); // Ensure this matches your XML file name

        // 1. Initialize Interactive Views
        initViews();

        // 2. Setup Click Listeners
        setupClickListeners();
    }

    /**
     * Finds and binds only the interactive views from the XML layout using their IDs.
     */
    private void initViews() {
        // Notification Icon
        txtNotificationBell = findViewById(R.id.txtNotificationBell);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        // Feature Grid Cards
        cardRevenue = findViewById(R.id.cardRevenue);
        cardExpense = findViewById(R.id.cardExpense);
        cardInventory = findViewById(R.id.cardInventory);
        cardBudget = findViewById(R.id.cardBudget);
        cardProfit = findViewById(R.id.cardProfit);
        cardAnalytics = findViewById(R.id.cardAnalytics);
    }

    /**
     * Configures the click actions for all interactive elements.
     */
    private void setupClickListeners() {
        // Notification Icon Click
        txtNotificationBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Notifications clicked");
            }
        });

        btnTopLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(BusinessDashboardActivity.this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Feature Card Clicks
        cardRevenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this,RevenueManagementActivity.class);
                startActivity(intent);
            }
        });

        cardExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this, ExpenseManagementActivity.class);
                startActivity(intent);
            }
        });

        cardInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this,InventoryManagementActivity.class);
                startActivity(intent);
            }
        });

        cardBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this,BudgetPlannerActivity.class);
                startActivity(intent);
            }
        });

        cardProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this, ProfitLossActivity.class);
                startActivity(intent);
            }
        });

        cardAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BusinessDashboardActivity.this,AnalyticsActivity.class);
                startActivity(intent);
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
