package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetPlannerActivity extends AppCompatActivity {

    // Duration segment buttons
    private TextView btnDuration1W;
    private TextView btnDuration1M;
    private TextView btnDuration3M;
    private TextView btnDuration6M;
    private TextView btnDurationCustom;

    // Dates
    private TextView txtStartDate;
    private TextView txtEndDate;
    private TextView txtDaysCount;

    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private int selectedDurationDays = 0;

    // Add-income row views
    private EditText editNewIncomeName;
    private EditText editNewIncomeAmount;

    // Container where income rows are rendered
    private LinearLayout containerIncomeItems;
    private TextView txtNoIncomes;

    private BudgetViewModel viewModel;

    private FirebaseFirestore db;
    private String userId;

    /** Running list of incomes loaded from Firestore */
    private final List<IncomeModel> incomeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_budget);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Hide results initially
        LinearLayout layoutResults = findViewById(R.id.layoutResults);
        if (layoutResults != null) layoutResults.setVisibility(View.GONE);

        MaterialCardView cardSemesterSummary = findViewById(R.id.cardSemesterSummary);
        if (cardSemesterSummary != null) cardSemesterSummary.setVisibility(View.GONE);

        MaterialCardView cardIncomeDetails = findViewById(R.id.cardIncomeDetails);
        if (cardIncomeDetails != null) cardIncomeDetails.setVisibility(View.VISIBLE);

        initViews();
        setupObservers();
        loadIncomesFromFirestore();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  View initialisation
    // ─────────────────────────────────────────────────────────────────────────

    private void initViews() {
        // Income list container
        containerIncomeItems = findViewById(R.id.containerIncomeItems);
        txtNoIncomes        = findViewById(R.id.txtNoIncomes);

        // Add-income inputs
        editNewIncomeName   = findViewById(R.id.editNewIncomeName);
        editNewIncomeAmount = findViewById(R.id.editNewIncomeAmount);

        MaterialButton btnAddIncome = findViewById(R.id.btnAddIncome);
        if (btnAddIncome != null) btnAddIncome.setOnClickListener(v -> addNewIncome());

        // Duration segment buttons
        btnDuration1W = findViewById(R.id.btnDuration1W);
        btnDuration1M = findViewById(R.id.btnDuration1M);
        btnDuration3M = findViewById(R.id.btnDuration3M);
        btnDuration6M = findViewById(R.id.btnDuration6M);
        btnDurationCustom = findViewById(R.id.btnDurationCustom);

        // Dates
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        txtDaysCount = findViewById(R.id.txtDaysCount);

        View.OnClickListener durationListener = v -> {
            resetDurationButtons();
            v.setBackgroundResource(R.drawable.bg_segment_active);
            ((TextView) v).setTextColor(Color.parseColor("#FFFFFF"));

            int id = v.getId();
            if (id == R.id.btnDuration1W) {
                setEndDateOffset(Calendar.DAY_OF_YEAR, 7);
            } else if (id == R.id.btnDuration1M) {
                setEndDateOffset(Calendar.MONTH, 1);
            } else if (id == R.id.btnDuration3M) {
                setEndDateOffset(Calendar.MONTH, 3);
            } else if (id == R.id.btnDuration6M) {
                setEndDateOffset(Calendar.MONTH, 6);
            } else if (id == R.id.btnDurationCustom) {
                // Just selecting it, no date change automatically
            }
        };

        btnDuration1W.setOnClickListener(durationListener);
        btnDuration1M.setOnClickListener(durationListener);
        btnDuration3M.setOnClickListener(durationListener);
        btnDuration6M.setOnClickListener(durationListener);
        btnDurationCustom.setOnClickListener(durationListener);

        txtStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        txtEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Initialize default to 1 month
        startDate = Calendar.getInstance();
        resetDurationButtons();
        btnDuration1M.setBackgroundResource(R.drawable.bg_segment_active);
        btnDuration1M.setTextColor(Color.parseColor("#FFFFFF"));
        setEndDateOffset(Calendar.MONTH, 1);

        // Calculate button
        MaterialButton btnCalculateBudget = findViewById(R.id.btnCalculateBudget);
        if (btnCalculateBudget != null)
            btnCalculateBudget.setOnClickListener(v -> validateAndCalculate());

        // Back button
        View btnBack = findViewById(R.id.btnBackContainer);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadUserGreeting();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Dates and Duration logic
    // ─────────────────────────────────────────────────────────────────────────

    private void setEndDateOffset(int field, int amount) {
        endDate = (Calendar) startDate.clone();
        endDate.add(field, amount);
        updateDateDisplays();
    }

    private void updateDateDisplays() {
        String startStr = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                startDate.get(Calendar.MONTH) + 1,
                startDate.get(Calendar.DAY_OF_MONTH),
                startDate.get(Calendar.YEAR));

        String endStr = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                endDate.get(Calendar.MONTH) + 1,
                endDate.get(Calendar.DAY_OF_MONTH),
                endDate.get(Calendar.YEAR));

        txtStartDate.setText(startStr);
        txtEndDate.setText(endStr);

        // Calculate difference in days
        long diffMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        selectedDurationDays = (int) (diffMillis / (1000 * 60 * 60 * 24));

        if (selectedDurationDays <= 0) {
            txtDaysCount.setText("Invalid date range");
            txtDaysCount.setTextColor(Color.RED);
        } else {
            txtDaysCount.setText(selectedDurationDays + " days in this budget period");
            txtDaysCount.setTextColor(Color.parseColor("#6B7280"));
        }
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar current = isStartDate ? startDate : endDate;
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    if (isStartDate) {
                        startDate.set(year, month, dayOfMonth);
                    } else {
                        endDate.set(year, month, dayOfMonth);
                    }
                    // Select "Custom range" button automatically
                    resetDurationButtons();
                    btnDurationCustom.setBackgroundResource(R.drawable.bg_segment_active);
                    btnDurationCustom.setTextColor(Color.parseColor("#FFFFFF"));

                    updateDateDisplays();
                },
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void resetDurationButtons() {
        TextView[] buttons = {btnDuration1W, btnDuration1M, btnDuration3M, btnDuration6M, btnDurationCustom};
        for (TextView btn : buttons) {
            btn.setBackgroundResource(R.drawable.bg_segment_inactive);
            btn.setTextColor(Color.parseColor("#5A6470"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Load incomes from Firestore (real-time listener)
    // ─────────────────────────────────────────────────────────────────────────

    private void loadIncomesFromFirestore() {
        if (userId == null) {
            showEmptyState();
            return;
        }

        db.collection("users").document(userId).collection("incomes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    incomeList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        IncomeModel income = doc.toObject(IncomeModel.class);
                        income.setId(doc.getId());
                        incomeList.add(income);
                    }
                    renderIncomeList();
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Render the income list rows
    // ─────────────────────────────────────────────────────────────────────────

    private void renderIncomeList() {
        containerIncomeItems.removeAllViews();

        if (incomeList.isEmpty()) {
            showEmptyState();
            return;
        }

        txtNoIncomes.setVisibility(View.GONE);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));

        for (IncomeModel income : incomeList) {
            // Row container
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dpToPx(10));
            row.setLayoutParams(rowParams);

            // Left section: name + subtitle
            LinearLayout leftCol = new LinearLayout(this);
            leftCol.setOrientation(LinearLayout.VERTICAL);
            leftCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(this);
            tvName.setText(income.getSource());
            tvName.setTextColor(Color.parseColor("#111827"));
            tvName.setTextSize(14f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText("from savings");
            tvSub.setTextColor(Color.parseColor("#9CA3AF"));
            tvSub.setTextSize(11f);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.topMargin = dpToPx(2);
            tvSub.setLayoutParams(subParams);

            leftCol.addView(tvName);
            leftCol.addView(tvSub);

            // Amount text
            TextView tvAmount = new TextView(this);
            String amountStr = "Rs." + formatAmount(income.getAmount());
            tvAmount.setText(amountStr);
            tvAmount.setTextColor(Color.parseColor("#111827"));
            tvAmount.setTextSize(14f);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams amtParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            amtParams.setMarginEnd(dpToPx(10));
            tvAmount.setLayoutParams(amtParams);

            // Remove (×) button
            TextView tvRemove = new TextView(this);
            tvRemove.setText("✕");
            tvRemove.setTextColor(Color.parseColor("#9CA3AF"));
            tvRemove.setTextSize(16f);
            tvRemove.setPadding(dpToPx(4), 0, 0, 0);
            tvRemove.setOnClickListener(v -> removeIncome(income.getId()));

            row.addView(leftCol);
            row.addView(tvAmount);
            row.addView(tvRemove);

            containerIncomeItems.addView(row);
        }
    }

    private void showEmptyState() {
        containerIncomeItems.removeAllViews();
        txtNoIncomes.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Add a new income to Firestore
    // ─────────────────────────────────────────────────────────────────────────

    private void addNewIncome() {
        String name   = editNewIncomeName.getText().toString().trim();
        String amtStr = editNewIncomeAmount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editNewIncomeName.setError("Enter income name");
            return;
        }
        if (TextUtils.isEmpty(amtStr)) {
            editNewIncomeAmount.setError("Enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                editNewIncomeAmount.setError("Must be > 0");
                return;
            }
        } catch (NumberFormatException e) {
            editNewIncomeAmount.setError("Invalid number");
            return;
        }

        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String incomeId = db.collection("users").document(userId)
                .collection("incomes").document().getId();

        Calendar cal = Calendar.getInstance();
        String date = String.format(Locale.getDefault(), "%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        IncomeModel model = new IncomeModel(incomeId, amount, name, date,
                System.currentTimeMillis());

        db.collection("users").document(userId)
                .collection("incomes").document(incomeId)
                .set(model)
                .addOnSuccessListener(aVoid -> {
                    editNewIncomeName.setText("");
                    editNewIncomeAmount.setText("");
                    Toast.makeText(this, "Income added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Remove an income from Firestore
    // ─────────────────────────────────────────────────────────────────────────

    private void removeIncome(String incomeId) {
        if (userId == null || incomeId == null) return;

        db.collection("users").document(userId)
                .collection("incomes").document(incomeId)
                .delete()
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to remove: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Validate and trigger budget calculation
    // ─────────────────────────────────────────────────────────────────────────

    private void validateAndCalculate() {
        if (incomeList.isEmpty()) {
            Toast.makeText(this, "Please add at least one income source", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDurationDays <= 0) {
            Toast.makeText(this, "Please select a valid duration (Start Date must be before End Date)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sum all incomes as the total period income
        double totalPeriodIncome = 0;
        for (IncomeModel inc : incomeList) {
            totalPeriodIncome += inc.getAmount();
        }

        // Pass total as the first parameter ("allowance"), duration as days
        viewModel.calculateAndSaveBudget(totalPeriodIncome, 0, 0, selectedDurationDays);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ViewModel observers
    // ─────────────────────────────────────────────────────────────────────────

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            MaterialButton btn = findViewById(R.id.btnCalculateBudget);
            if (btn == null) return;
            if (isLoading) {
                btn.setEnabled(false);
                btn.setText("Calculating...");
            } else {
                btn.setEnabled(true);
                btn.setText("Calculate Budget");
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getCalculationSuccess().observe(this, model -> {
            if (model != null) {
                Intent intent = new Intent(this, BudgetSummaryActivity.class);
                intent.putExtra("budgetModel", model);
                startActivity(intent);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  User greeting
    // ─────────────────────────────────────────────────────────────────────────

    private void loadUserGreeting() {
        TextView txtGreeting = findViewById(R.id.txtGreeting);
        if (txtGreeting == null) return;

        String baseGreeting = getGreeting();

        if (userId == null) {
            txtGreeting.setText(baseGreeting + " 👋");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("name")) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            String firstName = name.trim().split("\\s+")[0];
                            txtGreeting.setText(baseGreeting + ", " + firstName + " 👋");
                        } else {
                            txtGreeting.setText(baseGreeting + " 👋");
                        }
                    } else {
                        txtGreeting.setText(baseGreeting + " 👋");
                    }
                })
                .addOnFailureListener(e -> txtGreeting.setText(baseGreeting + " 👋"));
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12)       return "Good Morning";
        else if (hour < 17)  return "Good Afternoon";
        else                 return "Good Evening";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String formatAmount(double amount) {
        if (amount == Math.floor(amount) && !Double.isInfinite(amount)) {
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
        return String.format(Locale.getDefault(), "%,.0f", amount);
    }
}
