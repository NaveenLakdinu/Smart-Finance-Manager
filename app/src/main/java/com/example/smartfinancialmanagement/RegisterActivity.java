package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etEmail, etMobile, etPassword;
    private CheckBox termsCheckbox;
    private Button btnRegister;
    private android.widget.ProgressBar progressBar;
    private ImageView passwordToggle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variable to store the passed user role from UserRoleActivity
    private String userRole;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 💡 FIXED: Try to retrieve from Intent extra first
        userRole = getIntent().getStringExtra("USER_ROLE");

        // 💡 FIXED: Robust fallback fallback to SharedPreferences if intent extra was lost during activity state shifts
        if (userRole == null || userRole.isEmpty()) {
            userRole = getSharedPreferences("UserData", MODE_PRIVATE)
                    .getString("user_role", "Student");
        }

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
        System.out.println("✅ Current User Role finalized for DB entry: " + userRole);

        // 1. UI Views Initialize
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        passwordToggle = findViewById(R.id.passwordToggle);

        etFullName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String fullName = s.toString();
                if (!fullName.isEmpty() && !fullName.matches("^[a-zA-Z\\s]+$")) {
                    etFullName.setError("Please enter a valid name (letters and spaces only)");
                } else {
                    etFullName.setError(null);
                }
            }
        });

        termsCheckbox = findViewById(R.id.checkTerms);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // 2. Password Toggle Listener
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye);
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_off);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        // 3. CheckBox Click Listeners
        termsCheckbox.setOnClickListener(v -> {
            saveDataToSingleton();
            if (termsCheckbox.isChecked()) {
                startActivity(new Intent(RegisterActivity.this, TermsActivity.class));
            } else {
                UserRegistrationData.getInstance().isTermsAccepted = false;
            }
        });

        // 3. Register Button Click
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void saveDataToSingleton() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        data.fullName = etFullName.getText().toString().trim();
        data.age = etAge.getText().toString().trim();
        data.email = etEmail.getText().toString().trim();
        data.mobile = etMobile.getText().toString().trim();
        data.password = etPassword.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();

        UserRegistrationData data = UserRegistrationData.getInstance();

        etFullName.setText(data.fullName);
        etAge.setText(data.age);
        etEmail.setText(data.email);
        etMobile.setText(data.mobile);
        etPassword.setText(data.password);

        termsCheckbox.setChecked(data.isTermsAccepted);
    }

    private void registerUser() {
        if (mAuth == null || db == null) {
            Toast.makeText(this, "Firebase not initialized. Please restart the app.", Toast.LENGTH_LONG).show();
            return;
        }
        saveDataToSingleton();
        UserRegistrationData data = UserRegistrationData.getInstance();

        if (!validateInputs()) {
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. UI Loading State
        setLoadingState(true);

        System.out.println("Starting registration for email: " + email);

        // Add a timeout to catch network hangs (e.g. 20 seconds)
        final android.os.Handler timeoutHandler = new android.os.Handler();
        final Runnable timeoutRunnable = () -> {
            if (!isFinishing() && !btnRegister.isEnabled()) {
                setLoadingState(false);
                Toast.makeText(this, "Registration is taking longer than expected. Please check your internet.", Toast.LENGTH_LONG).show();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 20000);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("✅ Firebase Auth successful");
                        
                        try {
                            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                            
                            if (uid == null) {
                                throw new Exception("Failed to retrieve user ID after successful auth");
                            }

                            System.out.println("User UID: " + uid);

                            // Use WriteBatch for atomic data persistence across multiple collections/documents
                            WriteBatch batch = db.batch();

                            // Reference for the main Shared Base Profile document
                            DocumentReference userRef = db.collection("users").document(uid);

                            // 1. Populate Shared Base Document Data
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("name", data.fullName);
                            userMap.put("age", data.age);
                            userMap.put("email", data.email);
                            userMap.put("mobile", data.mobile);
                            userMap.put("role", userRole); // Saving the correctly synchronized role string
                            userMap.put("timestamp", System.currentTimeMillis());
                            userMap.put("status", "Active");
                            userMap.put("fcmToken", "");

                            // Loan Details Summary
                            userMap.put("hasLoan", data.hasLoan);
                            userMap.put("loanAmount", data.loanAmount != null ? data.loanAmount : "");
                            userMap.put("monthlyInstallment", data.monthlyInstallment != null ? data.monthlyInstallment : "");
                            userMap.put("monthsPaid", data.monthsPaid != null ? data.monthsPaid : "");
                            userMap.put("paymentMethod", data.paymentMethod != null ? data.paymentMethod : "");

                            // Saving Plan Details Summary
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

                            // Enqueue the root user document write operation
                            batch.set(userRef, userMap);

                            // 2. Setup Role-Specific Profiles via Subcollections (Pattern 1)
                            Map<String, Object> roleSpecificProfile = new HashMap<>();
                            String subcollectionName = "";

                            switch (userRole) {
                                case "Student":
                                    subcollectionName = "student_profile";
                                    roleSpecificProfile.put("university", "");
                                    roleSpecificProfile.put("course", "");
                                    roleSpecificProfile.put("studentId", "");
                                    break;

                                case "Company worker":
                                    subcollectionName = "worker_profile";
                                    roleSpecificProfile.put("companyName", "");
                                    roleSpecificProfile.put("designation", "");
                                    roleSpecificProfile.put("monthlySalary", 0.0);
                                    break;

                                case "Business owner":
                                    subcollectionName = "business_profile";
                                    roleSpecificProfile.put("businessName", "");
                                    roleSpecificProfile.put("regNumber", "");
                                    roleSpecificProfile.put("industryType", "");
                                    break;

                                case "Multiple account holder":
                                    subcollectionName = "multi_profile";
                                    roleSpecificProfile.put("linkedAccountsCount", 1);
                                    roleSpecificProfile.put("primaryWorkspace", "");
                                    break;
                            }

                            // Enqueue the subcollection profile document if valid subcollection exists
                            if (!subcollectionName.isEmpty()) {
                                DocumentReference roleRef = db.collection("users").document(uid)
                                        .collection(subcollectionName).document("profile_data");
                                batch.set(roleRef, roleSpecificProfile);
                            }

                            // 3. Save loan subcollection independently if checkbox is checked
                            if (data.hasLoan) {
                                DocumentReference loanRef = db.collection("users").document(uid).collection("loans").document();
                                Map<String, Object> loanData = new HashMap<>();
                                loanData.put("loanName", "Initial Loan");
                                loanData.put("principalAmount", safeParseDouble(data.loanAmount));
                                loanData.put("interestRate", 0.0);
                                loanData.put("durationMonths", safeParseInt(data.monthsPaid));
                                loanData.put("monthlyEmi", safeParseDouble(data.monthlyInstallment));
                                loanData.put("createdAt", System.currentTimeMillis());
                                batch.set(loanRef, loanData);
                            }

                            // 4. Save initial saving plan to Firestore if checked
                            if (data.hasSavingPlan) {
                                DocumentReference savingRef = db.collection("users").document(uid).collection("savings").document();
                                String savingId = savingRef.getId();
                                
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                String startDate = sdf.format(new Date()); // today
                                String targetDate = data.targetDate != null ? data.targetDate : startDate;
                                
                                SavingModel savingModel = new SavingModel(
                                        savingId,
                                        data.goalName != null && !data.goalName.isEmpty() ? data.goalName : "Initial Saving Goal",
                                        safeParseDouble(data.targetAmount),
                                        safeParseDouble(data.currentSavings),
                                        safeParseDouble(data.monthlySavingAmount),
                                        startDate,
                                        targetDate,
                                        "Active",
                                        System.currentTimeMillis()
                                );
                                
                                batch.set(savingRef, savingModel);
                            }

                            System.out.println("Committing atomic write batch for UID: " + uid);

                            // Execute all database writes concurrently
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                        setLoadingState(false);
                                        System.out.println("✅ All base and subcollection data saved to Firestore successfully");
                                        Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();

                                        data.clearData();

                                        // Navigate to OnboardingActivity after successful registration
                                        Intent onboardingIntent = new Intent(RegisterActivity.this, OnboardingActivity.class);
                                        onboardingIntent.putExtra("USER_ROLE", userRole);
                                        onboardingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(onboardingIntent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                        setLoadingState(false);
                                        System.err.println("❌ Firestore Batch Commit Error: " + e.getMessage());
                                        e.printStackTrace();
                                        
                                        // Rollback: Delete the Auth user if Firestore save fails
                                        if (mAuth.getCurrentUser() != null) {
                                            mAuth.getCurrentUser().delete();
                                        }
                                        
                                        Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                                    
                        } catch (Exception e) {
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            setLoadingState(false);
                            System.err.println("❌ Logic Error during registration: " + e.getMessage());
                            e.printStackTrace();
                            
                            // Rollback: Delete the Auth user if an exception occurs
                            if (mAuth.getCurrentUser() != null) {
                                mAuth.getCurrentUser().delete();
                            }
                            
                            Toast.makeText(this, "Internal Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        
                    } else {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        setLoadingState(false);

                        Exception e = task.getException();
                        String errorMessage = e != null ? e.getMessage() : "Unknown error";

                        if (errorMessage != null && errorMessage.contains("already in use")) {
                            errorMessage = "This email is already registered. Please Login instead.";
                        }

                        System.err.println("❌ Auth Error: " + errorMessage);
                        Toast.makeText(this, "Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            btnRegister.setEnabled(false);
            btnRegister.setVisibility(android.view.View.INVISIBLE);
            if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setVisibility(android.view.View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
        }
    }

    private boolean validateInputs() {
        String fullName = etFullName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name is required");
            etFullName.requestFocus();
            return false;
        }

        if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            etFullName.setError("Please enter a valid name (letters and spaces only)");
            etFullName.requestFocus();
            return false;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age < 1 || age > 120) {
                etAge.setError("Please enter a valid age (1-120)");
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid numeric age");
            etAge.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (mobile.isEmpty()) {
            etMobile.setError("Mobile number is required");
            etMobile.requestFocus();
            return false;
        }

        if (mobile.length() < 10) {
            etMobile.setError("Please enter a valid mobile number (min 10 digits)");
            etMobile.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Helper method to route users correctly after registration.
     * This method is deprecated - use OnboardingActivity instead.
     */
    @Deprecated
    private void navigateToDashboard(String role) {
        // This method is no longer used - users go through OnboardingActivity first
        Intent onboardingIntent = new Intent(this, OnboardingActivity.class);
        onboardingIntent.putExtra("USER_ROLE", role);
        onboardingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(onboardingIntent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
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

    private void clearAllFields() {
        if (etFullName != null) etFullName.setText("");
        if (etAge != null) etAge.setText("");
        if (etEmail != null) etEmail.setText("");
        if (etMobile != null) etMobile.setText("");
        if (etPassword != null) etPassword.setText("");

        if (termsCheckbox != null) termsCheckbox.setChecked(false);

        if (etFullName != null) etFullName.requestFocus();
        System.out.println("✅ All input fields cleared after registration");
    }
}