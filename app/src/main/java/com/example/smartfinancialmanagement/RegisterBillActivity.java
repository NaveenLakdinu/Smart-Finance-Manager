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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterBillActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText editBillName, editAmount, editPaymentDate;
    private Spinner spinnerCategory, spinnerStatus;
    private Button btnRegisterSubmit;

    private FirebaseFirestore db;
    private String uid;
    private double currentSavings = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bill);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        initViews();
        setupSpinners();
        setupDatePicker();
        setupClickListeners();
        fetchCurrentSavings();
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

    private void fetchCurrentSavings() {
        if (uid == null) return;
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String savingsStr = documentSnapshot.getString("currentSavings");
                        if (savingsStr != null) {
                            try {
                                currentSavings = Double.parseDouble(savingsStr.trim());
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                });
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
                double amount = Double.parseDouble(editAmount.getText().toString().trim());
                if (amount > currentSavings) {
                    showSavingsWarningDialog(amount);
                } else {
                    saveBillToFirestore(amount);
                }
            }
        });
    }

    private void showSavingsWarningDialog(double amount) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Insufficient Savings")
                .setMessage(String.format("The bill amount (LKR %.2f) exceeds your current savings (LKR %.2f). Would you like to register it anyway?", amount, currentSavings))
                .setPositiveButton("Register Anyway", (dialog, which) -> saveBillToFirestore(amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveBillToFirestore(double amount) {
        if (uid == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> bill = new HashMap<>();
        bill.put("billName", editBillName.getText().toString().trim());
        bill.put("amount", amount);
        bill.put("category", spinnerCategory.getSelectedItem().toString());
        bill.put("paymentDate", editPaymentDate.getText().toString().trim());
        bill.put("status", spinnerStatus.getSelectedItem().toString());
        bill.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).collection("utilities")
                .add(bill)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterBillActivity.this, "Bill Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterBillActivity.this, "Error saving bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        try {
            double amt = Double.parseDouble(editAmount.getText().toString().trim());
            if (amt <= 0) {
                editAmount.setError("Amount must be greater than zero");
                return false;
            }
        } catch (NumberFormatException e) {
            editAmount.setError("Enter a valid amount");
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