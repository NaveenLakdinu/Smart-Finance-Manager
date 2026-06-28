package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddBusinessActivity extends AppCompatActivity {

    private EditText etBusinessName, etBusinessCategory, etBusinessPhone, etBusinessEmail;
    private MaterialButton btnSaveBusiness;
    private ImageView btnBack;
    private FirebaseFirestore db;

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
        btnSaveBusiness.setOnClickListener(v -> saveBusinessToFirestore());
    }

    private void saveBusinessToFirestore() {
        String bizName = etBusinessName.getText().toString().trim();
        String bizCategory = etBusinessCategory.getText().toString().trim();
        String bizPhone = etBusinessPhone.getText().toString().trim();
        String bizEmail = etBusinessEmail.getText().toString().trim();

        if (TextUtils.isEmpty(bizName)) {
            etBusinessName.setError("Business Name is required");
            return;
        }
        if (TextUtils.isEmpty(bizCategory)) {
            bizCategory = "General";
        }

        BusinessModel executionModel = new BusinessModel(bizName, bizCategory, bizPhone, bizEmail);
        btnSaveBusiness.setEnabled(false);

        db.collection("businesses")
                .add(executionModel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddBusinessActivity.this, "Business registered successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveBusiness.setEnabled(true);
                    Toast.makeText(AddBusinessActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}