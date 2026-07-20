package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class ExpenseClaimsActivity extends AppCompatActivity {

    private View btnNewClaim, btnClaimHistory, btnClaimReport;
    private ImageView backButton;
    private TextView txtTotalPendingClaims;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claims_manager);

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingClaimsTotal();
    }

    private void initViews() {
        btnNewClaim = findViewById(R.id.btnNewClaim);
        btnClaimHistory = findViewById(R.id.btnClaimHistory);
        btnClaimReport = findViewById(R.id.btnClaimReport);
        backButton = findViewById(R.id.backButton);
        txtTotalPendingClaims = findViewById(R.id.txtTotalPendingClaims);
    }

    private void setupClickListeners() {
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        if (btnNewClaim != null) btnNewClaim.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimListActivity.class);
            startActivity(intent);
        });

        if (btnClaimHistory != null) btnClaimHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimListActivity.class);
            startActivity(intent);
        });

        if (btnClaimReport != null) btnClaimReport.setOnClickListener(v -> {
            generateExpenseReport();
        });
    }

    private void loadPendingClaimsTotal() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double pendingTotal = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if ("PENDING".equalsIgnoreCase(status)) {
                            Long amount = doc.getLong("amount");
                            if (amount != null) pendingTotal += amount.doubleValue();
                        }
                    }
                    if (txtTotalPendingClaims != null) {
                        txtTotalPendingClaims.setText(String.format(Locale.US, "LKR %,.2f", pendingTotal));
                    }
                })
                .addOnFailureListener(e -> {
                    // Silently handle - UI shows default value
                });
    }

    private void generateExpenseReport() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = 0, pending = 0, approved = 0, rejected = 0;
                    double totalAmount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        total++;
                        String status = doc.getString("status");
                        Long amount = doc.getLong("amount");
                        double amt = amount != null ? amount.doubleValue() : 0;
                        totalAmount += amt;

                        if ("PENDING".equalsIgnoreCase(status)) pending++;
                        else if ("APPROVED".equalsIgnoreCase(status)) approved++;
                        else if ("REJECTED".equalsIgnoreCase(status)) rejected++;
                    }

                    String report = String.format(Locale.US,
                            "Expense Claims Report\n\n" +
                            "Total Claims: %d\n" +
                            "Pending: %d\n" +
                            "Approved: %d\n" +
                            "Rejected: %d\n\n" +
                            "Total Amount: LKR %,.2f",
                            total, pending, approved, rejected, totalAmount);

                    new AlertDialog.Builder(this)
                            .setTitle("Expense Report")
                            .setMessage(report)
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to generate report", Toast.LENGTH_SHORT).show());
    }
}
