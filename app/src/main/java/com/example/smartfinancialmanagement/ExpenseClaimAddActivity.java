package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExpenseClaimAddActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etClaimTitle, etClaimAmount, etClaimDescription;
    private Spinner spinnerCategory;
    private TextView tvExpenseDate;
    private MaterialButton btnSubmitClaim;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claim_add);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        initViews();
        setupCategorySpinner();
        setupDatePicker();
        setupSubmitButton();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etClaimTitle = findViewById(R.id.etClaimTitle);
        etClaimAmount = findViewById(R.id.etClaimAmount);
        etClaimDescription = findViewById(R.id.etClaimDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvExpenseDate = findViewById(R.id.tvExpenseDate);
        btnSubmitClaim = findViewById(R.id.btnSubmitClaim);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Travel", "Meals", "Transport", "Accommodation", "Supplies", "Other"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories));
    }

    private void setupDatePicker() {
        tvExpenseDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                tvExpenseDate.setText(date);
                tvExpenseDate.setTextColor(getResources().getColor(R.color.text_on_dark_primary));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupSubmitButton() {
        btnSubmitClaim.setOnClickListener(v -> {
            if (validateInputs()) {
                saveClaimToFirestore();
            }
        });
    }

    private boolean validateInputs() {
        String title = etClaimTitle.getText().toString().trim();
        String amountStr = etClaimAmount.getText().toString().trim();

        if (title.isEmpty()) {
            etClaimTitle.setError("Title is required");
            etClaimTitle.requestFocus();
            return false;
        }

        if (amountStr.isEmpty()) {
            etClaimAmount.setError("Amount is required");
            etClaimAmount.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etClaimAmount.setError("Amount must be positive");
                etClaimAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etClaimAmount.setError("Invalid amount");
            etClaimAmount.requestFocus();
            return false;
        }

        return true;
    }

    private void saveClaimToFirestore() {
        if (uid == null) {
            Toast.makeText(this, "Please login to submit a claim", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitClaim.setEnabled(false);
        btnSubmitClaim.setText("Submitting...");

        String title = etClaimTitle.getText().toString().trim();
        String amountStr = etClaimAmount.getText().toString().trim();
        String description = etClaimDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = tvExpenseDate.getText().toString().trim();
        double amount = Double.parseDouble(amountStr);

        String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("title", title);
        claimData.put("category", category);
        claimData.put("amount", amount);
        claimData.put("expenseDate", date.equals("Tap to select date") ? "N/A" : date);
        claimData.put("description", description.isEmpty() ? "No description" : description);
        claimData.put("receiptCount", 0);
        claimData.put("status", "PENDING");
        claimData.put("workerEmail", email != null ? email : "");
        claimData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid)
                .collection("expense_claims")
                .add(claimData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Claim submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitClaim.setEnabled(true);
                    btnSubmitClaim.setText("Submit Claim");
                });
    }
}
