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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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

    private AutoCompleteTextView spinnerBusinessDropdown;
    private EditText etClientName, etClientBRN, etItemName, etQty, etPrice, etPaymentDueDate;
    private TextView txtSubtotal, txtGrandTotal;
    private ImageView btnBack;
    private MaterialButton btnGenerateInvoice;

    private FirebaseFirestore db;
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

        initializeViews();
        loadBusinessesFromFirestore();
        setupCalculationListeners();
        setupDatePicker();

        btnGenerateInvoice.setOnClickListener(v -> checkInputsAndPromptEmail());
        btnBack.setOnClickListener(v -> finish());
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
        db.collection("businesses")
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
                });
    }

    private void setupCalculationListeners() {
        TextWatcher calculationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateTotals(); }
            @Override public void afterTextChanged(Editable s) {}
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
        btnGenerateInvoice.setEnabled(false);

        // STEP A: Query the designated workspace document from the businesses collection to get its email address
        db.collection("businesses")
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

                    // Complete data assembly mapping tracking destinations
                    String client = etClientName.getText().toString().trim();
                    String brn = etClientBRN.getText().toString().trim();
                    String item = etItemName.getText().toString().trim();
                    int finalQty = Integer.parseInt(etQty.getText().toString().trim());
                    double finalPrice = Double.parseDouble(etPrice.getText().toString().trim());
                    String dueDate = etPaymentDueDate.getText().toString().trim();

                    InvoiceModel invoice = new InvoiceModel(
                            chosenBusinessName, client, brn, item, finalQty, finalPrice,
                            calculatedSubtotal, calculatedGrandTotal, dueDate, isEmailReminderEnabled, "pending"
                    );

                    // Map and save the tracked target workspace email field reference directly into the model setup
                    invoice.setBusinessEmail(targetBusinessEmail);

                    // STEP B: Save the completed Invoice data structure to cloud DB
                    db.collection("invoices")
                            .add(invoice)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(CreateInvoiceActivity.this, "Invoice generated and stored successfully!", Toast.LENGTH_SHORT).show();

                                // STEP C: Programmatically schedule notifications 1 day prior to the payment date
                                scheduleOneDayPriorNotification(client, dueDate);
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

    private void scheduleOneDayPriorNotification(String clientName, String dueDateString) {
        try {
            Date dueDate = dateFormat.parse(dueDateString);
            if (dueDate != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dueDate);
                // Subtract 1 day to push precisely 24 hours prior
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                // Set explicit preferred trigger hour execution time (e.g., 9:00 AM)
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                long triggerMillis = calendar.getTimeInMillis();

                // Only schedule if the calculated reminder time is still in the future
                if (triggerMillis > System.currentTimeMillis()) {
                    Intent intent = new Intent(this, InvoiceReminderReceiver.class);
                    intent.putExtra("clientName", clientName);
                    intent.putExtra("dueDate", dueDateString);

                    int uniqueRequestId = (int) System.currentTimeMillis();
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueRequestId,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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