package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;
import android.widget.LinearLayout;
import android.graphics.Color;
import java.text.NumberFormat;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

public class AddIncomeActivity extends AppCompatActivity {

    private EditText etIncomeAmount, etIncomeSource;
    private TextView tvIncomeDate;
    private MaterialButton btnSaveIncome;
    private FrameLayout btnBackIncome;
    private TextView tvTotalIncomeValue;
    private LinearLayout containerIncomes;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etIncomeAmount = findViewById(R.id.etIncomeAmount);
        etIncomeSource = findViewById(R.id.etIncomeSource);
        tvIncomeDate = findViewById(R.id.tvIncomeDate);
        btnSaveIncome = findViewById(R.id.btnSaveIncome);
        btnBackIncome = findViewById(R.id.btnBackIncome);
        tvTotalIncomeValue = findViewById(R.id.tvTotalIncomeValue);
        containerIncomes = findViewById(R.id.containerIncomes);

        // Set up click listeners
        btnBackIncome.setOnClickListener(v -> finish());

        tvIncomeDate.setOnClickListener(v -> showDatePicker());

        btnSaveIncome.setOnClickListener(v -> saveIncome());
        
        loadIncomes();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    tvIncomeDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveIncome() {
        String amountStr = etIncomeAmount.getText().toString().trim();
        String source = etIncomeSource.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etIncomeAmount.setError("Amount is required");
            return;
        }

        if (TextUtils.isEmpty(source)) {
            etIncomeSource.setError("Source is required");
            return;
        }

        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etIncomeAmount.setError("Invalid amount format");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        
        btnSaveIncome.setEnabled(false);
        btnSaveIncome.setText("Saving...");

        // Create document reference to get a unique ID
        String incomeId = db.collection("users").document(userId).collection("incomes").document().getId();
        
        long timestamp = System.currentTimeMillis();
        IncomeModel incomeModel = new IncomeModel(incomeId, amount, source, selectedDate, timestamp);

        db.collection("users").document(userId).collection("incomes").document(incomeId)
                .set(incomeModel)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddIncomeActivity.this, "Income saved successfully!", Toast.LENGTH_SHORT).show();
                    etIncomeAmount.setText("");
                    etIncomeSource.setText("");
                    tvIncomeDate.setText("Select Date");
                    selectedDate = "";
                    btnSaveIncome.setEnabled(true);
                    btnSaveIncome.setText("Save Income");
                })
                .addOnFailureListener(e -> {
                    btnSaveIncome.setEnabled(true);
                    btnSaveIncome.setText("Save Income");
                    Toast.makeText(AddIncomeActivity.this, "Failed to save income: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadIncomes() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("incomes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    double totalIncome = 0;
                    containerIncomes.removeAllViews();
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

                    for (QueryDocumentSnapshot doc : snapshot) {
                        IncomeModel income = doc.toObject(IncomeModel.class);
                        totalIncome += income.getAmount();

                        // Programmatically create layout for the income item
                        LinearLayout itemLayout = new LinearLayout(this);
                        itemLayout.setOrientation(LinearLayout.VERTICAL);
                        itemLayout.setPadding(32, 24, 32, 24);
                        
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);
                        itemLayout.setLayoutParams(params);
                        itemLayout.setBackgroundResource(R.drawable.form_card_bg); // Use existing card background

                        // Top row: Source and Amount
                        LinearLayout topRow = new LinearLayout(this);
                        topRow.setOrientation(LinearLayout.HORIZONTAL);
                        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        TextView tvSource = new TextView(this);
                        tvSource.setText(income.getSource());
                        tvSource.setTextColor(Color.WHITE);
                        tvSource.setTextSize(16f);
                        tvSource.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvSource.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        
                        TextView tvAmount = new TextView(this);
                        tvAmount.setText(format.format(income.getAmount()).replace("LKR", "LKR "));
                        tvAmount.setTextColor(Color.parseColor("#10B981")); // Mint green
                        tvAmount.setTextSize(16f);
                        tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
                        tvAmount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        topRow.addView(tvSource);
                        topRow.addView(tvAmount);

                        // Bottom row: Date
                        TextView tvDate = new TextView(this);
                        tvDate.setText(income.getDate());
                        tvDate.setTextColor(Color.parseColor("#9CA3AF")); // Gray
                        tvDate.setTextSize(14f);
                        
                        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        dateParams.topMargin = 8;
                        tvDate.setLayoutParams(dateParams);

                        itemLayout.addView(topRow);
                        itemLayout.addView(tvDate);
                        
                        containerIncomes.addView(itemLayout);
                    }

                    if (tvTotalIncomeValue != null) {
                        tvTotalIncomeValue.setText("Total: " + format.format(totalIncome).replace("LKR", "LKR "));
                    }
                });
    }
}
