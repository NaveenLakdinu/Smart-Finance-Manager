package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseClaimListActivity extends AppCompatActivity {

    private RecyclerView recyclerClaims;
    private TextView txtPendingTotal, txtPendingCount, txtApprovedTotal, txtApprovedCount;
    private TextView txtRejectedTotal, txtRejectedCount, txtResultCount;
    private TextView tabAll, tabPending, tabApproved, tabRejected;
    private FloatingActionButton fabNewClaim;
    private LinearLayout emptyState;

    private FirebaseFirestore db;
    private String uid;
    private List<ExpenseClaim> allClaims = new ArrayList<>();
    private List<ExpenseClaim> filteredClaims = new ArrayList<>();
    private ExpenseClaimAdapter adapter;
    private String currentTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claim_list);

        try {
            db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                uid = user.getUid();
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initViews();
            setupRecyclerView();
            setupTabs();
            setupFab();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uid != null) {
            loadClaimsFromFirestore();
        }
    }

    private void initViews() {
        txtPendingTotal = findViewById(R.id.txtPendingTotal);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtApprovedTotal = findViewById(R.id.txtApprovedTotal);
        txtApprovedCount = findViewById(R.id.txtApprovedCount);
        txtRejectedTotal = findViewById(R.id.txtRejectedTotal);
        txtRejectedCount = findViewById(R.id.txtRejectedCount);
        txtResultCount = findViewById(R.id.txtResultCount);
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);
        fabNewClaim = findViewById(R.id.fabNewClaim);
        recyclerClaims = findViewById(R.id.recyclerClaims);
        emptyState = findViewById(R.id.emptyState);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new ExpenseClaimAdapter(filteredClaims);
        adapter.setOnItemClickListener(new ExpenseClaimAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExpenseClaim claim, int position) {
                showClaimDetailDialog(claim);
            }

            @Override
            public void onItemLongClick(ExpenseClaim claim, int position) {
                showDeleteDialog(claim);
            }
        });

        recyclerClaims.setLayoutManager(new LinearLayoutManager(this));
        recyclerClaims.setAdapter(adapter);
    }

    private void setupTabs() {
        View.OnClickListener tabClickListener = v -> {
            TextView selected = (TextView) v;
            currentTab = selected.getText().toString();
            resetTabStyles();
            selected.setBackgroundResource(R.drawable.bg_tab_active);
            selected.setTextColor(ContextCompat.getColor(this, R.color.qa_purple_icon));
            applyFilter();
        };

        if (tabAll != null) tabAll.setOnClickListener(tabClickListener);
        if (tabPending != null) tabPending.setOnClickListener(tabClickListener);
        if (tabApproved != null) tabApproved.setOnClickListener(tabClickListener);
        if (tabRejected != null) tabRejected.setOnClickListener(tabClickListener);
    }

    private void resetTabStyles() {
        int inactiveColor = ContextCompat.getColor(this, R.color.text_on_dark_secondary);
        if (tabAll != null) { tabAll.setBackgroundResource(R.drawable.bg_tab_inactive); tabAll.setTextColor(inactiveColor); }
        if (tabPending != null) { tabPending.setBackgroundResource(R.drawable.bg_tab_inactive); tabPending.setTextColor(inactiveColor); }
        if (tabApproved != null) { tabApproved.setBackgroundResource(R.drawable.bg_tab_inactive); tabApproved.setTextColor(inactiveColor); }
        if (tabRejected != null) { tabRejected.setBackgroundResource(R.drawable.bg_tab_inactive); tabRejected.setTextColor(inactiveColor); }
    }

    private void setupFab() {
        if (fabNewClaim != null) {
            fabNewClaim.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(this, ExpenseClaimAddActivity.class));
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open claim form", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadClaimsFromFirestore() {
        if (uid == null || db == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    try {
                        allClaims.clear();
                        double pendingTotal = 0, approvedTotal = 0, rejectedTotal = 0;
                        int pendingCount = 0, approvedCount = 0, rejectedCount = 0;

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            ExpenseClaim claim = new ExpenseClaim();
                            claim.setClaimId(doc.getId());
                            claim.setTitle(doc.getString("title"));
                            claim.setCategory(doc.getString("category"));
                            claim.setExpenseDate(doc.getString("expenseDate"));
                            claim.setDescription(doc.getString("description"));
                            claim.setStatus(doc.getString("status"));

                            Long amountLong = doc.getLong("amount");
                            claim.setAmount(amountLong != null ? amountLong.doubleValue() : 0);
                            Long receiptLong = doc.getLong("receiptCount");
                            claim.setReceiptCount(receiptLong != null ? receiptLong.intValue() : 0);

                            if (claim.getTitle() == null) claim.setTitle("Untitled");
                            if (claim.getCategory() == null) claim.setCategory("Other");
                            if (claim.getExpenseDate() == null) claim.setExpenseDate("N/A");
                            if (claim.getStatus() == null) claim.setStatus("PENDING");

                            allClaims.add(claim);

                            if ("PENDING".equalsIgnoreCase(claim.getStatus())) { pendingTotal += claim.getAmount(); pendingCount++; }
                            else if ("APPROVED".equalsIgnoreCase(claim.getStatus())) { approvedTotal += claim.getAmount(); approvedCount++; }
                            else if ("REJECTED".equalsIgnoreCase(claim.getStatus())) { rejectedTotal += claim.getAmount(); rejectedCount++; }
                        }

                        if (txtPendingTotal != null) txtPendingTotal.setText(String.format(Locale.US, "Rs %,.2f", pendingTotal));
                        if (txtPendingCount != null) txtPendingCount.setText(String.format(Locale.US, "%d claims", pendingCount));
                        if (txtApprovedTotal != null) txtApprovedTotal.setText(String.format(Locale.US, "Rs %,.2f", approvedTotal));
                        if (txtApprovedCount != null) txtApprovedCount.setText(String.format(Locale.US, "%d claims", approvedCount));
                        if (txtRejectedTotal != null) txtRejectedTotal.setText(String.format(Locale.US, "Rs %,.2f", rejectedTotal));
                        if (txtRejectedCount != null) txtRejectedCount.setText(String.format(Locale.US, "%d claims", rejectedCount));

                        applyFilter();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load claims: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter() {
        filteredClaims.clear();
        for (ExpenseClaim claim : allClaims) {
            if (currentTab.equals("All") || claim.getStatus().equalsIgnoreCase(currentTab)) {
                filteredClaims.add(claim);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredClaims.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerClaims.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerClaims.setVisibility(View.VISIBLE);
        }

        if (txtResultCount != null) txtResultCount.setText(String.format(Locale.US, "%d results", filteredClaims.size()));
    }

    private void showDeleteDialog(ExpenseClaim claim) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Claim")
                .setMessage("Delete \"" + claim.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> deleteClaim(claim))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClaim(ExpenseClaim claim) {
        if (uid == null || claim.getClaimId() == null || db == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims").document(claim.getClaimId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Claim deleted", Toast.LENGTH_SHORT).show();
                    loadClaimsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showClaimDetailDialog(ExpenseClaim claim) {
        new AlertDialog.Builder(this)
                .setTitle(claim.getTitle())
                .setMessage(String.format(Locale.US,
                        "Category: %s\nAmount: Rs %,.2f\nDate: %s\nStatus: %s\n\n%s",
                        claim.getCategoryLabel(), claim.getAmount(),
                        claim.getExpenseDate(), claim.getStatusLabel(),
                        claim.getDescription() != null ? claim.getDescription() : ""))
                .setPositiveButton("OK", null)
                .show();
    }
}
