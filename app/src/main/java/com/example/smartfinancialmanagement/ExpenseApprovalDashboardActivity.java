package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ExpenseApprovalDashboardActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtCompanyScope;
    private RecyclerView recyclerApprovalClaims;
    private ExpenseClaimAdapter adapter;
    private List<ExpenseClaim> claimList = new ArrayList<>();
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_approval_dashboard);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();
        loadBusinessNamesAndClaims();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtCompanyScope = findViewById(R.id.txtCompanyScope);
        recyclerApprovalClaims = findViewById(R.id.recyclerApprovalClaims);

        btnBack.setOnClickListener(v -> finish());

        adapter = new ExpenseClaimAdapter(claimList);
        recyclerApprovalClaims.setLayoutManager(new LinearLayoutManager(this));
        recyclerApprovalClaims.setAdapter(adapter);

        adapter.setOnItemClickListener(new ExpenseClaimAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExpenseClaim claim, int position) {
                showApprovalDialog(claim);
            }

            @Override
            public void onItemLongClick(ExpenseClaim claim, int position) {
                // Not used
            }
        });
    }

    private void loadBusinessNamesAndClaims() {
        if (uid == null) return;
        
        db.collection("businesses")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> businessNames = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("businessName");
                        if (name != null) businessNames.add(name);
                    }
                    
                    if (businessNames.isEmpty()) {
                        txtCompanyScope.setText("No businesses found for approval scope.");
                        return;
                    }
                    
                    txtCompanyScope.setText("Scope: " + String.join(", ", businessNames));
                    loadPendingClaims(businessNames);
                });
    }

    private void loadPendingClaims(List<String> businessNames) {
        // We use collectionGroup query to find claims matching the companyName
        if (businessNames.size() > 10) {
             businessNames = businessNames.subList(0, 10); // Firestore whereIn limit is 10
        }
        
        db.collectionGroup("expense_claims")
                .whereEqualTo("status", "PENDING")
                .whereIn("companyName", businessNames)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    claimList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ExpenseClaim claim = doc.toObject(ExpenseClaim.class);
                        if (claim != null) {
                            // Extract path to easily update later. Format: users/{workerUid}/expense_claims/{docId}
                            claim.setDocumentId(doc.getReference().getPath());
                            claimList.add(claim);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (claimList.isEmpty()) {
                        txtCompanyScope.setText(txtCompanyScope.getText().toString() + "\nNo pending claims.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch claims", Toast.LENGTH_SHORT).show();
                    Log.e("ExpenseApproval", "Error", e);
                });
    }

    private void showApprovalDialog(ExpenseClaim claim) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Claim: " + claim.getTitle());
        String msg = "Amount: " + claim.getFormattedAmount() + "\n"
                + "Worker: " + claim.getWorkerEmail() + "\n"
                + "Desc: " + claim.getDescription();
        builder.setMessage(msg);

        builder.setPositiveButton("Approve", (dialog, which) -> updateClaimStatus(claim, "APPROVED"));
        builder.setNegativeButton("Reject", (dialog, which) -> updateClaimStatus(claim, "REJECTED"));
        
        if (claim.getReceiptUrl() != null && !claim.getReceiptUrl().isEmpty()) {
            builder.setNeutralButton("View Receipt", (dialog, which) -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(claim.getReceiptUrl()));
                startActivity(browserIntent);
            });
        }
        
        builder.show();
    }

    private void updateClaimStatus(ExpenseClaim claim, String newStatus) {
        if (claim.getDocumentId() == null) return;
        
        db.document(claim.getDocumentId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Claim " + newStatus, Toast.LENGTH_SHORT).show();
                    claimList.remove(claim);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}
