package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class LoanCompareActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout loanCardsContainer;
    private MaterialCardView btnAddOption;
    private int optionCount = 0;

    private com.google.android.material.button.MaterialButton btnCompare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_compare);

        initViews();
        setupListeners();

        // Start with 3 default options as requested
        for (int i = 0; i < 3; i++) {
            addLoanOptionCard();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        loanCardsContainer = findViewById(R.id.loanCardsContainer);
        btnAddOption = findViewById(R.id.btnAddOption);
        btnCompare = findViewById(R.id.btnCompare);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddOption.setOnClickListener(v -> addLoanOptionCard());
        
        btnCompare.setOnClickListener(v -> {
            if (validateAllCards()) {
                // TODO: Navigate to Report Activity with data
                android.widget.Toast.makeText(this, "All inputs valid! Generating report...", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateAllCards() {
        boolean allValid = true;
        int childCount = loanCardsContainer.getChildCount();

        if (childCount == 0) {
            android.widget.Toast.makeText(this, "Please add at least one loan option", android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < childCount; i++) {
            View cardView = loanCardsContainer.getChildAt(i);
            if (!validateSingleCard(cardView)) {
                allValid = false;
            }
        }

        if (!allValid) {
            android.widget.Toast.makeText(this, "Please fix the errors in your loan options", android.widget.Toast.LENGTH_SHORT).show();
        }

        return allValid;
    }

    private boolean validateSingleCard(View cardView) {
        EditText etBank = cardView.findViewById(R.id.etBankName);
        EditText etP = cardView.findViewById(R.id.etPrincipal);
        EditText etI = cardView.findViewById(R.id.etInterest);
        EditText etD = cardView.findViewById(R.id.etDuration);

        boolean valid = true;

        if (etBank.getText().toString().trim().isEmpty()) {
            etBank.setError("Bank name is required");
            valid = false;
        }

        String pStr = etP.getText().toString().trim();
        if (pStr.isEmpty() || Double.parseDouble(pStr) <= 0) {
            etP.setError("Enter valid principal");
            valid = false;
        }

        String iStr = etI.getText().toString().trim();
        if (iStr.isEmpty() || Double.parseDouble(iStr) < 0) {
            etI.setError("Enter valid interest");
            valid = false;
        }

        String dStr = etD.getText().toString().trim();
        if (dStr.isEmpty() || Integer.parseInt(dStr) <= 0) {
            etD.setError("Enter valid months");
            valid = false;
        }

        return valid;
    }

    private void addLoanOptionCard() {
        optionCount++;
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_loan_compare_card, loanCardsContainer, false);

        TextView title = cardView.findViewById(R.id.txtLoanOptionTitle);
        title.setText("Option " + (char) ('A' + (optionCount - 1)));

        ImageView btnRemove = cardView.findViewById(R.id.btnRemoveLoan);
        
        // Only show remove button for options after the first 3
        if (optionCount > 3) {
            btnRemove.setVisibility(View.VISIBLE);
            btnRemove.setOnClickListener(v -> {
                loanCardsContainer.removeView(cardView);
                // Note: Title letters won't auto-update without re-iterating, 
                // but this keeps implementation simple.
            });
        }

        setupCardLogic(cardView);
        loanCardsContainer.addView(cardView);
    }

    private void setupCardLogic(View cardView) {
        EditText etPrincipal = cardView.findViewById(R.id.etPrincipal);
        EditText etInterest = cardView.findViewById(R.id.etInterest);
        EditText etDuration = cardView.findViewById(R.id.etDuration);
        TextView txtResult = cardView.findViewById(R.id.txtComparisonResult);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateCardEMI(etPrincipal, etInterest, etDuration, txtResult);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPrincipal.addTextChangedListener(watcher);
        etInterest.addTextChangedListener(watcher);
        etDuration.addTextChangedListener(watcher);
    }

    private void calculateCardEMI(EditText etP, EditText etI, EditText etD, TextView txtRes) {
        try {
            String pStr = etP.getText().toString();
            String iStr = etI.getText().toString();
            String dStr = etD.getText().toString();

            if (!pStr.isEmpty() && !iStr.isEmpty() && !dStr.isEmpty()) {
                double p = Double.parseDouble(pStr);
                double annualRate = Double.parseDouble(iStr);
                int n = Integer.parseInt(dStr);

                double r = annualRate / (12 * 100);
                double emi;
                if (r == 0) {
                    emi = p / n;
                } else {
                    emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                }

                double total = emi * n;
                txtRes.setText(String.format(Locale.US, "EMI: $%.2f | Total: $%.2f", emi, total));
            } else {
                txtRes.setText("EMI: $0.00 | Total: $0.00");
            }
        } catch (Exception e) {
            txtRes.setText("EMI: $0.00 | Total: $0.00");
        }
    }
}
