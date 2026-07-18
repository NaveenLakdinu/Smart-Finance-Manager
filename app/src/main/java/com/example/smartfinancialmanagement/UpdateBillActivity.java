package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateBillActivity extends AppCompatActivity {

    // View component declarations
    private ImageView backButton;
    private EditText editBillName, editAccountNo, editPaymentDate;
    private Spinner spinnerCategory;
    private Button btnUpdateSubmit;

    // Database object and Document tracking key
    private FirebaseFirestore db;
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reuse the registration layout XML file directly
        setContentView(R.layout.activity_register_bill);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        setupDatePicker();

        // Extract data passed forward from the list card click target
        getIntentData();

        setupClickListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        editBillName = findViewById(R.id.editBillName);
        editAccountNo = findViewById(R.id.editAccountNo);
        editPaymentDate = findViewById(R.id.editPaymentDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnUpdateSubmit = findViewById(R.id.btnRegisterSubmit);

        // Dynamically override layout defaults to display an Update UI context
        btnUpdateSubmit.setText("Update Bill");
    }

    private void setupSpinners() {
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
    }

    private void getIntentData() {
        if (getIntent().hasExtra("BILL_ID")) {
            docId = getIntent().getStringExtra("BILL_ID");
            editBillName.setText(getIntent().getStringExtra("BILL_NAME"));
            editAccountNo.setText(getIntent().getStringExtra("BILL_ACC"));
            editPaymentDate.setText(getIntent().getStringExtra("BILL_DATE"));

            // Auto-select the corresponding spinner dropdown element matching passed data
            String category = getIntent().getStringExtra("BILL_CAT");

            // FIX: Cast explicitly to ArrayAdapter<String> instead of ArrayAdapter<?>
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> myAdap = (ArrayAdapter<String>) spinnerCategory.getAdapter();

            if (myAdap != null) {
                int spinnerPosition = myAdap.getPosition(category);
                spinnerCategory.setSelection(spinnerPosition);
            }
        }
    }
    private void setupDatePicker() {
        editPaymentDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UpdateBillActivity.this, R.style.Theme_SmartFinance_DatePicker,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        editPaymentDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        // Listens for execution command triggers
        btnUpdateSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                saveUpdatesToFirebase();
            }
        });
    }

    // Cleaned up core upload logic block mapping clean updates to the Firestore Document
    private void saveUpdatesToFirebase() {
        btnUpdateSubmit.setEnabled(false); // Lock interface interaction thread

        // Capture data safely from form inputs
        String name = editBillName.getText().toString().trim();
        String accountNo = editAccountNo.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String dueDateStr = editPaymentDate.getText().toString().trim();

        // Map key names precisely matching your fields on Cloud Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("accountNo", accountNo);
        updates.put("category", category);
        updates.put("dueDate", dueDateStr);

        // Target document update deployment line
        db.collection("bills").document(docId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bill Updated Successfully", Toast.LENGTH_SHORT).show();

                    // Trigger monthly alert reassignment routine using newly formatted parameters
                    scheduleBillNotification(name, dueDateStr);

                    finish(); // Fall back to dashboard display tree
                })
                .addOnFailureListener(e -> {
                    btnUpdateSubmit.setEnabled(true); // Release UI block on thread exception
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void scheduleBillNotification(String billName, String dueDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dueDateStr);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Alert 3 days before payment due configuration target parameter
                calendar.add(Calendar.DAY_OF_YEAR, -3);
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("BILL_NAME", billName);

                int uniqueIntentId = billName.hashCode();

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, uniqueIntentId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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