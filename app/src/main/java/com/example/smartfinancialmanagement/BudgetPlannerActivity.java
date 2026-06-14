package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetPlannerActivity extends AppCompatActivity {

    private TextView btnBack;
    private Spinner spinnerBudgetCategory;
    private EditText edtMonth;
    private EditText edtBudgetAmount;
    private Button btnSaveBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_planner);

        // 1. Hook up the interactive XML views
        initializeViews();

        // 2. Populate the category dropdown list
        setupSpinner();

        // 3. Attach standard click listeners
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerBudgetCategory = findViewById(R.id.spinnerBudgetCategory);
        edtMonth = findViewById(R.id.edtMonth);
        edtBudgetAmount = findViewById(R.id.edtBudgetAmount);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
    }

    private void setupSpinner() {
        String[] budgetCategories = {
                "Utilities",
                "Loan Payments",
                "Subscriptions",
                "Inventory",
                "Employee Salaries",
                "Marketing",
                "Transport",
                "Office Rent",
                "Equipment",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                budgetCategories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerBudgetCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Go back to the previous dashboard screen
        btnBack.setOnClickListener(v -> finish());

        // Handle frontend form validation
        btnSaveBudget.setOnClickListener(v -> {
            String month = edtMonth.getText().toString().trim();
            String amount = edtBudgetAmount.getText().toString().trim();

            if (month.isEmpty()) {
                edtMonth.setError("Enter month");
                return;
            }

            if (amount.isEmpty()) {
                edtBudgetAmount.setError("Enter budget amount");
                return;
            }

            // Form inputs are clean, wipe the inputs for the next entry
            clearFields();
        });
    }

    private void clearFields() {
        edtMonth.setText("");
        edtBudgetAmount.setText("");
        spinnerBudgetCategory.setSelection(0);
    }
}