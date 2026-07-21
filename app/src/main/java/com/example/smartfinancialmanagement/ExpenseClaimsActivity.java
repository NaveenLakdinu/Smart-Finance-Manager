package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
                    // Silently handle
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
                    double totalAmount = 0, pendingAmount = 0, approvedAmount = 0;
                    StringBuilder claimDetails = new StringBuilder();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        total++;
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String status = doc.getString("status");
                        String date = doc.getString("expenseDate");
                        Long amountLong = doc.getLong("amount");
                        double amt = amountLong != null ? amountLong.doubleValue() : 0;
                        totalAmount += amt;

                        if ("PENDING".equalsIgnoreCase(status)) {
                            pending++;
                            pendingAmount += amt;
                        } else if ("APPROVED".equalsIgnoreCase(status)) {
                            approved++;
                            approvedAmount += amt;
                        } else if ("REJECTED".equalsIgnoreCase(status)) {
                            rejected++;
                        }

                        claimDetails.append(String.format(Locale.US,
                                "%s | %s | %s | %s | LKR %,.2f\n",
                                title != null ? title : "N/A",
                                category != null ? category : "N/A",
                                date != null ? date : "N/A",
                                status != null ? status : "N/A",
                                amt));
                    }

                    String report = String.format(Locale.US,
                            "EXPENSE CLAIMS REPORT\n" +
                            "====================\n\n" +
                            "Total Claims: %d\n" +
                            "Pending: %d (LKR %,.2f)\n" +
                            "Approved: %d (LKR %,.2f)\n" +
                            "Rejected: %d\n\n" +
                            "Total Amount: LKR %,.2f\n\n" +
                            "---------------------\n" +
                            "CLAIM DETAILS:\n" +
                            "---------------------\n" +
                            "%s",
                            total, pending, pendingAmount, approved, approvedAmount, rejected, totalAmount,
                            claimDetails.length() > 0 ? claimDetails.toString() : "No claims found\n");

                    new AlertDialog.Builder(this)
                            .setTitle("Expense Report")
                            .setMessage(report)
                            .setPositiveButton("Download Report", (d, w) -> downloadReport(report))
                            .setNegativeButton("Close", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to generate report", Toast.LENGTH_SHORT).show());
    }

    private void downloadReport(String reportContent) {
        try {
            String fileName = "ExpenseReport_" + System.currentTimeMillis() + ".txt";
            OutputStream outputStream;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(uri);
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                outputStream.write(reportContent.getBytes());
                outputStream.flush();
                outputStream.close();
                Toast.makeText(this, "Report downloaded to Downloads folder", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
