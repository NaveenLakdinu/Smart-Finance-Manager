package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class RevenueManagementActivity extends AppCompatActivity {

    private TextView btnBack;
    private Spinner spinnerRevenueSource;
    private EditText edtRevenueAmount;
    private EditText edtRevenueDate;
    private EditText edtRevenueDescription;
    private Button btnAddRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management);

        // 1. Initialize interactive UI components
        initializeViews();

        // 2. Setup the fixed revenue sources dropdown list
        setupSpinner();

        // 3. Attach system calendar dialog to the date field
        setupDatePicker();

        // 4. Set click handling for navigation and input validation
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerRevenueSource = findViewById(R.id.spinnerRevenueSource);
        edtRevenueAmount = findViewById(R.id.edtRevenueAmount);
        edtRevenueDate = findViewById(R.id.edtRevenueDate);
        edtRevenueDescription = findViewById(R.id.edtRevenueDescription);
        btnAddRevenue = findViewById(R.id.btnAddRevenue);
    }

    private void setupSpinner() {
        String[] revenueSources = {
                "Product Sales",
                "Online Orders",
                "Service Income",
                "Delivery Charges",
                "Other Income"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                revenueSources
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerRevenueSource.setAdapter(adapter);
    }

    private void setupDatePicker() {
        edtRevenueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RevenueManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtRevenueDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Simple back button click to pop the stack
        btnBack.setOnClickListener(v -> finish());

        // Basic form error checker before saving data structures
        btnAddRevenue.setOnClickListener(v -> {
            String amount = edtRevenueAmount.getText().toString().trim();
            String date = edtRevenueDate.getText().toString().trim();

            if (amount.isEmpty()) {
                edtRevenueAmount.setError("Enter amount");
                return;
            }

            if (date.isEmpty()) {
                edtRevenueDate.setError("Select date");
                return;
            }

            // Input fields are cleanly checked, clear the screen inputs for next run
            clearFields();
        });
    }

    private void clearFields() {
        edtRevenueAmount.setText("");
        edtRevenueDate.setText("");
        edtRevenueDescription.setText("");
        spinnerRevenueSource.setSelection(0);
    }
}