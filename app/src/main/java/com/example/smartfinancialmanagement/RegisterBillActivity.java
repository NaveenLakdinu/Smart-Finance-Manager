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

// Firebase Firestore library to communicate with your cloud database
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterBillActivity extends AppCompatActivity {

    // UI elements declaration
    private ImageView backButton;
    private EditText editBillName, editAccountNo, editPaymentDate; // Changed editAmount to editAccountNo
    private Spinner spinnerCategory;
    private Button btnRegisterSubmit;

    // Database instance variable
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bill);

        // Initialize Cloud Firestore when the activity starts
        db = FirebaseFirestore.getInstance();

        // UI and event setup helper methods
        initViews();
        setupSpinners();
        setupDatePicker();
        setupClickListeners();
    }

    // Link Java objects to your XML layout elements via IDs
    private void initViews() {
        backButton = findViewById(R.id.backButton);
        editBillName = findViewById(R.id.editBillName);
        editAccountNo = findViewById(R.id.editAccountNo); // Linked to the new XML ID
        editPaymentDate = findViewById(R.id.editPaymentDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
    }

    // Populates the drop-down menu with your defined bill categories
    private void setupSpinners() {
        List<String> categories = new ArrayList<>();
        categories.add("Select Category"); // Hint item at position 0
        categories.add("Electricity");
        categories.add("Water");
        categories.add("Telephone");
        categories.add("Internet");
        categories.add("Television");
        categories.add("Rent");
        categories.add("Other");

        // Bind the list array data to the default Android spinner layout style
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    // Displays a popup calendar view when clicking the Due Date input field
    private void setupDatePicker() {
        editPaymentDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterBillActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Formats day and month with a leading zero if below 10 (e.g., 05/09/2026)
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        editPaymentDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    // Listens for clicks on actionable elements (Back arrow & Save button)
    private void setupClickListeners() {
        // Closes this screen and returns to the previous dashboard activity
        backButton.setOnClickListener(v -> finish());

        // Submits the data to the cloud only if user inputs clear standard error validation
        btnRegisterSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                saveBillToFirebase();
            }
        });
    }

    // Packages field variables and pushes them over the network to Firestore
    private void saveBillToFirebase() {
        // Extract plain values from UI fields
        String name = editBillName.getText().toString().trim();
        String accountNo = editAccountNo.getText().toString().trim(); // Stored as String to protect leading zeros
        String category = spinnerCategory.getSelectedItem().toString();
        String dueDateStr = editPaymentDate.getText().toString().trim();

        // Create an instance of our nested BillModel, forcing the status to start as "Pending"
        BillModel newBill = new BillModel(name, accountNo, category, dueDateStr);

        // Temporarily freeze button to prevent double-submit network spamming
        btnRegisterSubmit.setEnabled(false);

        // Uploads the mapped object to a collection named "bills"
        db.collection("bills")
                .add(newBill)
                .addOnSuccessListener(documentReference -> {
                    // Triggers if write is successful
                    Toast.makeText(RegisterBillActivity.this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous dashboard screen
                })
                .addOnFailureListener(e -> {
                    // Triggers if there's a connection/permission failure
                    btnRegisterSubmit.setEnabled(true); // Unfreeze button so the user can retry
                    Toast.makeText(RegisterBillActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Form client-side sanity check to ensure no crucial inputs are submitted empty
    private boolean validateForm() {
        if (editBillName.getText().toString().trim().isEmpty()) {
            editBillName.setError("Enter bill name");
            return false;
        }
        if (editAccountNo.getText().toString().trim().isEmpty()) {
            editAccountNo.setError("Enter account number");
            return false;
        }
        // Validates that user didn't leave the dropdown selection on "Select Category" (position 0)
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editPaymentDate.getText().toString().trim().isEmpty()) {
            editPaymentDate.setError("Select date");
            return false;
        }
        return true; // Form is clean and safe to upload
    }

    // =========================================================================
    // NESTED STATIC MODEL CLASS FOR FIREBASE
    // This replaces a standalone Bill.java file completely.
    // =========================================================================
    public static class BillModel {
        private String name;
        private String accountNo; // Changed from 'double amount' to 'String accountNo'
        private String category;
        private String dueDate;


        // Mandated empty constructor. Firebase requires this to map incoming structural data values.
        public BillModel() {}

        // Initialization constructor used when compiling the document model before uploading
        public BillModel(String name, String accountNo, String category, String dueDate) {
            this.name = name;
            this.accountNo = accountNo;
            this.category = category;
            this.dueDate = dueDate;

        }

        // Getters & Setters are required by Firestore reflection to parse object fields into document properties
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAccountNo() { return accountNo; }
        public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    }
}