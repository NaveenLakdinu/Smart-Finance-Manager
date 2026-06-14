package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView btnBack;
    private Button btnGeneratePdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_analytic);

        // Initialize the interactive UI components
        initializeViews();

        // Setup simple click handlers
        setupListeners();
    }

    /**
     * Binds only the functional, interactive components from your XML layout.
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
    }

    /**
     * Configures the actions for your back action and PDF button.
     */
    private void setupListeners() {
        // Finishes current activity and takes user back to previous screen
        btnBack.setOnClickListener(v -> finish());

        // Simple placeholder interaction for the report generator
        btnGeneratePdf.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "PDF Report Generated",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}