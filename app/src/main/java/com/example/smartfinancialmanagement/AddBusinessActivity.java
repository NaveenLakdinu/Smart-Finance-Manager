package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddBusinessActivity extends AppCompatActivity {

    private EditText etBusinessName, etBusinessCategory, etBusinessPhone, etBusinessEmail;
    private MaterialButton btnSaveBusiness;
    private ImageView btnBack;
    private FirebaseFirestore db;

    private boolean isUpdateMode = false;
    private String updateDocId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);

        db = FirebaseFirestore.getInstance();

        etBusinessName = findViewById(R.id.etBusinessName);
        etBusinessCategory = findViewById(R.id.etBusinessCategory);
        etBusinessPhone = findViewById(R.id.etBusinessPhone);
        etBusinessEmail = findViewById(R.id.etBusinessEmail);
        btnSaveBusiness = findViewById(R.id.btnSaveBusiness);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (getIntent() != null && getIntent().getBooleanExtra("IS_UPDATE_MODE", false)) {
            isUpdateMode = true;
            updateDocId = getIntent().getStringExtra("BIZ_ID");

            etBusinessName.setText(getIntent().getStringExtra("BIZ_NAME"));
            etBusinessCategory.setText(getIntent().getStringExtra("BIZ_CATEGORY"));
            etBusinessPhone.setText(getIntent().getStringExtra("BIZ_PHONE"));
            etBusinessEmail.setText(getIntent().getStringExtra("BIZ_EMAIL"));

            btnSaveBusiness.setText("Update Workspace Entity");
        }

        btnSaveBusiness.setOnClickListener(v -> handleBusinessSubmission());
    }

    private void handleBusinessSubmission() {
        String bizName = etBusinessName.getText().toString().trim();
        String bizCategory = etBusinessCategory.getText().toString().trim();
        String bizPhone = etBusinessPhone.getText().toString().trim();
        String bizEmail = etBusinessEmail.getText().toString().trim();

        // 1. Check Authentication Session
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Client-Side Validations with Focus & Error Hints
        if (TextUtils.isEmpty(bizName)) {
            etBusinessName.setError("Business Name is required");
            etBusinessName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(bizCategory)) {
            etBusinessCategory.setError("Business Category is required");
            etBusinessCategory.requestFocus();
            return;
        }

        // Phone Validation (Allows international formats like +94... or local digits 9 to 15 digits)
        if (TextUtils.isEmpty(bizPhone)) {
            etBusinessPhone.setError("Phone number is required");
            etBusinessPhone.requestFocus();
            return;
        } else if (!bizPhone.matches("^[+]?[0-9]{9,15}$")) {
            etBusinessPhone.setError("Enter a valid phone number (e.g. +94771234567 or 0771234567)");
            etBusinessPhone.requestFocus();
            return;
        }

        // Email Format Validation
        if (TextUtils.isEmpty(bizEmail)) {
            etBusinessEmail.setError("Email address is required");
            etBusinessEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(bizEmail).matches()) {
            etBusinessEmail.setError("Enter a valid email address (e.g. contact@workspace.com)");
            etBusinessEmail.requestFocus();
            return;
        }

        // Disable button while processing to prevent duplicate submissions
        btnSaveBusiness.setEnabled(false);

        if (isUpdateMode) {
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("businessName", bizName);
            updateMap.put("businessCategory", bizCategory);
            updateMap.put("businessPhone", bizPhone);
            updateMap.put("businessEmail", bizEmail);
            updateMap.put("userId", currentUserId);

            db.collection("businesses").document(updateDocId)
                    .update(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Business Updated Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSaveBusiness.setEnabled(true);
                        Toast.makeText(this, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            BusinessModel executionModel = new BusinessModel(bizName, bizCategory, bizPhone, bizEmail, currentUserId);

            db.collection("businesses")
                    .add(executionModel)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Business Saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSaveBusiness.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}