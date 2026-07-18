package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageBusinessActivity extends AppCompatActivity {

    private static final String TAG = "ManageBusinessActivity";
    private RecyclerView recyclerManageBiz;
    private FirebaseFirestore db;
    private java.lang.String currentUserId;

    private List<String> bizNames = new ArrayList<>();
    private List<String> bizIds = new ArrayList<>();
    private List<String> bizPhones = new ArrayList<>();
    private List<String> bizEmails = new ArrayList<>();
    private List<String> bizCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_business);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        recyclerManageBiz = findViewById(R.id.recyclerManageBiz);
        recyclerManageBiz.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            loadMyBusinesses();
        } else {
            Toast.makeText(this, "User session not active", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMyBusinesses() {
        if (currentUserId == null) return;

        // 💡 Server-Side Filter: Only pull records that match the authenticated user's ID
        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    bizNames.clear();
                    bizIds.clear();
                    bizPhones.clear();
                    bizEmails.clear();
                    bizCategories.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        bizNames.add(doc.getString("businessName"));
                        bizIds.add(doc.getId());
                        bizPhones.add(doc.getString("businessPhone"));
                        bizEmails.add(doc.getString("businessEmail"));
                        bizCategories.add(doc.getString("businessCategory"));
                    }
                    setupAdapter();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching businesses: " + e.getMessage());
                    Toast.makeText(ManageBusinessActivity.this, "Failed to load profiles", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupAdapter() {
        RecyclerView.Adapter<BizManageViewHolder> adapter = new RecyclerView.Adapter<BizManageViewHolder>() {
            @NonNull
            @Override
            public BizManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_business, parent, false);
                return new BizManageViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull BizManageViewHolder holder, int position) {
                String name = bizNames.get(position);
                holder.nameTv.setText(name);
                holder.catTv.setText("Category: " + bizCategories.get(position) + " | Phone: " + bizPhones.get(position));

                // Click handler sends data packages to AddBusinessActivity for updates
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(ManageBusinessActivity.this, AddBusinessActivity.class);
                    intent.putExtra("IS_UPDATE_MODE", true);
                    intent.putExtra("BIZ_ID", bizIds.get(position));
                    intent.putExtra("BIZ_NAME", name);
                    intent.putExtra("BIZ_CATEGORY", bizCategories.get(position));
                    intent.putExtra("BIZ_PHONE", bizPhones.get(position));
                    intent.putExtra("BIZ_EMAIL", bizEmails.get(position));
                    startActivity(intent);
                });

                // Optional fallback structure: Long click lets them interact with inline quick-actions dialog
                holder.itemView.setOnLongClickListener(v -> {
                    showUpdateDeleteDialog(position);
                    return true;
                });
            }

            @Override
            public int getItemCount() {
                return bizNames.size();
            }
        };
        recyclerManageBiz.setAdapter(adapter);
    }

    private void showUpdateDeleteDialog(int position) {
        String docId = bizIds.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage: " + bizNames.get(position));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText inputName = new EditText(this);
        inputName.setText(bizNames.get(position));
        inputName.setHint("Business Name");
        layout.addView(inputName);

        EditText inputPhone = new EditText(this);
        inputPhone.setText(bizPhones.get(position));
        inputPhone.setHint("Phone Number");
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = inputName.getText().toString().trim();
            String newPhone = inputPhone.getText().toString().trim();

            if (!newName.isEmpty()) {
                db.collection("businesses").document(docId)
                        .update("businessName", newName, "businessPhone", newPhone)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ManageBusinessActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                            loadMyBusinesses();
                        });
            }
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(ManageBusinessActivity.this)
                    .setTitle("Are you sure?")
                    .setMessage("This will permanently delete this workspace.")
                    .setPositiveButton("Yes, Delete", (d, w) -> {
                        db.collection("businesses").document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ManageBusinessActivity.this, "Deleted successfully!", Toast.LENGTH_SHORT).show();
                                    loadMyBusinesses();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    static class BizManageViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, catTv;
        BizManageViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.txtManageBizName);
            catTv = itemView.findViewById(R.id.txtManageBizDetails);
        }
    }
}