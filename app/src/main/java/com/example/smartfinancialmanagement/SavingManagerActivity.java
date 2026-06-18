package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SavingManagerActivity extends AppCompatActivity {

    private View btnAddGoal, btnViewGoals, btnSavingReport;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_manager_function);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnViewGoals = findViewById(R.id.btnViewGoals);
        btnSavingReport = findViewById(R.id.btnSavingReport);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAddGoal.setOnClickListener(v -> {
            // Reusing existing SavingPlanActivity for the add form
            Intent intent = new Intent(this, SavingPlanActivity.class);
            startActivity(intent);
        });

        btnViewGoals.setOnClickListener(v -> 
            Toast.makeText(this, "View All Goals - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        btnSavingReport.setOnClickListener(v -> 
            Toast.makeText(this, "Generating Saving Report...", Toast.LENGTH_SHORT).show()
        );
    }
}