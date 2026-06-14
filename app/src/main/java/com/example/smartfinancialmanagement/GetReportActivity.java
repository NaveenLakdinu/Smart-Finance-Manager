package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GetReportActivity extends AppCompatActivity {

    private ImageView backButton;
    private Spinner spinnerChooseBill;
    private Spinner spinnerChooseMonth;
    private EditText editBillAmount;
    private Button btnNextReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_report_form);

        // 1. Initialize Views from XML
        initializeViews();

        // 2. Setup Dropdown Menus with static items
        setupDropdowns();

        // 3. Attach standard click listeners
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        spinnerChooseBill = findViewById(R.id.ChooseBill);
        spinnerChooseMonth = findViewById(R.id.ChooseMonth);
        editBillAmount = findViewById(R.id.Bill);
        btnNextReport = findViewById(R.id.NextReport);
    }

    private void setupDropdowns() {
        // "Choose Bill" Dropdown Options
        String[] billsList = {"Select a Bill", "House Rent", "Home Water Bill", "Home Electricity"};
        ArrayAdapter<String> billAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                billsList
        );
        billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChooseBill.setAdapter(billAdapter);

        // "Choose Month" Dropdown Options
        String[] monthsList = {
                "Select Month", "January", "February", "March", "April",
                "May", "June", "July", "August", "September",
                "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                monthsList
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChooseMonth.setAdapter(monthAdapter);
    }

    private void setupListeners() {
        // Closes this screen and returns to dashboard
        backButton.setOnClickListener(v -> finish());

        // Standard frontend validation before screen routing
        btnNextReport.setOnClickListener(v -> {
            int billPosition = spinnerChooseBill.getSelectedItemPosition();
            int monthPosition = spinnerChooseMonth.getSelectedItemPosition();
            String amount = editBillAmount != null ? editBillAmount.getText().toString().trim() : "";

            if (billPosition == 0) {
                Toast.makeText(this, "Please choose a bill category", Toast.LENGTH_SHORT).show();
            } else if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter a payment amount", Toast.LENGTH_SHORT).show();
            } else if (monthPosition == 0) {
                Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            } else {
                // If everything is locally valid, proceed to transition to selection screen
                Intent intent = new Intent(GetReportActivity.this, ReportListSelectionActivity.class);
                startActivity(intent);
            }
        });
    }
}