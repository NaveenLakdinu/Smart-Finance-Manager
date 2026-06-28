package com.example.smartfinancialmanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailsActivity extends AppCompatActivity {

    private TextView txtInvoiceStatusBadge, txtDetInvoiceNum, txtDetClientName, txtDetClientBRN;
    private TextView txtDetItemName, txtDetQty, txtDetPrice, txtDetTotal, btnBack;
    private MaterialButton btnMarkPaid, btnDeleteInvoice;

    private FirebaseFirestore db;
    private String clientName, selectedBusiness, status, dueDate;
    private double grandTotal;

    // Replace this string with a dynamic document lookup ID passed from your adapter
    private String invoiceDocId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail); // Make sure your layout file matches this name

        db = FirebaseFirestore.getInstance();

        initializeViews();
        getIntentData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtInvoiceStatusBadge = findViewById(R.id.txtInvoiceStatusBadge);
        txtDetInvoiceNum = findViewById(R.id.txtDetInvoiceNum);
        txtDetClientName = findViewById(R.id.txtDetClientName);
        txtDetClientBRN = findViewById(R.id.txtDetClientBRN);
        txtDetItemName = findViewById(R.id.txtDetItemName);
        txtDetQty = findViewById(R.id.txtDetQty);
        txtDetPrice = findViewById(R.id.txtDetPrice);
        txtDetTotal = findViewById(R.id.txtDetTotal);
        btnMarkPaid = findViewById(R.id.btnMarkPaid);
        btnDeleteInvoice = findViewById(R.id.btnSendReminder); // Bound to old button id to minimize XML edits
    }

    private void getIntentData() {
        if (getIntent() != null) {
            clientName = getIntent().getStringExtra("clientName");
            selectedBusiness = getIntent().getStringExtra("selectedBusiness");
            status = getIntent().getStringExtra("status");
            dueDate = getIntent().getStringExtra("paymentDueDate");
            grandTotal = getIntent().getDoubleExtra("grandTotal", 0.0);

            if (status == null) status = "pending";

            // Fallback safety string formatting
            txtDetClientName.setText(clientName);
            txtDetClientBRN.setText(getIntent().getStringExtra("clientBRN"));
            txtDetItemName.setText(getIntent().getStringExtra("itemName"));
            txtDetQty.setText(String.valueOf(getIntent().getIntExtra("quantity", 1)));

            double unitPrice = getIntent().getDoubleExtra("unitPrice", 0.0);
            txtDetPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", unitPrice));
            txtDetTotal.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));

            // Random mock number block layout setup or parse sequence mapping
            txtDetInvoiceNum.setText("#INV-" + Math.abs(clientName.hashCode() % 10000));

            configureStatusUI();
        }
    }

    private void configureStatusUI() {
        txtInvoiceStatusBadge.setText(status.toUpperCase());

        if (status.equalsIgnoreCase("paid")) {
            txtInvoiceStatusBadge.setTextColor(Color.parseColor("#071A33"));
            txtInvoiceStatusBadge.setBackgroundColor(Color.parseColor("#4ADE80"));

            // Swap action layout behavior to Unpaid toggle state
            btnMarkPaid.setText("MARK AS UNPAID");
            btnMarkPaid.setBackgroundColor(Color.parseColor("#FF5555"));
            btnMarkPaid.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // Check if it's currently overdue ("due") compared to today
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
        // Toggle layout state logic paths
        String newStatus;
        if (status.equalsIgnoreCase("paid")) {
            // Re-calculate whether it belongs to pending or due based on target date metric parameters
            newStatus = determinePendingOrOverdue(dueDate);
        } else {
            newStatus = "paid";
        }

        // Locate document inside database collection structure paths
        db.collection("invoices")
                .whereEqualTo("clientName", clientName)
                .whereEqualTo("grandTotal", grandTotal)
                .whereEqualTo("paymentDueDate", dueDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("invoices").document(docId)
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    status = newStatus;
                                    configureStatusUI();
                                    Toast.makeText(this, "Invoice registry updated to " + newStatus, Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private String determinePendingOrOverdue(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date parsedDate = sdf.parse(dateStr);
            if (parsedDate != null && parsedDate.before(new Date())) {
                return "due"; // Overdue matrix tracking configuration point
            }
        } catch (ParseException ignored) {}
        return "pending";
    }

    private void deleteInvoiceRecord() {
        db.collection("invoices")
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
                                    Toast.makeText(this, "Invoice deleted successfully!", Toast.LENGTH_SHORT).show();
                                    finish(); // Drop activity stack frame to instantly route back to update view loops
                                });
                    } else {
                        Toast.makeText(this, "Document reference matching parameters not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Deletions Interrupted: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}