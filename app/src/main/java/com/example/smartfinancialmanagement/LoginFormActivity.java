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
            Toast.makeText(this, "Forgot Password - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkIfUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToDashboard();
        }
    }

    private void loginUser() {
        String email = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        System.out.println("Attempting login with email: " + email);

        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        System.out.println("Firebase Auth task completed");
                        
                        if (task.isSuccessful()) {
                            System.out.println("Firebase Auth successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                System.out.println("User authenticated: " + user.getEmail());
                                checkUserInFirestore(user.getUid());
                            }
                        } else {
                            System.out.println("Firebase Auth failed");
                            loginButton.setEnabled(true);
                            loginButton.setText("LOGIN");
                            String errorMessage = task.getException() != null 
                                    ? task.getException().getMessage() 
                                    : "Login failed";
                            System.out.println("Error: " + errorMessage);
                            Toast.makeText(this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        System.out.println("Auth failure listener: " + e.getMessage());
                        loginButton.setEnabled(true);
                        loginButton.setText("LOGIN");
                        Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            System.out.println("Exception during login: " + e.getMessage());
            e.printStackTrace();
            loginButton.setEnabled(true);
            loginButton.setText("LOGIN");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserInFirestore(String uid) {
        System.out.println("Checking user in Firestore with UID: " + uid);
        
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                    
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            System.out.println("User found in Firestore");
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToDashboard();
                        } else {
                            System.out.println("User not found in Firestore");
                            mAuth.signOut();
                            Toast.makeText(this, "User not found in database. Please register first.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        System.out.println("Firestore check failed: " + task.getException().getMessage());
                        mAuth.signOut();
                        Toast.makeText(this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Firestore failure: " + e.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                    mAuth.signOut();
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginFormActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
