package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.TextUtils;
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

        if (TextUtils.isEmpty(bizName) || TextUtils.isEmpty(bizCategory) ||
                TextUtils.isEmpty(bizPhone) || TextUtils.isEmpty(bizEmail)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveBusiness.setEnabled(false);

        if (isUpdateMode) {
            // ==========================================
            // 📝 UPDATE MODE
            // ==========================================
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("businessName", bizName);
            updateMap.put("businessCategory", bizCategory);
            updateMap.put("businessPhone", bizPhone);
            updateMap.put("businessEmail", bizEmail);

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
            // ==========================================
            // ➕ INSERT MODE:
            // ==========================================
            String currentOwnerEmail = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentOwnerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }

            BusinessModel executionModel = new BusinessModel(bizName, bizCategory, bizPhone, bizEmail, currentOwnerEmail);

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