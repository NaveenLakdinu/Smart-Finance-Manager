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

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etEmail, etMobile, etPassword;
    private CheckBox loanCheckbox, termsCheckbox, subscriptionCheckbox, savingPlanCheckbox;
    private Button btnRegister;
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

        loanCheckbox = findViewById(R.id.checkLoan);
        termsCheckbox = findViewById(R.id.checkTerms);
        subscriptionCheckbox = findViewById(R.id.checkSubscription);
        savingPlanCheckbox = findViewById(R.id.checkSaving);
        btnRegister = findViewById(R.id.btnRegister);

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

        loanCheckbox.setChecked(data.hasLoan);
        termsCheckbox.setChecked(data.isTermsAccepted);
        subscriptionCheckbox.setChecked(data.receiveUpdates);
        savingPlanCheckbox.setChecked(data.hasSavingPlan);
    }

    private void registerUser() {
        saveDataToSingleton();
        UserRegistrationData data = UserRegistrationData.getInstance();

        if (!validateInputs()) {
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

                        System.out.println("Committing atomic write batch for UID: " + uid);

                        // Execute all database writes concurrently
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("✅ All base and subcollection data saved to Firestore successfully");
                                    Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();

                                    data.clearData();
                                    clearAllFields();

                                    // Handle conditional routing based on dashboard page availability
                                    navigateToDashboard(userRole);
                                })
                                .addOnFailureListener(e -> {
                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Register");
                                    System.err.println("❌ Firestore Batch Commit Error: " + e.getMessage());
                                    e.printStackTrace();
                                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");

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
     // Helper method to safely route users to their designated dashboards without crashes.
     // Fallback to primary DashboardActivity occurs if role-specific screens are not yet created.
     */
    private void navigateToDashboard(String role) {
        Intent dashboardIntent;

        // 💡 FIXED: Now perfectly matches synchronized capitalized strings coming from ChooseRoleActivity
        switch (role) {
            case "Company worker":
                dashboardIntent = new Intent(this, WorkerDashboardActivity.class);
                break;

            case "Multiple account holder":
                dashboardIntent = new Intent(this, MultiAccountDashboardActivity.class);
                break;

            case "Student":
                dashboardIntent = new Intent(this, StudentDashboardActivity.class);
                break;

            case "Business owner":
                dashboardIntent = new Intent(this, BusinessDashboardActivity.class);
                break;

            default:
                dashboardIntent = new Intent(this, DashboardActivity.class);
                break;
        }

        // Carry over the current user role explicitly to update conditional dashboard typography
        dashboardIntent.putExtra("CURRENT_USER_ROLE", role);

        // Clear activity stack completely to avoid navigation loop issues on back button press
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboardIntent);
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

        if (loanCheckbox != null) loanCheckbox.setChecked(false);
        if (termsCheckbox != null) termsCheckbox.setChecked(false);
        if (subscriptionCheckbox != null) subscriptionCheckbox.setChecked(false);
        if (savingPlanCheckbox != null) savingPlanCheckbox.setChecked(false);

        if (etFullName != null) etFullName.requestFocus();
        System.out.println("✅ All input fields cleared after registration");
    }
}