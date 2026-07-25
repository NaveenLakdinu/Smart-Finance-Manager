
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    ViewFlipper viewFlipper;
    int currentStep = 0;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userRole;

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

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get user role from Intent or SharedPreferences
        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null || userRole.isEmpty()) {
            userRole = getSharedPreferences("UserData", MODE_PRIVATE)
                    .getString("user_role", "Student");
        }

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
            if (validateStep1()) {
                saveLoanData();
                goToStep(1);
            }
        });
        ((TextView) findViewById(R.id.step1Skip)).setOnClickListener(v -> {
            clearLoanData();
            goToStep(1);
        });

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
                saveAllDataToFirestore();
            }
        });
        ((TextView) findViewById(R.id.step3Skip)).setOnClickListener(v -> {
            saveAllDataToFirestore();
        });

        updateHeader(0);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Navigate to step
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void goToStep(int step) {
        currentStep = step;
        viewFlipper.setInAnimation(this, R.anim.flip_in);
        viewFlipper.setOutAnimation(this, R.anim.flip_out);
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

        int activeColor   = getResources().getColor(android.R.color.holo_green_dark);
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
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("has_loan", true)
                .putString("loan_amount", loanAmount.getText().toString().trim())
                .putString("monthly_installment", monthlyInstallment.getText().toString().trim())
                .putString("months_paid", monthsPaid.getText().toString().trim())
                .putString("payment_method", paymentMethodSpinner.getSelectedItem().toString())
                .apply();
    }

    private void clearLoanData() {
        loanAmount.setText("");
        monthlyInstallment.setText("");
        monthsPaid.setText("");
        paymentMethodSpinner.setSelection(0);
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("has_loan", false)
                .apply();
    }

    private void saveSubscriptionData() {
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                .putBoolean("wants_email", checkEmail.isChecked())
                .putBoolean("wants_sms",   checkSms.isChecked())
                .putBoolean("wants_push",  checkPush.isChecked())
                .apply();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Validation methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private boolean validateStep1() {
        String amount = loanAmount.getText().toString().trim();
        String installment = monthlyInstallment.getText().toString().trim();
        String months = monthsPaid.getText().toString().trim();

        if (amount.isEmpty()) {
            loanAmount.setError("Enter loan amount");
            loanAmount.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            loanAmount.setError("Enter valid amount");
            loanAmount.requestFocus();
            return false;
        }

        if (installment.isEmpty()) {
            monthlyInstallment.setError("Enter monthly installment");
            monthlyInstallment.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(installment);
        } catch (NumberFormatException e) {
            monthlyInstallment.setError("Enter valid amount");
            monthlyInstallment.requestFocus();
            return false;
        }

        if (months.isEmpty()) {
            monthsPaid.setError("Enter months paid");
            monthsPaid.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(months);
        } catch (NumberFormatException e) {
            monthsPaid.setError("Enter valid number");
            monthsPaid.requestFocus();
            return false;
        }

        if (paymentMethodSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
        try {
            Double.parseDouble(targetAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            targetAmount.setError("Enter valid amount");
            targetAmount.requestFocus();
            return false;
        }
        if (targetDateText.getText().toString().trim().isEmpty() || 
            targetDateText.getText().toString().equals("Select goal deadline")) {
            Toast.makeText(this, "Please select a target date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentSavings.getText().toString().trim().isEmpty()) {
            currentSavings.setError("Enter current savings");
            currentSavings.requestFocus();
            return false;
        }
        try {
            Double.parseDouble(currentSavings.getText().toString().trim());
        } catch (NumberFormatException e) {
            currentSavings.setError("Enter valid amount");
            currentSavings.requestFocus();
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
    // Save all data to Firestore
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void saveAllDataToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        // Create batch for atomic writes
        WriteBatch batch = db.batch();
        DocumentReference userRef = db.collection("users").document(uid);

        // 1. Save loan data if exists
        boolean hasLoan = prefs.getBoolean("has_loan", false);
        if (hasLoan) {
            DocumentReference loanRef = userRef.collection("loans").document();
            Map<String, Object> loanData = new HashMap<>();
            loanData.put("loanName", "Initial Loan");
            loanData.put("principalAmount", safeParseDouble(prefs.getString("loan_amount", "0")));
            loanData.put("interestRate", 0.0);
            loanData.put("durationMonths", safeParseInt(prefs.getString("months_paid", "0")));
            loanData.put("monthlyEmi", safeParseDouble(prefs.getString("monthly_installment", "0")));
            loanData.put("paymentMethod", prefs.getString("payment_method", ""));
            loanData.put("createdAt", System.currentTimeMillis());
            batch.set(loanRef, loanData);
        }

        // 2. Save savings data if exists
        boolean hasSaving = prefs.getBoolean("has_saving", false);
        if (hasSaving) {
            DocumentReference savingRef = userRef.collection("savings").document();
            Map<String, Object> savingData = new HashMap<>();
            savingData.put("goalName", prefs.getString("saving_goal_name", ""));
            savingData.put("targetAmount", safeParseDouble(prefs.getString("saving_target_amount", "0")));
            savingData.put("currentSavings", safeParseDouble(prefs.getString("current_savings", "0")));
            savingData.put("targetDate", prefs.getString("saving_target_date", ""));
            savingData.put("frequency", prefs.getString("saving_frequency", ""));
            savingData.put("status", "Active");
            savingData.put("createdAt", System.currentTimeMillis());
            batch.set(savingRef, savingData);
        }

        // 3. Update user document with subscription preferences
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("checkEmail", prefs.getBoolean("wants_email", false));
        userUpdates.put("checkSms", prefs.getBoolean("wants_sms", false));
        userUpdates.put("checkPush", prefs.getBoolean("wants_push", false));
        batch.update(userRef, userUpdates);

        // Execute batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateByRole();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Role-based navigation
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void navigateByRole() {
        Intent intent;
        
        switch (userRole) {
            case "Company worker":
                intent = new Intent(this, WorkerDashboardActivity.class);
                break;
            case "Multiple account holder":
                intent = new Intent(this, MultiAccountDashboardActivity.class);
                break;
            case "Student":
                intent = new Intent(this, StudentDashboardActivity.class);
                break;
            case "Business owner":
                intent = new Intent(this, BusinessDashboardActivity.class);
                break;
            case "student_worker_hybrid":
                intent = new Intent(this, StudentWorkerHybridDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, DashboardActivity.class);
                break;
        }

        intent.putExtra("CURRENT_USER_ROLE", userRole);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Helper methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private double safeParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Go to Dashboard (deprecated - use navigateByRole)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    private void goToDashboard() {
        navigateByRole();
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            goToStep(currentStep - 1);
        } else {
            super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}