package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseClaimListActivity extends AppCompatActivity {

    private RecyclerView recyclerClaims;
    private TextView txtPendingTotal, txtPendingCount, txtApprovedTotal, txtApprovedCount, txtResultCount;
    private TextView tabAll, tabPending, tabApproved, tabRejected, tabDrafts;
    private FloatingActionButton fabNewClaim;

    private FirebaseFirestore db;
    private String uid;
    private List<ExpenseClaim> allClaims = new ArrayList<>();
    private List<ExpenseClaim> filteredClaims = new ArrayList<>();
    private ExpenseClaimAdapter adapter;
    private String currentTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claims);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        initViews();
        setupRecyclerView();
        setupTabs();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClaimsFromFirestore();
    }

    private void initViews() {
        txtPendingTotal = findViewById(R.id.txtPendingTotal);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtApprovedTotal = findViewById(R.id.txtApprovedTotal);
        txtApprovedCount = findViewById(R.id.txtApprovedCount);
        txtResultCount = findViewById(R.id.txtResultCount);
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);
        tabDrafts = findViewById(R.id.tabDrafts);
        fabNewClaim = findViewById(R.id.fabNewClaim);
        recyclerClaims = findViewById(R.id.recyclerClaims);

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
                new AlertDialog.Builder(ExpenseClaimListActivity.this)
                        .setTitle("Delete Claim")
                        .setMessage("Delete \"" + claim.getTitle() + "\"? This cannot be undone.")
                        .setPositiveButton("Delete", (d, w) -> {
                            if (uid == null || claim.getClaimId() == null) return;
                            db.collection("users").document(uid)
                                    .collection("expense_claims").document(claim.getClaimId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ExpenseClaimListActivity.this, "Claim deleted", Toast.LENGTH_SHORT).show();
                                        loadClaimsFromFirestore();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(ExpenseClaimListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerClaims.setLayoutManager(new LinearLayoutManager(this));
        recyclerClaims.setAdapter(adapter);
        recyclerClaims.setNestedScrollingEnabled(false);
    }

    private void setupTabs() {
        View.OnClickListener tabClickListener = v -> {
            TextView selected = (TextView) v;
            currentTab = selected.getText().toString();

            tabAll.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabAll.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_secondary));
            tabPending.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabPending.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_secondary));
            tabApproved.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabApproved.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_secondary));
            tabRejected.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabRejected.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_secondary));
            tabDrafts.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabDrafts.setTextColor(ContextCompat.getColor(this, R.color.text_on_dark_secondary));

            selected.setBackgroundResource(R.drawable.bg_tab_active);
            selected.setTextColor(ContextCompat.getColor(this, R.color.qa_purple_icon));

            applyFilter();
        };

        if (tabAll != null) tabAll.setOnClickListener(tabClickListener);
        if (tabPending != null) tabPending.setOnClickListener(tabClickListener);
        if (tabApproved != null) tabApproved.setOnClickListener(tabClickListener);
        if (tabRejected != null) tabRejected.setOnClickListener(tabClickListener);
        if (tabDrafts != null) tabDrafts.setOnClickListener(tabClickListener);
    }

    private void setupFab() {
        if (fabNewClaim != null) {
            fabNewClaim.setOnClickListener(v -> showAddClaimDialog());
        }
    }

    private void loadClaimsFromFirestore() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allClaims.clear();
                    double pendingTotal = 0;
                    double approvedTotal = 0;
                    int pendingCount = 0;
                    int approvedCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ExpenseClaim claim = new ExpenseClaim();
                        claim.setClaimId(doc.getId());
                        claim.setTitle(doc.getString("title"));
                        claim.setCategory(doc.getString("category"));
                        claim.setExpenseDate(doc.getString("expenseDate"));
                        claim.setDescription(doc.getString("description"));
                        claim.setStatus(doc.getString("status"));
                        claim.setWorkerEmail(doc.getString("workerEmail"));

                        Long amountLong = doc.getLong("amount");
                        Long receiptLong = doc.getLong("receiptCount");
                        claim.setAmount(amountLong != null ? amountLong.doubleValue() : 0);
                        claim.setReceiptCount(receiptLong != null ? receiptLong.intValue() : 0);

                        if (claim.getTitle() == null) claim.setTitle("Untitled");
                        if (claim.getCategory() == null) claim.setCategory("Other");
                        if (claim.getExpenseDate() == null) claim.setExpenseDate("N/A");
                        if (claim.getStatus() == null) claim.setStatus("PENDING");

                        allClaims.add(claim);

                        if ("PENDING".equalsIgnoreCase(claim.getStatus())) {
                            pendingTotal += claim.getAmount();
                            pendingCount++;
                        } else if ("APPROVED".equalsIgnoreCase(claim.getStatus())) {
                            approvedTotal += claim.getAmount();
                            approvedCount++;
                        }
                    }

                    if (txtPendingTotal != null)
                        txtPendingTotal.setText(String.format(Locale.US, "LKR %,.2f", pendingTotal));
                    if (txtPendingCount != null)
                        txtPendingCount.setText(String.format(Locale.US, "%d claims", pendingCount));
                    if (txtApprovedTotal != null)
                        txtApprovedTotal.setText(String.format(Locale.US, "LKR %,.2f", approvedTotal));
                    if (txtApprovedCount != null)
                        txtApprovedCount.setText(String.format(Locale.US, "%d claims", approvedCount));

                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load claims", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter() {
        filteredClaims.clear();
        for (ExpenseClaim claim : allClaims) {
            if (currentTab.equals("All") ||
                    claim.getStatus().equalsIgnoreCase(currentTab) ||
                    ("Drafts".equals(currentTab) && "DRAFT".equalsIgnoreCase(claim.getStatus()))) {
                filteredClaims.add(claim);
            }
        }
        adapter.notifyDataSetChanged();
        if (txtResultCount != null)
            txtResultCount.setText(String.format(Locale.US, "%d results", filteredClaims.size()));
    }

    private void showAddClaimDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense_claim, null);

        EditText etTitle = dialogView.findViewById(R.id.etClaimTitle);
        EditText etAmount = dialogView.findViewById(R.id.etClaimAmount);
        EditText etDescription = dialogView.findViewById(R.id.etClaimDescription);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        TextView tvExpenseDate = dialogView.findViewById(R.id.tvExpenseDate);

        String[] categories = {"Travel", "Meals", "Transport", "Accommodation", "Supplies", "Other"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        final String[] selectedDate = {""};
        tvExpenseDate.setOnClickListener(v2 -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate[0] = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                tvExpenseDate.setText(selectedDate[0]);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("New Expense Claim")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();

                    if (title.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Title and amount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String date = selectedDate[0].isEmpty() ? "N/A" : selectedDate[0];
                    String email = FirebaseAuth.getInstance().getCurrentUser() != null ?
                            FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

                    Map<String, Object> claimData = new HashMap<>();
                    claimData.put("title", title);
                    claimData.put("category", category);
                    claimData.put("amount", amount);
                    claimData.put("expenseDate", date);
                    claimData.put("description", description.isEmpty() ? "No description" : description);
                    claimData.put("receiptCount", 0);
                    claimData.put("status", "PENDING");
                    claimData.put("workerEmail", email != null ? email : "");
                    claimData.put("createdAt", System.currentTimeMillis());

                    if (uid == null) return;
                    db.collection("users").document(uid)
                            .collection("expense_claims").add(claimData)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Claim submitted!", Toast.LENGTH_SHORT).show();
                                loadClaimsFromFirestore();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClaimDetailDialog(ExpenseClaim claim) {
        new AlertDialog.Builder(this)
                .setTitle(claim.getTitle())
                .setMessage(String.format(Locale.US,
                        "Category: %s\nAmount: %s\nDate: %s\nStatus: %s\n\n%s",
                        claim.getCategoryLabel(),
                        claim.getFormattedAmount(),
                        claim.getExpenseDate(),
                        claim.getStatusLabel(),
                        claim.getDescription()))
                .setPositiveButton("OK", null)
                .show();
    }
}
