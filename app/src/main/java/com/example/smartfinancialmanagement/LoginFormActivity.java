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
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
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

                            // 💡 FIXED: Evaluated switch-case strings to align perfectly with DB values
                            switch (role) {
                                case "Company worker":
                                    navigateToWorkerDashboard(role);
                                    break;

                                case "Multiple account holder":
                                    navigateToMultiAccountDashboard(role);
                                    break;

                                case "Student":
                                case "Business owner":
                                default:
                                    // Route to default dashboard since specific pages are pending creation
                                    navigateToDashboard(role);
                                    break;
                            }
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