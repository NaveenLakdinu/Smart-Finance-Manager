// SavingPlanActivity.java
package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class SavingPlanActivity extends AppCompatActivity {

    EditText goalNameInput, targetAmountInput, currentSavingsInput, monthlyAmountInput;
    TextView targetDateText, progressText;
    LinearLayout targetDatePicker;
    Spinner frequencySpinner;
    ProgressBar savingsProgressBar;
    MaterialButton registerButton;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_plan);

        // Views bind
        goalNameInput        = findViewById(R.id.goalNameInput);
        targetAmountInput    = findViewById(R.id.targetAmountInput);
        currentSavingsInput  = findViewById(R.id.currentSavingsInput);
        monthlyAmountInput   = findViewById(R.id.monthlyAmountInput);
        targetDateText       = findViewById(R.id.targetDateText);
        targetDatePicker     = findViewById(R.id.targetDatePicker);
        frequencySpinner     = findViewById(R.id.frequencySpinner);
        savingsProgressBar   = findViewById(R.id.savingsProgressBar);
        progressText         = findViewById(R.id.progressText);
        registerButton       = findViewById(R.id.registerButton);
        backButton           = findViewById(R.id.backButton);

        // ── Back button ──
        backButton.setOnClickListener(v -> onBackPressed());

        // ── Setup Frequency Spinner ──
        setupFrequencySpinner();

        // ── Date Picker ──
        targetDatePicker.setOnClickListener(v -> showDatePicker());

        // ── Progress bar real-time update ──
        currentSavingsInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProgressBar();
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        targetAmountInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProgressBar();
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        // ── Register button ──
        registerButton.setOnClickListener(v -> {
            if (validateForm()) {
                // ✅ Registration complete — Dashboard navigate කරන්න
                Toast.makeText(this,
                        "Registration Successful! Welcome to Red Ants!",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SavingPlanActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // ── Setup Frequency Spinner ──
    private void setupFrequencySpinner() {
        // Create an ArrayAdapter using string array and custom spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.saving_frequencies,
                R.layout.spinner_item);
        
        // Specify layout to use when list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        // Apply the adapter to the spinner
        frequencySpinner.setAdapter(adapter);
    }

    // ── Date Picker Dialog ──
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    targetDateText.setText(date);
                }, year, month, day);

        // Future dates only
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    // ── Progress Bar Update ──
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

    // ── Form Validation ──
    private boolean validateForm() {
        String goalName     = goalNameInput.getText().toString().trim();
        String targetAmount = targetAmountInput.getText().toString().trim();
        String targetDate   = targetDateText.getText().toString().trim();
        String currentSav   = currentSavingsInput.getText().toString().trim();

        if (goalName.isEmpty()) {
            goalNameInput.setError("Please enter saving goal name");
            goalNameInput.requestFocus();
            return false;
        }
        if (targetAmount.isEmpty()) {
            targetAmountInput.setError("Please enter target amount");
            targetAmountInput.requestFocus();
            return false;
        }
        if (targetDate.isEmpty() || targetDate.equals("Select goal deadline")) {
            Toast.makeText(this, "Please select a target date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentSav.isEmpty()) {
            currentSavingsInput.setError("Please enter current savings");
            currentSavingsInput.requestFocus();
            return false;
        }

        try {
            double current = Double.parseDouble(currentSav);
            double target  = Double.parseDouble(targetAmount);
            if (current > target) {
                currentSavingsInput.setError("Current savings cannot exceed target amount");
                currentSavingsInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}