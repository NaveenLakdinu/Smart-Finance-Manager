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

public class InventoryManagementActivity extends AppCompatActivity {

    private TextView btnBack;
    private Spinner spinnerCategory;
    private EditText edtItemName;
    private EditText edtSupplier;
    private EditText edtQuantity;
    private EditText edtUnitPrice;
    private EditText edtPurchaseDate;
    private Button btnAddInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_management);

        // 1. Initialize Interactive UI Components
        initializeViews();

        // 2. Setup Category Dropdown
        setupSpinner();

        // 3. Attach System Date Picker to the Date Field
        setupDatePicker();

        // 4. Setup Form and Navigation Click Listeners
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        edtItemName = findViewById(R.id.edtItemName);
        edtSupplier = findViewById(R.id.edtSupplier);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtUnitPrice = findViewById(R.id.edtUnitPrice);
        edtPurchaseDate = findViewById(R.id.edtPurchaseDate);
        btnAddInventory = findViewById(R.id.btnAddInventory);
    }

    private void setupSpinner() {
        String[] inventoryCategories = {
                "Flowers",
                "Packaging",
                "Decoration Items",
                "Office Supplies",
                "Equipment",
                "Raw Materials",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                inventoryCategories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        edtPurchaseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InventoryManagementActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtPurchaseDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        // Simple back navigation handler
        btnBack.setOnClickListener(v -> finish());

        // Basic frontend input validation
        btnAddInventory.setOnClickListener(v -> {
            String itemName = edtItemName.getText().toString().trim();
            String quantity = edtQuantity.getText().toString().trim();
            String unitPrice = edtUnitPrice.getText().toString().trim();
            String purchaseDate = edtPurchaseDate.getText().toString().trim();

            if (itemName.isEmpty()) {
                edtItemName.setError("Enter item name");
                return;
            }

            if (quantity.isEmpty()) {
                edtQuantity.setError("Enter quantity");
                return;
            }

            if (unitPrice.isEmpty()) {
                edtUnitPrice.setError("Enter unit price");
                return;
            }

            if (purchaseDate.isEmpty()) {
                edtPurchaseDate.setError("Select date");
                return;
            }

            // Input fields are verified, reset input form for the next item entry
            clearFields();
        });
    }

    private void clearFields() {
        edtItemName.setText("");
        edtSupplier.setText("");
        edtQuantity.setText("");
        edtUnitPrice.setText("");
        edtPurchaseDate.setText("");
        spinnerCategory.setSelection(0);
    }
}