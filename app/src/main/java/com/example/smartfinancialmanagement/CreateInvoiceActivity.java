package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class CreateInvoiceActivity extends AppCompatActivity {

    private TextView btnBack;
    private TextInputEditText etClientName, etClientBRN, etItemName, etQty, etPrice;
    private TextView txtSubtotal, txtGrandTotal;
    private MaterialButton btnGenerateInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etClientName = findViewById(R.id.etClientName);
        etClientBRN = findViewById(R.id.etClientBRN);
        etItemName = findViewById(R.id.etItemName);
        etQty = findViewById(R.id.etQty);
        etPrice = findViewById(R.id.etPrice);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);
        btnGenerateInvoice = findViewById(R.id.btnGenerateInvoice);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Dynamic pricing calculation watch bindings
        TextWatcher calculationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotals();
            }
        };

        etQty.addTextChangedListener(calculationWatcher);
        etPrice.addTextChangedListener(calculationWatcher);

        btnGenerateInvoice.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Invoice Processed and Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void calculateTotals() {
        String qtyString = etQty.getText().toString().trim();
        String priceString = etPrice.getText().toString().trim();

        int qty = qtyString.isEmpty() ? 0 : Integer.parseInt(qtyString);
        double price = priceString.isEmpty() ? 0.0 : Double.parseDouble(priceString);

        double total = qty * price;

        String formattedTotal = String.format(Locale.getDefault(), "Rs. %,.2f", total);
        txtSubtotal.setText(formattedTotal);
        txtGrandTotal.setText(formattedTotal);
    }

    private boolean validateInputs() {
        if (etClientName.getText().toString().trim().isEmpty()) {
            etClientName.setError("Client Name required");
            return false;
        }
        if (etItemName.getText().toString().trim().isEmpty()) {
            etItemName.setError("Item details required");
            return false;
        }
        if (etQty.getText().toString().trim().isEmpty()) {
            etQty.setError("Specify quantity");
            return false;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Specify base rate");
            return false;
        }
        return true;
    }
}