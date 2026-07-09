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
    private View cardActiveGoal;
    private View txtSeeAllActiveGoals;

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
        cardActiveGoal = findViewById(R.id.cardActiveGoal);
        txtSeeAllActiveGoals = findViewById(R.id.txtSeeAllActiveGoals);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingAddGoalActivity.class);
            startActivity(intent);
        });

        btnViewGoals.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingListActivity.class);
            startActivity(intent);
        });

        btnSavingReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingGenerateReportActivity.class);
            startActivity(intent);
        });

        cardActiveGoal.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingDetailsActivity.class);
            startActivity(intent);
        });

        txtSeeAllActiveGoals.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingListActivity.class);
            startActivity(intent);
        });
    }
}