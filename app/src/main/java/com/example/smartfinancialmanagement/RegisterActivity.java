package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

        // 2. CheckBox Click Listeners (Loop එක වැළැක්වීමට ClickListener පාවිච්චි කරමු)
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
        data.fullName = etFullName.getText().toString();
        data.age = etAge.getText().toString();
        data.email = etEmail.getText().toString();
        data.mobile = etMobile.getText().toString();
        data.password = etPassword.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // වෙනත් පිටුවල සිට එන විට දත්ත ආපසු පිරවීම
        UserRegistrationData data = UserRegistrationData.getInstance();

        etFullName.setText(data.fullName);
        etAge.setText(data.age);
        etEmail.setText(data.email);
        etMobile.setText(data.mobile);
        etPassword.setText(data.password);

        // UI එක update කිරීමේදී Listener එක trigger නොවන ලෙස OnClickListener හොඳින් ක්‍රියා කරයි
        loanCheckbox.setChecked(data.hasLoan);
        termsCheckbox.setChecked(data.isTermsAccepted);
        subscriptionCheckbox.setChecked(data.receiveUpdates);
        savingPlanCheckbox.setChecked(data.hasSavingPlan);
    }

    private void registerUser() {
        saveDataToSingleton(); // අන්තිම මොහොතේත් දත්ත සේව් කරගනිමු
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

        System.out.println("Starting registration for email: " + email);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ Firebase Auth successful");
                        String uid = mAuth.getCurrentUser().getUid();
                        System.out.println("User UID: " + uid);

                        // Firestore එකට ඔක්කොම දත්ත ටික සේව් කිරීම
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("name", data.fullName);
                        userMap.put("age", data.age);
                        userMap.put("email", data.email);
                        userMap.put("mobile", data.mobile);
                        userMap.put("timestamp", System.currentTimeMillis());

                        // Loan Details
                        userMap.put("hasLoan", data.hasLoan);
                        userMap.put("loanAmount", data.loanAmount != null ? data.loanAmount : "");
                        userMap.put("loanType", data.loanType != null ? data.loanType : "");

                        // Saving Plan Details
                        userMap.put("hasSavingPlan", data.hasSavingPlan);
                        userMap.put("savingGoalName", data.goalName != null ? data.goalName : "");
                        userMap.put("savingTargetAmount", data.targetAmount != null ? data.targetAmount : "");
                        userMap.put("savingTargetDate", data.targetDate != null ? data.targetDate : "");
                        userMap.put("monthlySavingAmount", data.monthlySavingAmount != null ? data.monthlySavingAmount : "");

                        // Extras
                        userMap.put("receiveUpdates", data.receiveUpdates);

                        System.out.println("Saving to Firestore with UID: " + uid);
                        System.out.println("User data: " + userMap.toString());

                        // Use UID as document ID
                        db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("✅ Data saved to Firestore successfully");
                                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();
                                    data.clearData();
                                    clearAllFields();
                                    // TODO: Navigate to Dashboard
                                    // startActivity(new Intent(this, DashboardActivity.class));
                                    // finish();
                                })
                                .addOnFailureListener(e -> {
                                    System.err.println("❌ Firestore Error: " + e.getMessage());
                                    e.printStackTrace();
                                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        System.err.println("❌ Auth Error: " + task.getException().getMessage());
                        task.getException().printStackTrace();
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
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