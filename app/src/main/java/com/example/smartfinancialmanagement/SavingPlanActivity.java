package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Map;

public class SavingPlanActivity extends AppCompatActivity {

    private EditText goalNameInput, targetAmountInput, currentSavingsInput, monthlyAmountInput;
    private TextView targetDateText, progressText;
    private LinearLayout targetDatePicker;
    private Spinner frequencySpinner;
    private ProgressBar savingsProgressBar;
    private MaterialButton registerButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_saving_plan);

        // 1. Views Bind
        initViews();

        // 2. before saving data show
        loadExistingData();

        // 3. Setup Listeners
        setupListeners();

        // 4. Progress bar  update  TextWatchers
        setupTextWatchers();
    }

    private void initViews() {
        goalNameInput        = findViewById(R.id.goalNameInput);
        targetAmountInput    = findViewById(R.id.targetAmountInput);
        currentSavingsInput  = findViewById(R.id.currentSavingsInput);
        monthlyAmountInput   = findViewById(R.id.monthlyAmountInput);
        targetDateText       = findViewById(R.id.targetDateText);
        targetDatePicker     = findViewById(R.id.targetDatePicker);
        frequencySpinner     = findViewById(R.id.frequencySpinner);
        savingsProgressBar   = findViewById(R.id.savingsProgressBar);
        progressText         = findViewById(R.id.progressText);
        registerButton       = findViewById(R.id.nextButton);
        backButton           = findViewById(R.id.backButton);

        setupFrequencySpinner();
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Date Picker
        targetDatePicker.setOnClickListener(v -> showDatePicker());

        // "Register"  Button
        registerButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveToSingleton();
                Toast.makeText(this, "Saving plan details saved!", Toast.LENGTH_SHORT).show();
                finish(); // back to Main Register page
            }
        });
    }

    private void saveToSingleton() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        data.hasSavingPlan = true; // Checkbox tik show
        data.goalName = goalNameInput.getText().toString().trim();
        data.targetAmount = targetAmountInput.getText().toString().trim();
        data.targetDate = targetDateText.getText().toString().trim();
        data.currentSavings = currentSavingsInput.getText().toString().trim();
        data.monthlySavingAmount = monthlyAmountInput.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("hasSavingPlan", true);
            update.put("savingGoalName", data.goalName);
            update.put("savingTargetAmount", data.targetAmount);
            update.put("savingTargetDate", data.targetDate);
            update.put("currentSavings", data.currentSavings);
            update.put("monthlySavingAmount", data.monthlySavingAmount);

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update(update)
                    .addOnFailureListener(e -> {
                        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                .set(update, com.google.firebase.firestore.SetOptions.merge());
                    });
        }
    }

    private void loadExistingData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean hasPlan = documentSnapshot.getBoolean("hasSavingPlan");
                            if (hasPlan != null && hasPlan) {
                                goalNameInput.setText(documentSnapshot.getString("savingGoalName"));
                                targetAmountInput.setText(documentSnapshot.getString("savingTargetAmount"));
                                targetDateText.setText(documentSnapshot.getString("savingTargetDate"));
                                currentSavingsInput.setText(documentSnapshot.getString("currentSavings"));
                                monthlyAmountInput.setText(documentSnapshot.getString("monthlySavingAmount"));
                                updateProgressBar();
                            }
                        }
                    });
        } else {
            UserRegistrationData data = UserRegistrationData.getInstance();
            if (data.hasSavingPlan) {
                goalNameInput.setText(data.goalName);
                targetAmountInput.setText(data.targetAmount);
                targetDateText.setText(data.targetDate);
                currentSavingsInput.setText(data.currentSavings);
                monthlyAmountInput.setText(data.monthlySavingAmount);
                updateProgressBar();
            }
        }
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProgressBar();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        currentSavingsInput.addTextChangedListener(watcher);
        targetAmountInput.addTextChangedListener(watcher);
    }

    private void updateProgressBar() {
        try {
            String currentStr = currentSavingsInput.getText().toString().trim();
            String targetStr  = targetAmountInput.getText().toString().trim();

            if (!currentStr.isEmpty() && !targetStr.isEmpty()) {
                double current = Double.parseDouble(currentStr);
                double target  = Double.parseDouble(targetStr);

                if (target > 0) {
                    int progress = (int) ((current / target) * 100);
                    progress = Math.min(progress, 100);
                    savingsProgressBar.setProgress(progress);
                    progressText.setText(progress + "% of target reached");
                }
            }
        } catch (NumberFormatException e) {
            savingsProgressBar.setProgress(0);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = String.format(java.util.Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    targetDateText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupFrequencySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.saving_frequencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);
    }

    private boolean validateForm() {
        String goalName = goalNameInput.getText().toString().trim();
        String targetAmountStr = targetAmountInput.getText().toString().trim();
        String currentSavingsStr = currentSavingsInput.getText().toString().trim();
        String monthlyAmountStr = monthlyAmountInput.getText().toString().trim();

        if (goalName.isEmpty()) {
            goalNameInput.setError("Enter goal name");
            goalNameInput.requestFocus();
            return false;
        }

        if (targetAmountStr.isEmpty()) {
            targetAmountInput.setError("Enter target amount");
            targetAmountInput.requestFocus();
            return false;
        }

        try {
            double target = Double.parseDouble(targetAmountStr);
            if (target <= 0) {
                targetAmountInput.setError("Target amount must be greater than zero");
                targetAmountInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            targetAmountInput.setError("Enter a valid numeric amount");
            targetAmountInput.requestFocus();
            return false;
        }

        if (!currentSavingsStr.isEmpty()) {
            try {
                double current = Double.parseDouble(currentSavingsStr);
                if (current < 0) {
                    currentSavingsInput.setError("Current savings cannot be negative");
                    currentSavingsInput.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                currentSavingsInput.setError("Enter a valid numeric amount");
                currentSavingsInput.requestFocus();
                return false;
            }
        }

        if (!monthlyAmountStr.isEmpty()) {
            try {
                double monthly = Double.parseDouble(monthlyAmountStr);
                if (monthly < 0) {
                    monthlyAmountInput.setError("Monthly amount cannot be negative");
                    monthlyAmountInput.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                monthlyAmountInput.setError("Enter a valid numeric amount");
                monthlyAmountInput.requestFocus();
                return false;
            }
        }

        if (targetDateText.getText().toString().equals("Select goal deadline")) {
            Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}