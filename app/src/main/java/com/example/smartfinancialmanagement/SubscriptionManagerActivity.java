package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Locale;

public class SubscriptionManagerActivity extends AppCompatActivity {

    private View btnAddSubscription, btnViewSubscriptions, btnSubscriptionReport;
    private ImageView backButton;
    private TextView txtTotalSubscription, btnSeeAll;
    private RecyclerView recyclerActiveSubscriptions;
    private ArrayList<Subscription> activeList;
    private RecentSubscriptionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_manager_function);

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Ensure daily background check is scheduled
        SubscriptionWorkScheduler.scheduleDailyCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        btnAddSubscription = findViewById(R.id.btnAddSubscription);
        btnViewSubscriptions = findViewById(R.id.btnViewSubscriptions);
        btnSubscriptionReport = findViewById(R.id.btnSubscriptionReport);
        backButton = findViewById(R.id.backButton);
        txtTotalSubscription = findViewById(R.id.txtTotalSubscription);
        btnSeeAll = findViewById(R.id.btnSeeAll);
        recyclerActiveSubscriptions = findViewById(R.id.recyclerActiveSubscriptions);
    }

    private void setupRecyclerView() {
        activeList = new ArrayList<>();
        adapter = new RecentSubscriptionAdapter(this, activeList);
        recyclerActiveSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        recyclerActiveSubscriptions.setAdapter(adapter);
        recyclerActiveSubscriptions.setNestedScrollingEnabled(false);

        adapter.setOnItemClickListener(new RecentSubscriptionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Subscription subscription, int position) {
                Intent intent = new Intent(SubscriptionManagerActivity.this, SubscriptionDetailActivity.class);
                intent.putExtra("DOC_ID", subscription.getDocumentId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Subscription subscription, int position) {}
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAddSubscription.setOnClickListener(v -> {
            startActivity(new Intent(this, AddSubscriptionActivity.class));
        });

        btnViewSubscriptions.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionListActivity.class));
        });

        btnSubscriptionReport.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionReportActivity.class));
        });

        btnSeeAll.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionListActivity.class));
        });
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double total = 0;
                    activeList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subscription sub = doc.toObject(Subscription.class);
                        sub.setDocumentId(doc.getId());
                        activeList.add(sub);

                        double amount = sub.getAmount();
                        if ("Yearly".equalsIgnoreCase(sub.getBillingCycle())) {
                            amount /= 12.0;
                        }
                        total += amount;
                    }

                    txtTotalSubscription.setText(String.format(Locale.US, "Rs %,.2f", total));
                    adapter.notifyDataSetChanged();
                });
    }
}
