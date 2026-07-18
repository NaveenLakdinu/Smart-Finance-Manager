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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterBillActivity extends AppCompatActivity {

    // UI elements declaration
    private ImageView backButton;
    private EditText editBillName, editAccountNo, editPaymentDate;
    private Spinner spinnerCategory;
    private Button btnRegisterSubmit;
    private TextView txtTitle;

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
        editAccountNo = findViewById(R.id.editAccountNo);
        editPaymentDate = findViewById(R.id.editPaymentDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        txtTitle = findViewById(R.id.txtTitle);

        if (txtTitle != null) {
            txtTitle.setText("Register Bill");
        }
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
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        editPaymentDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    // Listens for clicks on actionable elements (Back arrow & Save button)
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

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
        String accountNo = editAccountNo.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String dueDateStr = editPaymentDate.getText().toString().trim();

        // Get the currently logged-in user's unique ID
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX: Matches the 8-argument constructor in UtilityBill exactly
        UtilityBill newBill = new UtilityBill(
                name,
                accountNo,
                0.0,                      // Default amount until paid/updated
                category,
                dueDateStr,
                "Pending",                // Initial status
                System.currentTimeMillis(),// Creation timestamp
                currentUserId
        );

        // Temporarily freeze button to prevent double-submit network spamming
        btnRegisterSubmit.setEnabled(false);

        // Uploads the mapped object to your collection
        db.collection("utilityBill")
                .add(newBill)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterBillActivity.this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous dashboard screen
                })
                .addOnFailureListener(e -> {
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
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editPaymentDate.getText().toString().trim().isEmpty()) {
            editPaymentDate.setError("Select date");
            return false;
        }
        return true;
    }
}