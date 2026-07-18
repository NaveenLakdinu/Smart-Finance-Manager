package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateInvoiceActivity extends AppCompatActivity {

    private static final String TAG = "CreateInvoiceActivity";
    private AutoCompleteTextView spinnerBusinessDropdown;
    private EditText etClientName, etClientBRN, etItemName, etQty, etPrice, etPaymentDueDate;
    private TextView txtSubtotal, txtGrandTotal;
    private ImageView btnBack;
    private MaterialButton btnGenerateInvoice;

    private FirebaseFirestore db;
    private String currentUserId;
    private List<String> businessNamesList = new ArrayList<>();
    private String chosenBusinessName = "";

    private double calculatedSubtotal = 0.00;
    private double calculatedGrandTotal = 0.00;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            Toast.makeText(this, "User session not active", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadBusinessesFromFirestore();
        setupCalculationListeners();
        setupDatePicker();

        btnGenerateInvoice.setOnClickListener(v -> checkInputsAndPromptEmail());
        btnBack.setOnClickListener(v -> finish());

        // Android 13 හෝ ඊට ඉහළ නම් පරිශීලකයාගෙන් Notification අවසර ඉල්ලීම
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerBusinessDropdown = findViewById(R.id.spinnerBusinessDropdown);
        etClientName = findViewById(R.id.etClientName);
        etClientBRN = findViewById(R.id.etClientBRN);
        etItemName = findViewById(R.id.etItemName);
        etQty = findViewById(R.id.etQty);
        etPrice = findViewById(R.id.etPrice);
        etPaymentDueDate = findViewById(R.id.etPaymentDueDate);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);
        btnGenerateInvoice = findViewById(R.id.btnGenerateInvoice);
    }

    private void setupDatePicker() {
        etPaymentDueDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateFormatted = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);
                        etPaymentDueDate.setText(dateFormatted);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void loadBusinessesFromFirestore() {
        if (currentUserId == null) return;

        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    businessNamesList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        BusinessModel business = doc.toObject(BusinessModel.class);
                        if (business != null && business.getBusinessName() != null) {
                            businessNamesList.add(business.getBusinessName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, businessNamesList);
                    spinnerBusinessDropdown.setAdapter(adapter);
                    spinnerBusinessDropdown.setOnItemClickListener((parent, view, position, id) ->
                            chosenBusinessName = parent.getItemAtPosition(position).toString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching businesses: " + e.getMessage()));
    }

    private void setupCalculationListeners() {
        TextWatcher calculationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotals();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        etQty.addTextChangedListener(calculationWatcher);
        etPrice.addTextChangedListener(calculationWatcher);
    }

    private void calculateTotals() {
        String qtyString = etQty.getText().toString().trim();
        String priceString = etPrice.getText().toString().trim();
        calculatedSubtotal = 0.00;
        calculatedGrandTotal = 0.00;

        if (!qtyString.isEmpty() && !priceString.isEmpty()) {
            try {
                int quantity = Integer.parseInt(qtyString);
                double unitPrice = Double.parseDouble(priceString);
                calculatedSubtotal = quantity * unitPrice;
                calculatedGrandTotal = calculatedSubtotal;
            } catch (NumberFormatException ignored) {}
        }
        txtSubtotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", calculatedSubtotal));
        txtGrandTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", calculatedGrandTotal));
    }

    private void checkInputsAndPromptEmail() {
        String client = etClientName.getText().toString().trim();
        String item = etItemName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String dueDate = etPaymentDueDate.getText().toString().trim();

        if (chosenBusinessName.isEmpty()) {
            Toast.makeText(this, "Please select a Business workspace", Toast.LENGTH_SHORT).show();
            return;
        }
        if (client.isEmpty() || item.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, "Please fulfill all mandatory information inputs", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Invoice Email Reminder")
                .setMessage("Would you like to send an automated copy of this invoice reminder to your business email address as well?")
                .setPositiveButton("Yes, Send Email", (dialog, which) -> saveInvoiceToDatabase(true))
                .setNegativeButton("No, Thanks", (dialog, which) -> saveInvoiceToDatabase(false))
                .setCancelable(false)
                .show();
    }

    private void saveInvoiceToDatabase(boolean isEmailReminderEnabled) {
        if (currentUserId == null) return;
        btnGenerateInvoice.setEnabled(false);

        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("businessName", chosenBusinessName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String targetBusinessEmail = "";
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        BusinessModel bizProfile = doc.toObject(BusinessModel.class);
                        if (bizProfile != null && bizProfile.getBusinessEmail() != null) {
                            targetBusinessEmail = bizProfile.getBusinessEmail();
                        }
                    }

                    String client = etClientName.getText().toString().trim();
                    String brn = etClientBRN.getText().toString().trim();
                    String item = etItemName.getText().toString().trim();
                    int finalQty = Integer.parseInt(etQty.getText().toString().trim());
                    double finalPrice = Double.parseDouble(etPrice.getText().toString().trim());
                    String dueDate = etPaymentDueDate.getText().toString().trim();

                    // 💡 FIXED: currentUserId passed directly here to match the 12-argument constructor signature perfectly
                    InvoiceModel invoice = new InvoiceModel(
                            chosenBusinessName, client, brn, item, finalQty, finalPrice,
                            calculatedSubtotal, calculatedGrandTotal, dueDate, isEmailReminderEnabled, "pending", currentUserId
                    );

                    invoice.setBusinessEmail(targetBusinessEmail);

                    db.collection("invoices")
                            .add(invoice)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(CreateInvoiceActivity.this, "Invoice generated and stored successfully!", Toast.LENGTH_SHORT).show();
                                scheduleOneDayPriorNotification(client, dueDate, isEmailReminderEnabled);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnGenerateInvoice.setEnabled(true);
                                Toast.makeText(CreateInvoiceActivity.this, "Storage Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnGenerateInvoice.setEnabled(true);
                    Toast.makeText(this, "Failed to resolve business settings profile context info", Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleOneDayPriorNotification(String clientName, String dueDateString, boolean isEmailEnabled) {
        try {
            Date dueDate = dateFormat.parse(dueDateString);
            if (dueDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dueDate);

                calendar.add(Calendar.DAY_OF_YEAR, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                long triggerMillis = calendar.getTimeInMillis();

                if (triggerMillis > System.currentTimeMillis()) {
                    Intent intent = new Intent(this, InvoiceReminderReceiver.class);
                    intent.putExtra("clientName", clientName);
                    intent.putExtra("dueDate", dueDateString);
                    intent.putExtra("isEmailReminderEnabled", isEmailEnabled);

                    int uniqueRequestId = Math.abs((clientName + dueDateString).hashCode());

                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueRequestId, intent, flags);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                try {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                                } catch (SecurityException e) {
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                                }
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                        }
                    }
                }
            }
        } catch (ParseException ignored) {}
    }
}