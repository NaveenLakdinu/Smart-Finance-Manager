package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class AccountSettingsActivity extends AppCompatActivity {

    private Spinner spinnerCurrency;
    private EditText etNewPassword, etConfirmPassword;
    private EditText etEditName, etEditAge, etEditMobile, etEditUniversity, etEditCourse, etEditStudentId, etEditMonthlyIncome;
    private android.widget.LinearLayout llStudentInfo, llWorkerInfo;
    private MaterialButton btnSaveSettings;
    
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private final String[] currencies = {"LKR", "$", "€", "£", "₹"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        etEditName = findViewById(R.id.etEditName);
        etEditAge = findViewById(R.id.etEditAge);
        etEditMobile = findViewById(R.id.etEditMobile);
        etEditUniversity = findViewById(R.id.etEditUniversity);
        etEditCourse = findViewById(R.id.etEditCourse);
        etEditStudentId = findViewById(R.id.etEditStudentId);
        etEditMonthlyIncome = findViewById(R.id.etEditMonthlyIncome);
        llStudentInfo = findViewById(R.id.llStudentInfo);
        llWorkerInfo = findViewById(R.id.llWorkerInfo);

        String role = getSharedPreferences("UserData", MODE_PRIVATE).getString("user_role", "Student");
        if (role.equals("Company worker") || role.equals("Multiple account holder")) {
            if (llWorkerInfo != null) llWorkerInfo.setVisibility(android.view.View.VISIBLE);
        } else if (role.equals("Business owner")) {
            // Leave both hidden
        } else {
            if (llStudentInfo != null) llStudentInfo.setVisibility(android.view.View.VISIBLE);
        }

        setupCurrencySpinner();
        loadUserData(role);

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        String currentCurrency = CurrencyHelper.getCurrencySymbol(this);
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currentCurrency)) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }
    }

    private void loadUserData(String role) {
        if (user == null) return;
        String uid = user.getUid();

        // Load Base Info
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("name")) etEditName.setText(documentSnapshot.getString("name"));
                        if (documentSnapshot.contains("age")) etEditAge.setText(documentSnapshot.getString("age"));
                        if (documentSnapshot.contains("mobile")) etEditMobile.setText(documentSnapshot.getString("mobile"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load base info", Toast.LENGTH_SHORT).show());

        // Load Specific Info
        if (role.equals("Company worker")) {
            db.collection("users").document(uid).collection("worker_profile").document("profile_data").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("monthlySalary")) {
                                Double sal = documentSnapshot.getDouble("monthlySalary");
                                if (sal != null) etEditMonthlyIncome.setText(String.valueOf(sal));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load worker info", Toast.LENGTH_SHORT).show());
        } else if (role.equals("Multiple account holder")) {
            db.collection("users").document(uid).collection("multi_profile").document("profile_data").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("monthlySalary")) {
                                Double sal = documentSnapshot.getDouble("monthlySalary");
                                if (sal != null) etEditMonthlyIncome.setText(String.valueOf(sal));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load multi account info", Toast.LENGTH_SHORT).show());
        } else if (role.equals("Business owner")) {
            // Nothing extra to load
        } else {
            db.collection("users").document(uid).collection("student_profile").document("profile_data").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("university")) etEditUniversity.setText(documentSnapshot.getString("university"));
                            if (documentSnapshot.contains("course")) etEditCourse.setText(documentSnapshot.getString("course"));
                            if (documentSnapshot.contains("studentId")) etEditStudentId.setText(documentSnapshot.getString("studentId"));
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load student info", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveSettings() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                etNewPassword.setError("Password must be at least 6 characters");
                etNewPassword.requestFocus();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
            
            if (PinHelper.isPinSet(this)) {
                showPinConfirmationDialog(newPass);
            } else {
                performSave(newPass);
            }
        } else {
            performSave(newPass);
        }
    }

    private void showPinConfirmationDialog(String newPass) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Confirm PIN");
        builder.setMessage("Please enter your PIN to authorize the password change.");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setBackgroundResource(R.drawable.bg_input_dark);
        input.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setPadding(50, 20, 50, 0);
        layout.addView(input, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        builder.setView(layout);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredPin = input.getText().toString();
            if (PinHelper.verifyPin(this, enteredPin)) {
                performSave(newPass);
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void promptForOldPasswordAndReauthenticate(String newPass) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Session Expired");
        builder.setMessage("Please enter your current password to re-authenticate.");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setBackgroundResource(R.drawable.bg_input_dark);
        input.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setPadding(50, 20, 50, 0);
        layout.addView(input, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        builder.setView(layout);

        builder.setPositiveButton("Re-authenticate", (dialog, which) -> {
            String oldPass = input.getText().toString();
            if (user != null && user.getEmail() != null) {
                com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), oldPass);
                user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPass).addOnSuccessListener(aVoid2 -> {
                        Toast.makeText(this, "Profile and Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Re-authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void performSave(String newPass) {
        String selectedCurrency = spinnerCurrency.getSelectedItem().toString();
        CurrencyHelper.setCurrencySymbol(this, selectedCurrency);

        String name = etEditName.getText().toString().trim();
        String age = etEditAge.getText().toString().trim();
        String mobile = etEditMobile.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Name, Age and Mobile are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveSettings.setEnabled(false);
        btnSaveSettings.setText("Saving...");

        WriteBatch batch = db.batch();

        DocumentReference userRef = db.collection("users").document(user.getUid());
        Map<String, Object> baseUpdates = new HashMap<>();
        baseUpdates.put("name", name);
        baseUpdates.put("age", age);
        baseUpdates.put("mobile", mobile);
        batch.update(userRef, baseUpdates);

        String role = getSharedPreferences("UserData", MODE_PRIVATE).getString("user_role", "Student");
        
        if (role.equals("Company worker")) {
            String incomeStr = etEditMonthlyIncome.getText().toString().trim();
            double income = incomeStr.isEmpty() ? 0.0 : Double.parseDouble(incomeStr);
            DocumentReference workerRef = db.collection("users").document(user.getUid()).collection("worker_profile").document("profile_data");
            Map<String, Object> workerUpdates = new HashMap<>();
            workerUpdates.put("monthlySalary", income);
            batch.set(workerRef, workerUpdates, com.google.firebase.firestore.SetOptions.merge());
        } else if (role.equals("Multiple account holder")) {
            String incomeStr = etEditMonthlyIncome.getText().toString().trim();
            double income = incomeStr.isEmpty() ? 0.0 : Double.parseDouble(incomeStr);
            DocumentReference multiRef = db.collection("users").document(user.getUid()).collection("multi_profile").document("profile_data");
            Map<String, Object> multiUpdates = new HashMap<>();
            multiUpdates.put("monthlySalary", income);
            batch.set(multiRef, multiUpdates, com.google.firebase.firestore.SetOptions.merge());
        } else if (!role.equals("Business owner")) {
            String university = etEditUniversity.getText().toString().trim();
            String course = etEditCourse.getText().toString().trim();
            String studentId = etEditStudentId.getText().toString().trim();
            DocumentReference studentRef = db.collection("users").document(user.getUid()).collection("student_profile").document("profile_data");
            Map<String, Object> studentUpdates = new HashMap<>();
            studentUpdates.put("university", university);
            studentUpdates.put("course", course);
            studentUpdates.put("studentId", studentId);
            batch.set(studentRef, studentUpdates, com.google.firebase.firestore.SetOptions.merge());
        }

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!newPass.isEmpty()) {
                    if (user != null) {
                        user.updatePassword(newPass)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Profile and Password updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnSaveSettings.setEnabled(true);
                                    btnSaveSettings.setText("Save Changes");
                                    if (e.getMessage() != null && (e.getMessage().contains("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") || e.getMessage().contains("recent authentication"))) {
                                        promptForOldPasswordAndReauthenticate(newPass);
                                    } else {
                                        Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(this, "Settings updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                btnSaveSettings.setEnabled(true);
                btnSaveSettings.setText("Save Changes");
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
