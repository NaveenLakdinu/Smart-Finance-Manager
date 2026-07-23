package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class RoleUpgradeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialButton btnConfirmUpgrade;
    private ProgressBar progressBarUpgrade;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_upgrade);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnConfirmUpgrade = findViewById(R.id.btnConfirmUpgrade);
        progressBarUpgrade = findViewById(R.id.progressBarUpgrade);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnConfirmUpgrade != null) {
            btnConfirmUpgrade.setOnClickListener(v -> performUpgrade());
        }
    }

    private void performUpgrade() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // Show progress, disable button
        btnConfirmUpgrade.setEnabled(false);
        if (progressBarUpgrade != null) {
            progressBarUpgrade.setVisibility(View.VISIBLE);
        }

        // Prepare worker profile default fields
        Map<String, Object> workerProfile = new HashMap<>();
        workerProfile.put("companyName", "Not specified");
        workerProfile.put("designation", "Hybrid Worker");
        workerProfile.put("monthlySalary", 0.0);

        // Atomic write batch to update main profile and initialize worker subcollection
        WriteBatch batch = db.batch();

        DocumentReference userRef = db.collection("users").document(uid);
        DocumentReference workerProfileRef = db.collection("users").document(uid)
                .collection("worker_profile").document("profile_data");

        batch.update(userRef, "role", "student_worker_hybrid");
        batch.set(workerProfileRef, workerProfile);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Update SharedPreferences user_role
                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                    prefs.edit().putString("user_role", "student_worker_hybrid").apply();

                    if (progressBarUpgrade != null) {
                        progressBarUpgrade.setVisibility(View.GONE);
                    }

                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    btnConfirmUpgrade.setEnabled(true);
                    if (progressBarUpgrade != null) {
                        progressBarUpgrade.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Upgrade failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("🎉 Upgrade Successful!")
                .setMessage("Congratulations! Your role has been updated to Student-Worker Hybrid. You can now access both student budgeting tools and worker portals from your unified dashboard.")
                .setPositiveButton("Go to Dashboard", (dialog, which) -> {
                    Intent intent = new Intent(RoleUpgradeActivity.this, StudentWorkerHybridDashboardActivity.class);
                    intent.putExtra("CURRENT_USER_ROLE", "student_worker_hybrid");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
