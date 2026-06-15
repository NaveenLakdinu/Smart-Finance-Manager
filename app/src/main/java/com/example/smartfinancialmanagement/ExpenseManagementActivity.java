package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ExpenseManagementActivity extends AppCompatActivity {

    private TextView btnBack;
    private Spinner spinnerExpenseCategory;
    private EditText edtExpenseAmount;
    private EditText edtExpenseDate;
    private EditText edtExpenseDescription;
    private Button btnAddExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management);

        // 1. Hook up the interactive layout components
        initializeViews();

        // 2. Setup standard static dropdown list items
        setupSpinner();

        // 3. Keep local OS UI components like the native date picker calendar
        setupDatePicker();

        // 4. Attach standard click listeners
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        btnAddExpense = findViewById(R.id.btnAddExpense);
    }

    private void setupSpinner() {
        String[] expenseCategories = {
                "Utility Bill",
                "Loan Payment",
                "Subscription",
                "Inventory Purchase",
                "Employee Salary",
                "Marketing",
                "Transport",
                "Equipment",
                "Office Rent",
                "Other Expense"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                expenseCategories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerExpenseCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        edtExpenseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ExpenseManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtExpenseDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Simple navigation handling back to dashboard
        btnBack.setOnClickListener(v -> finish());

        // Standard frontend validation logic before submissions
        btnAddExpense.setOnClickListener(v -> {
            String amountStr = edtExpenseAmount.getText().toString().trim();
            String date = edtExpenseDate.getText().toString().trim();

            if (amountStr.isEmpty()) {
                edtExpenseAmount.setError("Enter amount");
                edtExpenseAmount.requestFocus();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    edtExpenseAmount.setError("Amount must be greater than zero");
                    edtExpenseAmount.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                edtExpenseAmount.setError("Enter a valid numeric amount");
                edtExpenseAmount.requestFocus();
                return;
            }

            if (date.isEmpty()) {
                edtExpenseDate.setError("Select date");
                edtExpenseDate.requestFocus();
                return;
            }

            // Input is clean, clear the form fields for next entry
            clearFields();
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFields() {
        edtExpenseAmount.setText("");
        edtExpenseDate.setText("");
        edtExpenseDescription.setText("");
        spinnerExpenseCategory.setSelection(0);
    }
}