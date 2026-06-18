package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Locale;

public class InvoiceHubActivity extends AppCompatActivity {

    // UI View References
    private TextView btnBack;
    private TextView txtTotalAmountDue;
    private RecyclerView rvInvoices;
    private FloatingActionButton fabAddInvoice;

    // Local State Variables (Matching layout data)
    private double totalOutstandingAmount = 1420500.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_hub); // Matches your layout file name

        initializeViews();
        setupNavigation();
        setupInvoiceList();
        updateOutstandingUI();
    }

    /**
     * Finds and hooks into the layout elements.
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTotalAmountDue = findViewById(R.id.txtTotalAmountDue);
        rvInvoices = findViewById(R.id.rvInvoices);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);
    }

    /**
     * Handles actionable click routines.
     */
    private void setupNavigation() {
        // Safe navigation back to Main Dashboard
        btnBack.setOnClickListener(v -> finish());

        // Fab action shortcut button routing to invoice entry page
        fabAddInvoice.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Invoice Creator...", Toast.LENGTH_SHORT).show();

            // Un-comment this track when your Invoice Form Activity is ready:
            // Intent intent = new Intent(InvoiceHubActivity.this, CreateInvoiceActivity.class);
            // startActivity(intent);
        });
    }

    /**
     * Prepares your internal data stream engine loops.
     */
    private void setupInvoiceList() {
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));

        // Note: For now, the RecyclerView is set up with an empty state layout template.
        // Once your custom list row item layout and Custom Adapter classes are written,
        // you would initialize and pass data models here:
        // InvoiceAdapter adapter = new InvoiceAdapter(getMockInvoiceData());
        // rvInvoices.setAdapter(adapter);
    }

    /**
     * Synchronizes display layout text fields with standard currency parsing patterns.
     */
    private void updateOutstandingUI() {
        txtTotalAmountDue.setText(String.format(Locale.getDefault(), "Rs. %,.2f", totalOutstandingAmount));
    }
}