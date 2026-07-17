package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFormActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private ImageView passwordToggle;
    private TextView signUpLink, forgotPassword;
    private com.google.android.material.button.MaterialButton loginButton;
    private ImageView googleBtn, facebookBtn, appleBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        checkIfUserLoggedIn();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordToggle = findViewById(R.id.passwordToggle);
        signUpLink = findViewById(R.id.signUpLink);
        forgotPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        googleBtn = findViewById(R.id.googleBtn);
        facebookBtn = findViewById(R.id.facebookBtn);
        appleBtn = findViewById(R.id.appleBtn);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> loginUser());

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginFormActivity.this, ChooseRoleActivity.class);
            startActivity(intent);
        });

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye);
            } else {
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_off);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordInput.setSelection(passwordInput.getText().length());
        });

        forgotPassword.setOnClickListener(v -> {
            handleForgotPassword();
        });

        googleBtn.setOnClickListener(v -> Toast.makeText(this, "Google Login - Configuration Required", Toast.LENGTH_SHORT).show());
        facebookBtn.setOnClickListener(v -> Toast.makeText(this, "Facebook Login - Configuration Required", Toast.LENGTH_SHORT).show());
        appleBtn.setOnClickListener(v -> Toast.makeText(this, "Apple Login - Configuration Required", Toast.LENGTH_SHORT).show());
    }

    private void handleForgotPassword() {
        String email = usernameInput.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserInFirestore(currentUser.getUid());
        }
    }

    private void loginUser() {
        String email = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            usernameInput.setError("Please enter email");
            usernameInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameInput.setError("Please enter a valid email address");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Please enter password");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestore(user.getUid());
                        }
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("LOGIN");
                        String errorMessage = getFriendlyErrorMessage(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserInFirestore(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            String status = document.getString("status");

                            // ──────────────────────────────────────────────────────────────
                            // 🚫 DEACTIVATED: Hard block — do not let the user in at all
                            // ──────────────────────────────────────────────────────────────
                            if ("deactivated".equalsIgnoreCase(status)) {
                                mAuth.signOut();
                                loginButton.setEnabled(true);
                                loginButton.setText("LOGIN");
                                new androidx.appcompat.app.AlertDialog.Builder(this)
                                        .setTitle("Account Deactivated")
                                        .setMessage("Your account has been permanently deactivated. " +
                                                "Please contact support if you believe this is a mistake.")
                                        .setPositiveButton("OK", null)
                                        .setCancelable(false)
                                        .show();
                                return;
                            }

                            // ──────────────────────────────────────────────────────────────
                            // ⚠️ SUSPENDED: Warning — allow login but show notice.
                            //    If suspended for ≥ 30 days → auto-deactivate and hard block.
                            // ──────────────────────────────────────────────────────────────
                            if ("suspended".equalsIgnoreCase(status)) {
                                com.google.firebase.Timestamp suspendedAt =
                                        document.getTimestamp("suspendedAt");

                                boolean autoDeactivated = false;

                                if (suspendedAt != null) {
                                    long suspendedMillis = suspendedAt.toDate().getTime();
                                    long thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000;
                                    long elapsed = System.currentTimeMillis() - suspendedMillis;

                                    if (elapsed >= thirtyDaysMillis) {
                                        // 30 days have passed — escalate to deactivated
                                        db.collection("users").document(uid)
                                                .update(
                                                        "status", "deactivated",
                                                        "deactivatedAt",
                                                        com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                );
                                        autoDeactivated = true;
                                    }
                                }

                                if (autoDeactivated) {
                                    // Show deactivation message after auto-escalation
                                    mAuth.signOut();
                                    loginButton.setEnabled(true);
                                    loginButton.setText("LOGIN");
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("Account Deactivated")
                                            .setMessage("Your account has been deactivated because " +
                                                    "it remained suspended for more than 30 days. " +
                                                    "Please contact support for assistance.")
                                            .setPositiveButton("OK", null)
                                            .setCancelable(false)
                                            .show();
                                    return;
                                } else {
                                    // Still within the 30-day window — warn but let them in
                                    // Store role first, then show warning on top of dashboard
                                    String role = document.getString("role");
                                    if (role == null) role = "Student";
                                    final String finalRole = role;

                                    getSharedPreferences("UserData", MODE_PRIVATE)
                                            .edit()
                                            .putString("user_role", role)
                                            .apply();

                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("⚠️ Account Suspended")
                                            .setMessage("Your account has been suspended due to " +
                                                    "not following the agreement. You can still " +
                                                    "access the app, but your account may be " +
                                                    "permanently deactivated if this is not resolved.")
                                            .setPositiveButton("I Understand", (dialog, which) -> {
                                                navigateByRole(finalRole);
                                            })
                                            .setCancelable(false)
                                            .show();
                                    return; // Navigation handled inside dialog callback
                                }
                            }

                            // ──────────────────────────────────────────────────────────────
                            // ✅ ACTIVE (or no status field): Normal login flow
                            // ──────────────────────────────────────────────────────────────
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // 💡 FIXED: Read the role and check against synchronized capitalized strings
                            String role = document.getString("role");
                            System.out.println("✅ Login Role fetched from DB: " + role);

                            if (role == null) {
                                role = "Student"; // Default fallback
                            }

                            // Update local SharedPreferences state upon successful login fetch
                            getSharedPreferences("UserData", MODE_PRIVATE)
                                    .edit()
                                    .putString("user_role", role)
                                    .apply();

                            navigateByRole(role);
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this, "User profile not found. Please register.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (loginButton.isEnabled()) {
                            Toast.makeText(this, "Database check failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 💡 FIXED: Modified navigation methods to pass current user role as Intent extra to prevent dashboard state breaks
    private void navigateToDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, DashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToWorkerDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, WorkerDashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMultiAccountDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, MultiAccountDashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // 💡LoginFormActivity එකේ අගටම මේ මෙතඩ් දෙක පේස්ට් කරන්න:
    private void navigateToStudentDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, StudentDashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToBusinessDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, BusinessDashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHybridDashboard(String role) {
        Intent intent = new Intent(LoginFormActivity.this, StudentWorkerHybridDashboardActivity.class);
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    /**
     * Routes the user to the correct dashboard based on their Firestore role string.
     * Single source of truth for all role-based navigation in this Activity.
     */
    private void navigateByRole(String role) {
        // Show success toast only for normal (non-suspended) logins
        switch (role) {
            case "Company worker":
                navigateToWorkerDashboard(role);
                break;
            case "Multiple account holder":
                navigateToMultiAccountDashboard(role);
                break;
            case "Student":
                navigateToStudentDashboard(role);
                break;
            case "Business owner":
                navigateToBusinessDashboard(role);
                break;
            case "student_worker_hybrid":
                navigateToHybridDashboard(role);
                break;
            default:
                navigateToDashboard(role);
                break;
        }
    }

    /**
     * Converts raw Firebase Auth exception messages into user-friendly strings.
     */

    private String getFriendlyErrorMessage(Exception e) {
        if (e == null) return "Login failed. Please try again.";
        String msg = e.getMessage();
        if (msg == null) return "Login failed. Please try again.";
        if (msg.contains("no user record") || msg.contains("user-not-found") || msg.contains("There is no user"))
            return "No account found with this email. Please sign up first.";
        if (msg.contains("password is invalid") || msg.contains("wrong-password") || msg.contains("The password is invalid"))
            return "Incorrect password. Please try again.";
        if (msg.contains("email address is badly formatted"))
            return "Please enter a valid email address.";
        if (msg.contains("too many requests") || msg.contains("too-many-requests"))
            return "Too many failed attempts. Please try again later.";
        if (msg.contains("network") || msg.contains("Network"))
            return "Network error. Please check your internet connection.";
        return "Login failed. Please check your credentials.";
    }
}