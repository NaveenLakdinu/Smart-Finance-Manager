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

public class RevenueManagementActivity extends AppCompatActivity {

    // Header & Dashboard Total Readouts
    private TextView btnBack;
    private TextView txtTotalRevenue;
    private TextView txtMonthlyRevenue;

    // Interactive Entry Form Components
    private Spinner spinnerRevenueSource;
    private EditText edtRevenueAmount;
    private EditText edtRevenueDate;
    private EditText edtRevenueDescription;
    private Button btnAddRevenue;

    // Temporary Local State (matching layout placeholder defaults)
    private double totalRevenue = 250000.0;
    private double monthlyRevenue = 50000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management); // Double-check this matches your XML file name

        initializeViews();
        setupSourceSpinner();
        setupDatePicker();
        setupListeners();

        // Render initial view strings
        updateSummaryUI();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        txtMonthlyRevenue = findViewById(R.id.txtMonthlyRevenue);

        spinnerRevenueSource = findViewById(R.id.spinnerRevenueSource);
        edtRevenueAmount = findViewById(R.id.edtRevenueAmount);
        edtRevenueDate = findViewById(R.id.edtRevenueDate);
        edtRevenueDescription = findViewById(R.id.edtRevenueDescription);
        btnAddRevenue = findViewById(R.id.btnAddRevenue);
    }

    /**
     * Binds business source options to your dropdown Spinner element.
     */
    private void setupSourceSpinner() {
        String[] sources = {"Sales Income", "Service Fees", "B2B Invoices", "Investments", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRevenueSource.setAdapter(adapter);
    }

    /**
     * Spawns a calendar picker utility to populate dates cleanly.
     */
    private void setupDatePicker() {
        edtRevenueDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RevenueManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        edtRevenueDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Return without passing new variables
        btnBack.setOnClickListener(v -> finish());

        // Process additions and relay values through back channels
        btnAddRevenue.setOnClickListener(v -> {
            String amountInput = edtRevenueAmount.getText().toString().trim();

            if (amountInput.isEmpty()) {
                Toast.makeText(this, "Please enter a revenue amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            double newRevenueAmount = Double.parseDouble(amountInput);

            // Step up internal tracking data models
            totalRevenue += newRevenueAmount;
            monthlyRevenue += newRevenueAmount;

            updateSummaryUI();

            Toast.makeText(this, "Revenue added locally!", Toast.LENGTH_SHORT).show();

            // Clear the form fields cleanly
            edtRevenueAmount.setText("");
            edtRevenueDate.setText("");
            edtRevenueDescription.setText("");

            // Pack up intent extras and push them out to listeners
            Intent returnIntent = new Intent();
            returnIntent.putExtra("UPDATED_REVENUE", totalRevenue);
            setResult(RESULT_OK, returnIntent);

            // Optional: Uncomment to kick back to analytics dashboard instantly on click
            // finish();
        });
    }

    /**
     * Formats local data states to display text properties gracefully.
     */
    private void updateSummaryUI() {
        txtTotalRevenue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", totalRevenue));
        txtMonthlyRevenue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", monthlyRevenue));
    }
}