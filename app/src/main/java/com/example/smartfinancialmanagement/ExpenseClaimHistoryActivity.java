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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseClaimHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerClaims;
    private TextView txtTotalAmount, txtTotalCount;
    private TextView txtPendingTotal, txtPendingCount;
    private TextView txtApprovedTotal, txtApprovedCount;
    private TextView txtRejectedTotal, txtRejectedCount;
    private TextView txtResultCount, txtSubtitle;
    private TextView tabAll, tabPending, tabApproved, tabRejected;
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
        setContentView(R.layout.activity_expense_claim_history);

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
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClaims();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtPendingTotal = findViewById(R.id.txtPendingTotal);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtApprovedTotal = findViewById(R.id.txtApprovedTotal);
        txtApprovedCount = findViewById(R.id.txtApprovedCount);
        txtRejectedTotal = findViewById(R.id.txtRejectedTotal);
        txtRejectedCount = findViewById(R.id.txtRejectedCount);
        txtResultCount = findViewById(R.id.txtResultCount);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);
        recyclerClaims = findViewById(R.id.recyclerClaims);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        adapter = new ExpenseClaimAdapter(filteredClaims);
        adapter.setOnItemClickListener(new ExpenseClaimAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExpenseClaim claim, int position) {
                showClaimDetail(claim);
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
        View.OnClickListener listener = v -> {
            TextView selected = (TextView) v;
            currentTab = selected.getText().toString().trim();
            resetTabs();
            selected.setBackgroundResource(R.drawable.bg_tab_active);
            selected.setTextColor(ContextCompat.getColor(this, R.color.qa_tertiary_icon));
            applyFilter();
        };
        tabAll.setOnClickListener(listener);
        tabPending.setOnClickListener(listener);
        tabApproved.setOnClickListener(listener);
        tabRejected.setOnClickListener(listener);
    }

    private void resetTabs() {
        int color = ContextCompat.getColor(this, R.color.text_on_dark_secondary);
        tabAll.setBackgroundResource(R.drawable.bg_tab_inactive); tabAll.setTextColor(color);
        tabPending.setBackgroundResource(R.drawable.bg_tab_inactive); tabPending.setTextColor(color);
        tabApproved.setBackgroundResource(R.drawable.bg_tab_inactive); tabApproved.setTextColor(color);
        tabRejected.setBackgroundResource(R.drawable.bg_tab_inactive); tabRejected.setTextColor(color);
    }

    private void loadClaims() {
        if (uid == null || db == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    try {
                        allClaims.clear();
                        double pTotal = 0, aTotal = 0, rTotal = 0;
                        int pCount = 0, aCount = 0, rCount = 0;

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            ExpenseClaim claim = new ExpenseClaim();
                            claim.setClaimId(doc.getId());
                            claim.setTitle(doc.getString("title"));
                            claim.setCategory(doc.getString("category"));
                            claim.setExpenseDate(doc.getString("expenseDate"));
                            claim.setDescription(doc.getString("description"));
                            claim.setStatus(doc.getString("status"));

                            Long amt = doc.getLong("amount");
                            claim.setAmount(amt != null ? amt.doubleValue() : 0);
                            Long rc = doc.getLong("receiptCount");
                            claim.setReceiptCount(rc != null ? rc.intValue() : 0);

                            if (claim.getTitle() == null) claim.setTitle("Untitled");
                            if (claim.getCategory() == null) claim.setCategory("Other");
                            if (claim.getExpenseDate() == null) claim.setExpenseDate("N/A");
                            if (claim.getStatus() == null) claim.setStatus("PENDING");

                            allClaims.add(claim);

                            if ("PENDING".equalsIgnoreCase(claim.getStatus())) { pTotal += claim.getAmount(); pCount++; }
                            else if ("APPROVED".equalsIgnoreCase(claim.getStatus())) { aTotal += claim.getAmount(); aCount++; }
                            else if ("REJECTED".equalsIgnoreCase(claim.getStatus())) { rTotal += claim.getAmount(); rCount++; }
                        }

                        double grandTotal = pTotal + aTotal + rTotal;
                        int grandCount = pCount + aCount + rCount;

                        txtTotalAmount.setText(String.format(Locale.US, "Rs %,.2f", grandTotal));
                        txtTotalCount.setText(String.format(Locale.US, "%d claims", grandCount));
                        txtPendingTotal.setText(String.format(Locale.US, "Rs %,.2f", pTotal));
                        txtPendingCount.setText(String.format(Locale.US, "%d", pCount));
                        txtApprovedTotal.setText(String.format(Locale.US, "Rs %,.2f", aTotal));
                        txtApprovedCount.setText(String.format(Locale.US, "%d", aCount));
                        txtRejectedTotal.setText(String.format(Locale.US, "Rs %,.2f", rTotal));
                        txtRejectedCount.setText(String.format(Locale.US, "%d", rCount));
                        txtSubtitle.setText(String.format(Locale.US, "%d total claims", grandCount));

                        applyFilter();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        txtResultCount.setText(String.format(Locale.US, "%d results", filteredClaims.size()));
    }

    private void showClaimDetail(ExpenseClaim claim) {
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
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    loadClaims();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
