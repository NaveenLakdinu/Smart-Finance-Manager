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
    private EditText etEditName, etEditAge, etEditMobile, etEditUniversity, etEditCourse, etEditStudentId;
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

        setupCurrencySpinner();
        loadUserData();

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

    private void loadUserData() {
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

        // Load Student Specific Info
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

    private void saveSettings() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String selectedCurrency = spinnerCurrency.getSelectedItem().toString();

        // 1. Save Currency
        CurrencyHelper.setCurrencySymbol(this, selectedCurrency);

        // 2. Profile Data
        String name = etEditName.getText().toString().trim();
        String age = etEditAge.getText().toString().trim();
        String mobile = etEditMobile.getText().toString().trim();
        String university = etEditUniversity.getText().toString().trim();
        String course = etEditCourse.getText().toString().trim();
        String studentId = etEditStudentId.getText().toString().trim();

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

        DocumentReference studentRef = db.collection("users").document(user.getUid()).collection("student_profile").document("profile_data");
        Map<String, Object> studentUpdates = new HashMap<>();
        studentUpdates.put("university", university);
        studentUpdates.put("course", course);
        studentUpdates.put("studentId", studentId);
        batch.set(studentRef, studentUpdates, com.google.firebase.firestore.SetOptions.merge());

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 3. Update Password (if provided)
                if (!newPass.isEmpty()) {
                    if (newPass.equals(confirmPass)) {
                        if (user != null) {
                            user.updatePassword(newPass)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Profile and Password updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnSaveSettings.setEnabled(true);
                                        btnSaveSettings.setText("Save Changes");
                                        Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        btnSaveSettings.setEnabled(true);
                        btnSaveSettings.setText("Save Changes");
                        etConfirmPassword.setError("Passwords do not match");
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
