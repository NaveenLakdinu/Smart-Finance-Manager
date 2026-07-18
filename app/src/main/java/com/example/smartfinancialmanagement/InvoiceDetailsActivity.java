package com.example.smartfinancialmanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailsActivity extends AppCompatActivity {

    private static final String TAG = "InvoiceDetailsActivity";
    private TextView txtInvoiceStatusBadge, txtDetInvoiceNum, txtDetClientName, txtDetClientBRN;
    private TextView txtDetItemName, txtDetQty, txtDetPrice, txtDetTotal;
    private ImageView btnBack;
    private MaterialButton btnMarkPaid, btnDeleteInvoice;

    private FirebaseFirestore db;
    private String currentUserId; // 💡 Added for isolated data scope tracking
    private String clientName, selectedBusiness, status, dueDate;
    private double grandTotal;

    private String invoiceDocId = "";
    private TextView txtDetBusinessWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 💡 Initialize and secure user session check context
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            Toast.makeText(this, "User session not active", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        getIntentData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.bBack);
        txtInvoiceStatusBadge = findViewById(R.id.txtInvoiceStatusBadge);
        txtDetInvoiceNum = findViewById(R.id.txtDetInvoiceNum);
        txtDetClientName = findViewById(R.id.txtDetClientName);
        txtDetClientBRN = findViewById(R.id.txtDetClientBRN);
        txtDetItemName = findViewById(R.id.txtDetItemName);
        txtDetQty = findViewById(R.id.txtDetQty);
        txtDetPrice = findViewById(R.id.txtDetPrice);
        txtDetTotal = findViewById(R.id.txtDetTotal);
        btnMarkPaid = findViewById(R.id.btnMarkPaid);
        btnDeleteInvoice = findViewById(R.id.btnSendReminder);
        txtDetBusinessWorkspace = findViewById(R.id.txtDetBusinessWorkspace);
    }

    private void getIntentData() {
        if (getIntent() != null) {
            clientName = getIntent().getStringExtra("clientName");
            selectedBusiness = getIntent().getStringExtra("selectedBusiness");
            status = getIntent().getStringExtra("status");
            dueDate = getIntent().getStringExtra("paymentDueDate");
            grandTotal = getIntent().getDoubleExtra("grandTotal", 0.0);

            if (status == null) status = "pending";

            txtDetClientName.setText(clientName);
            txtDetClientBRN.setText(getIntent().getStringExtra("clientBRN"));
            txtDetItemName.setText(getIntent().getStringExtra("itemName"));
            txtDetQty.setText(String.valueOf(getIntent().getIntExtra("quantity", 1)));

            double unitPrice = getIntent().getDoubleExtra("unitPrice", 0.0);
            txtDetPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", unitPrice));
            txtDetTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));

            txtDetInvoiceNum.setText("#INV-" + Math.abs(clientName.hashCode() % 10000));

            if (selectedBusiness != null && !selectedBusiness.isEmpty()) {
                txtDetBusinessWorkspace.setText("Workspace: " + selectedBusiness);
            } else {
                txtDetBusinessWorkspace.setText("Business Commercial Statement");
            }

            configureStatusUI();
        }
    }

    private void configureStatusUI() {
        txtInvoiceStatusBadge.setText(status.toUpperCase());

        if (status.equalsIgnoreCase("paid")) {
            txtInvoiceStatusBadge.setTextColor(Color.parseColor("#071A33"));
            txtInvoiceStatusBadge.setBackgroundColor(Color.parseColor("#4ADE80"));

            btnMarkPaid.setText("MARK AS UNPAID");
            btnMarkPaid.setBackgroundColor(Color.parseColor("#FF5555"));
            btnMarkPaid.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            if (status.equalsIgnoreCase("due")) {
                txtInvoiceStatusBadge.setTextColor(Color.parseColor("#FFFFFF"));
                txtInvoiceStatusBadge.setBackgroundColor(Color.parseColor("#FF5555"));
            } else {
                txtInvoiceStatusBadge.setTextColor(Color.parseColor("#FFB800"));
                txtInvoiceStatusBadge.setBackgroundColor(Color.parseColor("#1A3050"));
            }

            btnMarkPaid.setText("MARK AS PAID & RECONCILE");
            btnMarkPaid.setBackgroundColor(Color.parseColor("#4ADE80"));
            btnMarkPaid.setTextColor(Color.parseColor("#071A33"));
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnMarkPaid.setOnClickListener(v -> toggleInvoicePaidStatus());
        btnDeleteInvoice.setOnClickListener(v -> deleteInvoiceRecord());
    }

    private void toggleInvoicePaidStatus() {
        if (currentUserId == null) return;

        String newStatus;
        if (status.equalsIgnoreCase("paid")) {
            newStatus = determinePendingOrOverdue(dueDate);
        } else {
            newStatus = "paid";
        }

        // 💡 Server-Side Filter: Restrict the operational query specifically matching this active userId baseline
        db.collection("invoices")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("clientName", clientName)
                .whereEqualTo("grandTotal", grandTotal)
                .whereEqualTo("paymentDueDate", dueDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String docId = document.getId();

                        boolean isEmailReminderEnabled = false;
                        if (document.contains("emailReminderEnabled")) {
                            Boolean enabled = document.getBoolean("emailReminderEnabled");
                            if (enabled != null) isEmailReminderEnabled = enabled;
                        }
                        final boolean finalIsEmailEnabled = isEmailReminderEnabled;

                        String emailFromDb = "";
                        if (document.contains("businessEmail")) {
                            emailFromDb = document.getString("businessEmail");
                        }
                        if (emailFromDb == null) emailFromDb = "";
                        final String finalBusinessEmail = emailFromDb;

                        db.collection("invoices").document(docId)
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    status = newStatus;
                                    configureStatusUI();
                                    Toast.makeText(this, "Invoice registry updated to " + newStatus, Toast.LENGTH_SHORT).show();

                                    if (status.equalsIgnoreCase("paid")) {
                                        InvoiceReminderScheduler.cancelInvoiceReminder(InvoiceDetailsActivity.this, clientName, dueDate);
                                    } else {
                                        InvoiceReminderScheduler.scheduleInvoiceReminder(
                                                InvoiceDetailsActivity.this,
                                                clientName,
                                                dueDate,
                                                finalIsEmailEnabled,
                                                finalBusinessEmail,
                                                grandTotal
                                        );
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Invoice document reference not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error matching matching document parameters: " + e.getMessage()));
    }

    private String determinePendingOrOverdue(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date parsedDate = sdf.parse(dateStr);
            if (parsedDate != null && parsedDate.before(new Date())) {
                return "due";
            }
        } catch (ParseException ignored) {}
        return "pending";
    }

    private void deleteInvoiceRecord() {
        if (currentUserId == null) return;

        // 💡 Server-Side Filter: Add userId parameter protection check to prevent out-of-bounds entity purges
        db.collection("invoices")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("clientName", clientName)
                .whereEqualTo("grandTotal", grandTotal)
                .whereEqualTo("paymentDueDate", dueDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("invoices").document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    InvoiceReminderScheduler.cancelInvoiceReminder(InvoiceDetailsActivity.this, clientName, dueDate);
                                    Toast.makeText(this, "Invoice deleted successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    } else {
                        Toast.makeText(this, "Document reference matching parameters not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Deletion Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}