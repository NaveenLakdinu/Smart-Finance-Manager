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
            Intent intent = new Intent(LoginFormActivity.this, RegisterActivity.class);
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
            // Check internet before deciding to sign out user
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
                            
                            // Get user role from Firestore
                            String role = document.getString("role");
                            if ("worker".equalsIgnoreCase(role)) {
                                navigateToWorkerDashboard();
                            } else if ("multi".equalsIgnoreCase(role)) {
                                navigateToMultiAccountDashboard();
                            } else {
                                navigateToDashboard();
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

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginFormActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToWorkerDashboard() {
        Intent intent = new Intent(LoginFormActivity.this, WorkerDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMultiAccountDashboard() {
        Intent intent = new Intent(LoginFormActivity.this, MultiAccountDashboardActivity.class);
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
