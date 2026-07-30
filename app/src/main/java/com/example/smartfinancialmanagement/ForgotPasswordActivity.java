package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * ForgotPasswordActivity - Allows users to reset their password via email
 * Uses Firebase Authentication to send password reset links
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextView tvBackToLogin;
    private com.google.android.material.button.MaterialButton btnSendReset;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Send Reset Link button
        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            
            if (validateEmail(email)) {
                sendPasswordResetEmail(email);
            }
        });

        // Back to Login link
        tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Validate email input
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    private boolean validateEmail(String email) {
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

        return true;
    }

    /**
     * Send password reset email using Firebase Auth
     * @param email User's registered email address
     */
    private void sendPasswordResetEmail(String email) {
        // Show loading indicator
        setLoadingState(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    
                    if (task.isSuccessful()) {
                        // Success - password reset email sent
                        Toast.makeText(this, 
                                "Password reset link sent to your email!", 
                                Toast.LENGTH_LONG).show();
                        
                        // Finish activity and return to login
                        finish();
                    } else {
                        // Error - show user-friendly message
                        Exception exception = task.getException();
                        String errorMessage = "Failed to send reset email";
                        
                        if (exception != null) {
                            String exceptionMessage = exception.getMessage();
                            
                            // Provide user-friendly error messages
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("no user record corresponding")) {
                                    errorMessage = "No account found with this email";
                                } else if (exceptionMessage.contains("invalid email")) {
                                    errorMessage = "Invalid email address";
                                } else if (exceptionMessage.contains("network")) {
                                    errorMessage = "Network error. Please check your connection";
                                } else {
                                    errorMessage = "Error: " + exceptionMessage;
                                }
                            }
                        }
                        
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Set loading state - show/hide progress bar and disable/enable button
     * @param isLoading true to show loading, false to hide
     */
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendReset.setEnabled(false);
            btnSendReset.setText("Sending...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendReset.setEnabled(true);
            btnSendReset.setText("Send Reset Link");
        }
    }
}
