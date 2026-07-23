package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class ExpenseClaimsActivity extends AppCompatActivity {

    private View btnNewClaim, btnClaimHistory, btnClaimReport;
    private ImageView backButton;
    private TextView txtTotalPendingClaims, txtTotalApprovedClaims, txtTotalRejectedClaims;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claims_manager);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClaimStats();
    }

    private void initViews() {
        btnNewClaim = findViewById(R.id.btnNewClaim);
        btnClaimHistory = findViewById(R.id.btnClaimHistory);
        btnClaimReport = findViewById(R.id.btnClaimReport);
        backButton = findViewById(R.id.backButton);
        txtTotalPendingClaims = findViewById(R.id.txtTotalPendingClaims);
        txtTotalApprovedClaims = findViewById(R.id.txtTotalApprovedClaims);
        txtTotalRejectedClaims = findViewById(R.id.txtTotalRejectedClaims);
    }

    private void setupClickListeners() {
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        // New Claim → Opens Add Claim form
        if (btnNewClaim != null) btnNewClaim.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimAddActivity.class);
            startActivity(intent);
        });

        // History → Opens History dashboard
        if (btnClaimHistory != null) btnClaimHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimHistoryActivity.class);
            startActivity(intent);
        });

        // Report → Opens Report dashboard
        if (btnClaimReport != null) btnClaimReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimReportActivity.class);
            startActivity(intent);
        });
    }

    private void loadClaimStats() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double pendingTotal = 0, approvedTotal = 0, rejectedTotal = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        Long amountLong = doc.getLong("amount");
                        double amt = amountLong != null ? amountLong.doubleValue() : 0;

                        if ("PENDING".equalsIgnoreCase(status)) pendingTotal += amt;
                        else if ("APPROVED".equalsIgnoreCase(status)) approvedTotal += amt;
                        else if ("REJECTED".equalsIgnoreCase(status)) rejectedTotal += amt;
                    }

                    if (txtTotalPendingClaims != null)
                        txtTotalPendingClaims.setText(String.format(Locale.US, "Rs %,.2f", pendingTotal));
                    if (txtTotalApprovedClaims != null)
                        txtTotalApprovedClaims.setText(String.format(Locale.US, "Rs %,.2f", approvedTotal));
                    if (txtTotalRejectedClaims != null)
                        txtTotalRejectedClaims.setText(String.format(Locale.US, "Rs %,.2f", rejectedTotal));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                });
    }
}
