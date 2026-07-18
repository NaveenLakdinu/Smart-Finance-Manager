package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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

    private static final String TAG = "UpdateBillActivity";

    private ImageView backButton;
    private EditText editBillName, editAccountNo, editPaymentDate;
    private Spinner spinnerCategory;
    private Button btnUpdateSubmit;
    private TextView txtTitle;

    private FirebaseFirestore db;
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bill);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        setupDatePicker();

        // Fetch the target bill data using the passed ID
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
        txtTitle = findViewById(R.id.txtTitle);

        if (txtTitle != null) {
            txtTitle.setText("Update Bill"); // Changes header text to "Update Bill"
        }

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

    // FIX: Instead of looking for missing Intent extras, we query Firestore directly using the ID
    private void getIntentData() {
        if (getIntent().hasExtra("BILL_ID")) {
            docId = getIntent().getStringExtra("BILL_ID");

            // Pull the latest document snapshot data directly from the collection
            db.collection("utilityBill").document(docId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UtilityBill bill = documentSnapshot.toObject(UtilityBill.class);
                            if (bill != null) {
                                // Populate input fields safely
                                editBillName.setText(bill.getBillName());
                                editAccountNo.setText(bill.getAccountNo());
                                editPaymentDate.setText(bill.getPaymentDate());

                                // Set matching spinner item position
                                String category = bill.getCategory();
                                @SuppressWarnings("unchecked")
                                ArrayAdapter<String> myAdap = (ArrayAdapter<String>) spinnerCategory.getAdapter();
                                if (myAdap != null && category != null) {
                                    int spinnerPosition = myAdap.getPosition(category);
                                    spinnerCategory.setSelection(spinnerPosition);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching bill data", e);
                        Toast.makeText(this, "Failed to load bill details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Error: No Bill ID received", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupDatePicker() {
        editPaymentDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UpdateBillActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                        editPaymentDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnUpdateSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                saveUpdatesToFirebase();
            }
        });
    }

    private void saveUpdatesToFirebase() {
        if (docId == null) return;
        btnUpdateSubmit.setEnabled(false);

        String name = editBillName.getText().toString().trim();
        String accountNo = editAccountNo.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String dueDateStr = editPaymentDate.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("billName", name);
        updates.put("accountNo", accountNo);
        updates.put("category", category);
        updates.put("paymentDate", dueDateStr);

        db.collection("utilityBill").document(docId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bill Updated Successfully", Toast.LENGTH_SHORT).show();
                    scheduleBillNotification(name, dueDateStr);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnUpdateSubmit.setEnabled(true);
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
                calendar.add(Calendar.DAY_OF_YEAR, -1);
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