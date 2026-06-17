package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterBillActivity extends AppCompatActivity {

    // Declare layout elements
    private ImageView backButton;
    private EditText editBillName, editAmount, editPaymentDate;
    private Spinner spinnerCategory, spinnerStatus;
    private Button btnRegisterSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bill);

        initViews();
        setupSpinners();
        setupDatePicker();
        setupClickListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        editBillName = findViewById(R.id.editBillName);
        editAmount = findViewById(R.id.editAmount);
        editPaymentDate = findViewById(R.id.editPaymentDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
    }

    private void setupSpinners() {
        // Category Spinner
        List<String> categories = new ArrayList<>();
        categories.add("Select Category");
        categories.add("Electricity");
        categories.add("Water");
        categories.add("Telephone");
        categories.add("Internet");
        categories.add("Television");
        categories.add("Rent");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Status Spinner
        List<String> statuses = new ArrayList<>();
        statuses.add("Select Status");
        statuses.add("Paid");
        statuses.add("Pending");
        statuses.add("Due");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupDatePicker() {
        editPaymentDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterBillActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editPaymentDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnRegisterSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                Toast.makeText(this, "Bill Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean validateForm() {
        if (editBillName.getText().toString().trim().isEmpty()) {
            editBillName.setError("Enter bill name");
            return false;
        }
        if (editAmount.getText().toString().trim().isEmpty()) {
            editAmount.setError("Enter amount");
            return false;
        }
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editPaymentDate.getText().toString().trim().isEmpty()) {
            editPaymentDate.setError("Select date");
            return false;
        }
        if (spinnerStatus.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select a status", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}