package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Locale;

public class LoanFormActivity extends AppCompatActivity implements LoanAdapter.OnLoanClickListener {

    private MaterialCardView btnNewLoan, btnCompareLoans, btnLoanReport;
    private ImageView btnBack;
    private RecyclerView recyclerActiveLoans;
    private TextView totalActiveBalance;
    private LoanAdapter adapter;
    private ArrayList<Loan> loanList;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_form);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        btnNewLoan = findViewById(R.id.btnNewLoan);
        btnCompareLoans = findViewById(R.id.btnCompareLoans);
        btnLoanReport = findViewById(R.id.btnLoanReport);
        recyclerActiveLoans = findViewById(R.id.recyclerActiveLoans);
        totalActiveBalance = findViewById(R.id.totalActiveBalance);

        setupRecyclerView();

        // Set Click Listeners
        btnBack.setOnClickListener(v -> finish());
        btnNewLoan.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanAddActivity.class);
            startActivity(intent);
        });

        btnCompareLoans.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanCompareActivity.class);
            startActivity(intent);
        });

        btnLoanReport.setOnClickListener(v -> {
            Intent intent = new Intent(LoanFormActivity.this, LoanReportActivity.class);
            startActivity(intent);
        });

        loadLoans();
    }

    private void setupRecyclerView() {
        loanList = new ArrayList<>();
        adapter = new LoanAdapter(this, loanList, this);
        recyclerActiveLoans.setLayoutManager(new LinearLayoutManager(this));
        recyclerActiveLoans.setAdapter(adapter);
    }

    private void loadLoans() {
        if (uid == null) return;

        db.collection("users").document(uid).collection("loans")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading loans: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loanList.clear();
                    double total = 0;
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Loan loan = doc.toObject(Loan.class);
                            if (loan != null) {
                                loan.setId(doc.getId());
                                loanList.add(loan);
                                total += loan.getPrincipalAmount();
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    totalActiveBalance.setText(String.format(Locale.US, "LKR %.2f", total));
                });
    }

    @Override
    public void onLoanClick(Loan loan) {
        Intent intent = new Intent(this, LoanAddActivity.class);
        intent.putExtra("loan", loan);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Loan loan) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Loan")
                .setMessage("Are you sure you want to delete this loan?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users").document(uid).collection("loans")
                            .document(loan.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Loan deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
