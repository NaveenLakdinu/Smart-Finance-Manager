package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Locale;

public class UtilityBillActivity extends AppCompatActivity implements UtilityAdapter.OnUtilityClickListener {

    private ImageView btnBack;
    private RecyclerView recyclerBills;
    private TextView txtPaidCount;
    private TextView txtDueCount;

    private UtilityAdapter adapter;
    private ArrayList<UtilityBill> billList;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_bills);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        btnBack = findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> finish());

        txtPaidCount = findViewById(R.id.txtPaidCount);
        txtDueCount = findViewById(R.id.txtDueCount);
        recyclerBills = findViewById(R.id.recyclerBills);

        setupRecyclerView();
        loadUtilityBills();
    }

    private void setupRecyclerView() {
        billList = new ArrayList<>();
        adapter = new UtilityAdapter(this, billList, this);
        recyclerBills.setLayoutManager(new LinearLayoutManager(this));
        recyclerBills.setAdapter(adapter);
    }

    private void loadUtilityBills() {
        if (uid == null) return;

        db.collection("users").document(uid).collection("utilities")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading bills: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    billList.clear();
                    int paid = 0;
                    int due = 0;

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            UtilityBill bill = doc.toObject(UtilityBill.class);
                            if (bill != null) {
                                bill.setId(doc.getId());
                                billList.add(bill);
                                
                                String status = bill.getStatus() != null ? bill.getStatus().toLowerCase() : "";
                                if (status.equals("paid")) {
                                    paid++;
                                } else {
                                    due++;
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (txtPaidCount != null) txtPaidCount.setText(String.format(Locale.US, "%02d", paid));
                    if (txtDueCount != null) txtDueCount.setText(String.format(Locale.US, "%02d", due));
                });
    }

    @Override
    public void onDeleteClick(UtilityBill bill) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users").document(uid).collection("utilities")
                            .document(bill.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
