package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class InvoiceDetailsActivity extends AppCompatActivity {

    private TextView btnBack;
    private TextView txtInvoiceStatusBadge;
    private TextView txtDetInvoiceNum;
    private TextView txtDetClientName;
    private TextView txtDetClientBRN;
    private TextView txtDetItemName;
    private TextView txtDetQty;
    private TextView txtDetPrice;
    private TextView txtDetTotal;

    private MaterialButton btnMarkPaid;
    private MaterialButton btnSendReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        initializeViews();
        setupListeners();
        loadInvoiceData();
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
        btnSendReminder = findViewById(R.id.btnSendReminder);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMarkPaid.setOnClickListener(v -> {
            // Update status UI state dynamically
            txtInvoiceStatusBadge.setText("PAID");
            txtInvoiceStatusBadge.setTextColor(android.graphics.Color.parseColor("#4ADE80"));

            Toast.makeText(this, "Invoice reconciled successfully!", Toast.LENGTH_SHORT).show();
            btnMarkPaid.setEnabled(false);
            btnSendReminder.setEnabled(false);
        });

        btnSendReminder.setOnClickListener(v -> {
            Toast.makeText(this, "Reminder alert notification dispatched to partner!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadInvoiceData() {
        // Intended target location to extract intent bundles when hooking up real dashboard database items
        // Example: String invoiceNum = getIntent().getStringExtra("INVOICE_NUM");
    }
}