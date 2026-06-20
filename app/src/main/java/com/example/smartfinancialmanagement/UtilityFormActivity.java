package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfinancialmanagement.UtilityReportActivity;

public class UtilityFormActivity extends AppCompatActivity {

    private FrameLayout backButtonContainer;
    private Spinner spinnerChooseBill, spinnerChooseMonth;
    private EditText editTextBillAmount;
    private Button btnNextReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_utilityreport_form); // Your rewritten Form layout name

        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        backButtonContainer = findViewById(R.id.backButtonContainer);
        spinnerChooseBill = findViewById(R.id.ChooseBill);
        spinnerChooseMonth = findViewById(R.id.ChooseMonth);
        editTextBillAmount = findViewById(R.id.Bill);
        btnNextReport = findViewById(R.id.NextReport);
    }

    private void setupSpinners() {
        // Sample data for demonstration; adjust to match your string arrays
        String[] bills = {"House Rent", "Home Water Bill", "Home Electricity"};
        String[] months = {"January", "February", "March", "April", "May", "June"};

        ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bills);
        spinnerChooseBill.setAdapter(billAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        spinnerChooseMonth.setAdapter(monthAdapter);
    }

    private void setupClickListeners() {
        // Finish activity to go back to Dashboard
        if (backButtonContainer != null) {
            backButtonContainer.setOnClickListener(v -> finish());
        }

        // Navigate to the Report Summary screen
        btnNextReport.setOnClickListener(v -> {
            String amountStr = editTextBillAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(UtilityFormActivity.this, "Please enter a bill amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Launch the next screen (Report Panel)
            Intent intent = new Intent(UtilityFormActivity.this, UtilityReportActivity.class);
            // Optional: Pass data to the report screen if needed
            intent.putExtra("BILL_TYPE", spinnerChooseBill.getSelectedItem().toString());
            intent.putExtra("BILL_AMOUNT", amountStr);
            intent.putExtra("BILL_MONTH", spinnerChooseMonth.getSelectedItem().toString());
            startActivity(intent);
        });
    }
}