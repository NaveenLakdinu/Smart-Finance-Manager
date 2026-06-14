package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfitLossActivity extends AppCompatActivity {

    private TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profitloss);

        // 1. Initialize only interactive views
        initializeViews();

        // 2. Setup the navigation listener
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Closes this activity and returns safely to the dashboard
        btnBack.setOnClickListener(v -> finish());
    }
}