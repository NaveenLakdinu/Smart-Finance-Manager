package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class SubscriptionListActivity extends AppCompatActivity {

    private RecyclerView recyclerSubscriptions;
    private LinearLayout emptyState;
    private ImageView backButton, btnAdd;
    private ArrayList<Subscription> subscriptionList;
    private RecentSubscriptionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);

        initViews();
        setupRecyclerView();
        loadSubscriptions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubscriptions();
    }

    private void initViews() {
        recyclerSubscriptions = findViewById(R.id.recyclerSubscriptions);
        emptyState = findViewById(R.id.emptyState);
        backButton = findViewById(R.id.backButton);
        btnAdd = findViewById(R.id.btnAdd);

        backButton.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddSubscriptionActivity.class));
        });
    }

    private void setupRecyclerView() {
        subscriptionList = new ArrayList<>();
        adapter = new RecentSubscriptionAdapter(this, subscriptionList);
        recyclerSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSubscriptions.setAdapter(adapter);

        adapter.setOnItemClickListener(new RecentSubscriptionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Subscription subscription, int position) {
                Intent intent = new Intent(SubscriptionListActivity.this, SubscriptionDetailActivity.class);
                intent.putExtra("DOC_ID", subscription.getDocumentId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Subscription subscription, int position) {
                showDeleteDialog(subscription, position);
            }
        });
    }

    private void loadSubscriptions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    subscriptionList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subscription sub = doc.toObject(Subscription.class);
                        sub.setDocumentId(doc.getId());
                        subscriptionList.add(sub);
                    }
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(subscriptionList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerSubscriptions.setVisibility(subscriptionList.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog(Subscription subscription, int position) {
        new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setTitle("Delete Subscription")
                .setMessage("Delete \"" + subscription.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteSubscription(subscription, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSubscription(Subscription subscription, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SubscriptionNotificationScheduler.cancelAll(this, user.getUid(), subscription.getDocumentId());

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions").document(subscription.getDocumentId())
                .delete()
                .addOnSuccessListener(v -> {
                    adapter.removeItem(position);
                    Toast.makeText(this, "Subscription deleted", Toast.LENGTH_SHORT).show();
                    if (subscriptionList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerSubscriptions.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
