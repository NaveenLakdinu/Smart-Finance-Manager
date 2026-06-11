package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etEmail, etMobile, etPassword;
    private CheckBox loanCheckbox, termsCheckbox, subscriptionCheckbox, savingPlanCheckbox;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Verify Firebase initialization
        if (mAuth == null) {
            System.err.println("❌ Firebase Auth not initialized");
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show();
        }
        if (db == null) {
            System.err.println("❌ Firestore not initialized");
            Toast.makeText(this, "Database initialization failed", Toast.LENGTH_LONG).show();
        }
        
        System.out.println("✅ Firebase Auth initialized: " + (mAuth != null));
        System.out.println("✅ Firestore initialized: " + (db != null));

        // 1. UI Views Initialize
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);

        loanCheckbox = findViewById(R.id.checkLoan);
        termsCheckbox = findViewById(R.id.checkTerms);
        subscriptionCheckbox = findViewById(R.id.checkSubscription);
        savingPlanCheckbox = findViewById(R.id.checkSaving);
        btnRegister = findViewById(R.id.btnRegister);

        // 2. CheckBox Click Listeners ( ClickListener use loop stop)
        loanCheckbox.setOnClickListener(v -> {
            saveDataToSingleton();
            if (loanCheckbox.isChecked()) {
                startActivity(new Intent(RegisterActivity.this, LoanDetailsActivity.class));
            } else {
                UserRegistrationData.getInstance().hasLoan = false;
            }
        });

        termsCheckbox.setOnClickListener(v -> {
            saveDataToSingleton();
            if (termsCheckbox.isChecked()) {
                startActivity(new Intent(RegisterActivity.this, TermsActivity.class));
            } else {
                UserRegistrationData.getInstance().isTermsAccepted = false;
            }
        });

        subscriptionCheckbox.setOnClickListener(v -> {
            saveDataToSingleton();
            if (subscriptionCheckbox.isChecked()) {
                startActivity(new Intent(RegisterActivity.this, SubscriptionActivity.class));
            } else {
                UserRegistrationData.getInstance().receiveUpdates = false;
            }
        });

        savingPlanCheckbox.setOnClickListener(v -> {
            saveDataToSingleton();
            if (savingPlanCheckbox.isChecked()) {
                startActivity(new Intent(RegisterActivity.this, SavingPlanActivity.class));
            } else {
                UserRegistrationData.getInstance().hasSavingPlan = false;
            }
        });

        // 3. Register Button Click
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void saveDataToSingleton() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        data.fullName = etFullName.getText().toString().trim();
        data.age = etAge.getText().toString().trim();
        data.email = etEmail.getText().toString().trim();   // trim to match Firebase Auth email
        data.mobile = etMobile.getText().toString().trim();
        data.password = etPassword.getText().toString();    // do NOT trim passwords
    }

    @Override
    protected void onResume() {
        super.onResume();

        //back to other pages come show data again

        UserRegistrationData data = UserRegistrationData.getInstance();

        etFullName.setText(data.fullName);
        etAge.setText(data.age);
        etEmail.setText(data.email);
        etMobile.setText(data.mobile);
        etPassword.setText(data.password);

        // UI එක update karanakota Listener එක trigger venne nethuva OnClickListener
        loanCheckbox.setChecked(data.hasLoan);
        termsCheckbox.setChecked(data.isTermsAccepted);
        subscriptionCheckbox.setChecked(data.receiveUpdates);
        savingPlanCheckbox.setChecked(data.hasSavingPlan);
    }

    private void registerUser() {
        saveDataToSingleton(); // last movemt save data
        UserRegistrationData data = UserRegistrationData.getInstance();

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!data.isTermsAccepted) {
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-clicks
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        System.out.println("Starting registration for email: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ Firebase Auth successful");
                        String uid = mAuth.getCurrentUser().getUid();
                        System.out.println("User UID: " + uid);

                        // Use WriteBatch for atomic data persistence
                        WriteBatch batch = db.batch();
                        DocumentReference userRef = db.collection("users").document(uid);

                        // Firestore save all data to user profile
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("name", data.fullName);
                        userMap.put("age", data.age);
                        userMap.put("email", data.email);
                        userMap.put("mobile", data.mobile);
                        userMap.put("timestamp", System.currentTimeMillis());

                        // Get user role from SharedPreferences (set in ChooseRoleActivity)
                        String role = getSharedPreferences("UserData", MODE_PRIVATE).getString("user_role", "student");
                        userMap.put("role", role);

                        // Loan Details (Summary in profile)
                        userMap.put("hasLoan", data.hasLoan);
                        userMap.put("loanAmount", data.loanAmount != null ? data.loanAmount : "");
                        userMap.put("monthlyInstallment", data.monthlyInstallment != null ? data.monthlyInstallment : "");
                        userMap.put("monthsPaid", data.monthsPaid != null ? data.monthsPaid : "");
                        userMap.put("paymentMethod", data.paymentMethod != null ? data.paymentMethod : "");

                        // Saving Plan Details (Summary in profile)
                        userMap.put("hasSavingPlan", data.hasSavingPlan);
                        userMap.put("savingGoalName", data.goalName != null ? data.goalName : "");
                        userMap.put("savingTargetAmount", data.targetAmount != null ? data.targetAmount : "");
                        userMap.put("savingTargetDate", data.targetDate != null ? data.targetDate : "");
                        userMap.put("monthlySavingAmount", data.monthlySavingAmount != null ? data.monthlySavingAmount : "");
                        userMap.put("currentSavings", data.currentSavings != null ? data.currentSavings : "");

                        // Extras
                        userMap.put("receiveUpdates", data.receiveUpdates);
                        userMap.put("checkEmail", data.checkEmail);
                        userMap.put("checkSms", data.checkSms);
                        userMap.put("checkPush", data.checkPush);
                        userMap.put("checkReport", data.checkReport);
                        userMap.put("checkPromo", data.checkPromo);

                        batch.set(userRef, userMap);

                        // ✅ CRITICAL FIX: Save loan to sub-collection so it shows in Loan Manager
                        if (data.hasLoan) {
                            DocumentReference loanRef = db.collection("users").document(uid).collection("loans").document();
                            Map<String, Object> loanData = new HashMap<>();
                            loanData.put("loanName", "Initial Loan");
                            loanData.put("principalAmount", safeParseDouble(data.loanAmount));
                            loanData.put("interestRate", 0.0); // Default
                            loanData.put("durationMonths", safeParseInt(data.monthsPaid)); 
                            loanData.put("monthlyEmi", safeParseDouble(data.monthlyInstallment));
                            loanData.put("createdAt", System.currentTimeMillis());
                            batch.set(loanRef, loanData);
                        }

                        System.out.println("Committing batch for UID: " + uid);

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("✅ All data saved to Firestore successfully");
                                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();
                                    data.clearData();
                                    clearAllFields();
                                    
                                    // Navigate to correct dashboard based on role
                                    Intent intent;
                                    if ("worker".equalsIgnoreCase(role)) {
                                        intent = new Intent(this, WorkerDashboardActivity.class);
                                    } else if ("multi".equalsIgnoreCase(role)) {
                                        intent = new Intent(this, MultiAccountDashboardActivity.class);
                                    } else {
                                        intent = new Intent(this, DashboardActivity.class);
                                    }
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Register");
                                    System.err.println("❌ Firestore Error: " + e.getMessage());
                                    e.printStackTrace();
                                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        
                        Exception e = task.getException();
                        String errorMessage = e != null ? e.getMessage() : "Unknown error";
                        
                        // Check specifically for "Email already in use"
                        if (errorMessage != null && errorMessage.contains("already in use")) {
                            errorMessage = "This email is already registered. Please Login instead.";
                        }
                        
                        System.err.println("❌ Auth Error: " + errorMessage);
                        Toast.makeText(this, "Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

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

    // Method to clear all input fields after successful registration
    private void clearAllFields() {
        // Clear EditText fields
        if (etFullName != null) etFullName.setText("");
        if (etAge != null) etAge.setText("");
        if (etEmail != null) etEmail.setText("");
        if (etMobile != null) etMobile.setText("");
        if (etPassword != null) etPassword.setText("");

        // Uncheck all checkboxes
        if (loanCheckbox != null) loanCheckbox.setChecked(false);
        if (termsCheckbox != null) termsCheckbox.setChecked(false);
        if (subscriptionCheckbox != null) subscriptionCheckbox.setChecked(false);
        if (savingPlanCheckbox != null) savingPlanCheckbox.setChecked(false);

        // Focus on first field for new registration
        if (etFullName != null) etFullName.requestFocus();

        System.out.println("✅ All input fields cleared after registration");
    }
}