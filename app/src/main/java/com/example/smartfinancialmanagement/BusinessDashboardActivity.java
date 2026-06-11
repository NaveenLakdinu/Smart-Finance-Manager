package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Profile Area Views
    private TextView txtNotificationBell;

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

        // Feature Card Clicks
        cardRevenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Revenue Management clicked");
            }
        });

        cardExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Expenses Tracker clicked");
            }
        });

        cardInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Inventory Management clicked");
            }
        });

        cardBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Budget Planner clicked");
            }
        });

        cardProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Profit and Loss clicked");
            }
        });

        cardAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Analytics clicked");
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
