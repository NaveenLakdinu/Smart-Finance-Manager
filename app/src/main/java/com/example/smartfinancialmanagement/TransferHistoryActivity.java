package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class TransferHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerTransfers;
    private TextView txtEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_history);

        recyclerTransfers = findViewById(R.id.recyclerTransfers);
        txtEmptyState = findViewById(R.id.txtEmptyState);

        recyclerTransfers.setLayoutManager(new LinearLayoutManager(this));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadTransferHistory();
    }

    private void loadTransferHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("transfers")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TransferAdapter.TransferInfo> transfers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String from = doc.getString("fromAccount");
                        String to = doc.getString("toAccount");
                        Double amount = doc.getDouble("amount");
                        String note = doc.getString("note");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        if (from != null && to != null && amount != null) {
                            transfers.add(new TransferAdapter.TransferInfo(from, to, amount, note, ts));
                        }
                    }
                    if (transfers.isEmpty()) {
                        txtEmptyState.setVisibility(View.VISIBLE);
                        recyclerTransfers.setVisibility(View.GONE);
                    } else {
                        txtEmptyState.setVisibility(View.GONE);
                        recyclerTransfers.setVisibility(View.VISIBLE);
                        recyclerTransfers.setAdapter(new TransferAdapter(transfers));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
