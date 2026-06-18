package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseManagementActivity extends AppCompatActivity {

    // Top Header & Summary Display Elements
    private TextView btnBack;
    private TextView txtTotalExpenses;
    private TextView txtMonthlyExpenses;

    // Form Input Elements
    private Spinner spinnerExpenseCategory;
    private EditText edtExpenseAmount;
    private EditText edtExpenseDate;
    private EditText edtExpenseDescription;
    private Button btnAddExpense;

    // Local Variables tracking current display numbers
    private double totalExpenses = 170000.0; // Default matching your layout hint
    private double monthlyExpenses = 35000.0; // Default matching your layout hint

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management); // Ensure this matches your layout XML file name

        initializeViews();
        setupCategorySpinner();
        setupDatePicker();
        setupListeners();

        // Display initial dummy states nicely formatted
        updateSummaryUI();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses);
        txtMonthlyExpenses = findViewById(R.id.txtMonthlyExpenses);

        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        edtExpenseAmount = findViewById(R.id.edtExpenseAmount);
        edtExpenseDate = findViewById(R.id.edtExpenseDate);
        edtExpenseDescription = findViewById(R.id.edtExpenseDescription);
        btnAddExpense = findViewById(R.id.btnAddExpense);
    }

    /**
     * Fills the spinner dropdown menu with business expense category choices.
     */
    private void setupCategorySpinner() {
        String[] categories = {"Utilities", "Rent/Lease", "Inventory/Stock", "Salaries", "Marketing", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseCategory.setAdapter(adapter);
    }

    /**
     * Displays a native Android calendar calendar modal whenever the date field is clicked.
     */
    private void setupDatePicker() {
        edtExpenseDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ExpenseManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        edtExpenseDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Safe immediate back routing out of activity
        btnBack.setOnClickListener(v -> finish());

        // Process additions and relay new values back to the Analytics Activity channel
        btnAddExpense.setOnClickListener(v -> {
            String amountInput = edtExpenseAmount.getText().toString().trim();

            if (amountInput.isEmpty()) {
                Toast.makeText(this, "Please enter an amount first!", Toast.LENGTH_SHORT).show();
                return;
            }

            double newExpenseAmount = Double.parseDouble(amountInput);

            // Increment local fields temporarily
            totalExpenses += newExpenseAmount;
            monthlyExpenses += newExpenseAmount;

            updateSummaryUI();

            Toast.makeText(this, "Expense tracked locally!", Toast.LENGTH_SHORT).show();

            // Clear the form entry block cleanly for subsequent inputs
            edtExpenseAmount.setText("");
            edtExpenseDate.setText("");
            edtExpenseDescription.setText("");

            // Bundle up updated state calculations to send back backwards through the contract channel
            Intent returnIntent = new Intent();
            returnIntent.putExtra("UPDATED_EXPENSE", totalExpenses);
            setResult(RESULT_OK, returnIntent);

            // Optional: If you want to automatically take them back to analytics screen immediately, uncomment next line:
            // finish();
        });
    }

    /**
     * Refreshes local view layout displays with currency formatting structures.
     */
    private void updateSummaryUI() {
        txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %,.0f", totalExpenses));
        txtMonthlyExpenses.setText(String.format(Locale.getDefault(), "Rs. %,.0f", monthlyExpenses));
    }
}