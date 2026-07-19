
package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class OnboardingActivity extends AppCompatActivity {

    ViewFlipper viewFlipper;
    int currentStep = 0;

    // Step 1 — Loan
    EditText loanAmount, monthlyInstallment, monthsPaid;
    Spinner  paymentMethodSpinner;

    // Step 2 — Subscription
    CheckBox checkEmail, checkSms, checkPush;

    // Step 3 — Saving
    EditText goalName, targetAmount, currentSavings;
    TextView targetDateText;
    LinearLayout targetDateRow;
    Spinner frequencySpinner;

    // Header views
    TextView stepLabel, stepTitle, stepSubtitle;
    View seg1, seg2, seg3;

    // Step metadata
    String[] titles    = {"Enter Your Loan Details", "Subscription Updates", "I Do Have a Saving Plan"};
    String[] subtitles = {
            "Help us understand your loan so we can track it better.",
            "Choose how you want to stay informed about renewals.",
            "Set up your savings goal and let us help you reach it."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Header
        stepLabel    = findViewById(R.id.stepLabel);
        stepTitle    = findViewById(R.id.stepTitle);
        stepSubtitle = findViewById(R.id.stepSubtitle);
        seg1 = findViewById(R.id.seg1);
        seg2 = findViewById(R.id.seg2);
        seg3 = findViewById(R.id.seg3);

        // ViewFlipper
        viewFlipper = findViewById(R.id.viewFlipper);

        // Step 1 views
        loanAmount           = findViewById(R.id.loanAmount);
        monthlyInstallment   = findViewById(R.id.monthlyInstallment);
        monthsPaid           = findViewById(R.id.monthsPaid);
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);

        // Step 2 views
        checkEmail = findViewById(R.id.checkEmail);
        checkSms   = findViewById(R.id.checkSms);
        checkPush  = findViewById(R.id.checkPush);

        // Step 3 views
        goalName      = findViewById(R.id.goalName);
        targetAmount  = findViewById(R.id.targetAmount);
        currentSavings= findViewById(R.id.currentSavings);
        targetDateText= findViewById(R.id.targetDateText);
        targetDateRow = findViewById(R.id.targetDateRow);
        frequencySpinner = findViewById(R.id.frequencySpinner);

        // Date picker
        targetDateRow.setOnClickListener(v -> showDatePicker());

        // Global skip all
        findViewById(R.id.globalSkipBtn).setOnClickListener(v -> goToDashboard());

        // Step 1 buttons
        ((MaterialButton) findViewById(R.id.step1Next)).setOnClickListener(v -> {
            saveLoanData();
            goToStep(1);
        });
        ((TextView) findViewById(R.id.step1Skip)).setOnClickListener(v -> goToStep(1));

        // Step 2 buttons
        ((MaterialButton) findViewById(R.id.step2Next)).setOnClickListener(v -> {
            saveSubscriptionData();
            goToStep(2);
        });
        ((TextView) findViewById(R.id.step2Skip)).setOnClickListener(v -> goToStep(2));

        // Step 3 buttons
        ((MaterialButton) findViewById(R.id.step3Save)).setOnClickListener(v -> {
            if (validateStep3()) {
                savePlanData();
                Toast.makeText(this, "Profile saved! Welcome 🎉", Toast.LENGTH_SHORT).show();
                goToDashboard();
            }
        });
        ((TextView) findViewById(R.id.step3Skip)).setOnClickListener(v -> goToDashboard());

        updateHeader(0);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Navigate to step
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void goToStep(int step) {
        currentStep = step;
        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        viewFlipper.setDisplayedChild(step);
        updateHeader(step);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Update top header
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void updateHeader(int step) {
        stepLabel.setText("Step " + (step + 1) + " of 3");
        stepTitle.setText(titles[step]);
        stepSubtitle.setText(subtitles[step]);

        int activeColor   = getResources().getColor(android.R.color.holo_blue_dark);
        int inactiveColor = getResources().getColor(android.R.color.darker_gray);

        seg1.setBackgroundResource(step >= 0 ? R.drawable.seg_active : R.drawable.seg_inactive);
        seg2.setBackgroundResource(step >= 1 ? R.drawable.seg_active : R.drawable.seg_inactive);
        seg3.setBackgroundResource(step >= 2 ? R.drawable.seg_active : R.drawable.seg_inactive);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Date Picker
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            targetDateText.setText(d + "/" + (m + 1) + "/" + y);
            targetDateText.setTextColor(getResources().getColor(android.R.color.black));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Save data to SharedPreferences
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void saveLoanData() {
        String amount = loanAmount.getText().toString().trim();
        if (amount.isEmpty()) return;

        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("has_loan", true)
                .putString("loan_amount", amount)
                .putString("monthly_installment", monthlyInstallment.getText().toString().trim())
                .putString("months_paid", monthsPaid.getText().toString().trim())
                .putString("payment_method", paymentMethodSpinner.getSelectedItem().toString())
                .apply();
    }

    private void saveSubscriptionData() {
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("wants_email", checkEmail.isChecked())
                .putBoolean("wants_sms",   checkSms.isChecked())
                .putBoolean("wants_push",  checkPush.isChecked())
                .apply();
    }

    private boolean validateStep3() {
        if (goalName.getText().toString().trim().isEmpty()) {
            goalName.setError("Enter saving goal name");
            goalName.requestFocus();
            return false;
        }
        if (targetAmount.getText().toString().trim().isEmpty()) {
            targetAmount.setError("Enter target amount");
            targetAmount.requestFocus();
            return false;
        }
        if (targetDateText.getHint() != null &&
                targetDateText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a target date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePlanData() {
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("has_saving", true)
                .putString("saving_goal_name",   goalName.getText().toString().trim())
                .putString("saving_target_amount", targetAmount.getText().toString().trim())
                .putString("saving_target_date",  targetDateText.getText().toString())
                .putString("current_savings",     currentSavings.getText().toString().trim())
                .putString("saving_frequency",    frequencySpinner.getSelectedItem().toString())
                .apply();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Go to Dashboard
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void goToDashboard() {
        Intent i = new Intent(this, DashboardActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            goToStep(currentStep - 1);
        } else {
            super.onBackPressed();
        }
    }
}