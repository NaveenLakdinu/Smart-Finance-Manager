package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SubscriptionManagerActivity extends AppCompatActivity {

    private View btnAddSubscription, btnViewSubscriptions, btnSubscriptionReport;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_manager_function);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnAddSubscription = findViewById(R.id.btnAddSubscription);
        btnViewSubscriptions = findViewById(R.id.btnViewSubscriptions);
        btnSubscriptionReport = findViewById(R.id.btnSubscriptionReport);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAddSubscription.setOnClickListener(v -> {
            // Reusing existing SubscriptionActivity for the add form
            Intent intent = new Intent(this, SubscriptionActivity.class);
            startActivity(intent);
        });

        btnViewSubscriptions.setOnClickListener(v -> 
            Toast.makeText(this, "View All Plans - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        btnSubscriptionReport.setOnClickListener(v -> 
            Toast.makeText(this, "Generating Subscription Report...", Toast.LENGTH_SHORT).show()
        );
    }
}