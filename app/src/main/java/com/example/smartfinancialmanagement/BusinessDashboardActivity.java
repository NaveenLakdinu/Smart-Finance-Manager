package com.example.smartfinancialmanagement;

import android.Manifest; // Required import
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;  // Required import
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Required import
import androidx.core.content.ContextCompat; // Required import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Header / Profile Info
    private TextView txtProfileLetter;
    private TextView txtGreeting;
    private TextView txtUserEmail;
    private TextView btnNotifications;
    private View btnTopLogout;

    // Summary State Fields
    private TextView txtTotalCount;
    private TextView txtSubMessage;

    // My Businesses Custom Filter Horizontal Feed
    private RecyclerView recyclerBusinessFilters;
    private View cardStaticBizAdd;
    private FirebaseFirestore db;

    // Feature Modules Grid
    private MaterialCardView cardManageLoan;
    private MaterialCardView cardManageSubscription;
    private MaterialCardView cardManageUtility;
    private MaterialCardView cardSavingManager;
    private MaterialCardView cardB2BInvoice;
    private MaterialCardView cardAnalytics;

    // Bottom Feed Lists
    private View recentSection;
    private TextView txtViewAll;
    private RecyclerView SampleRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard);

        initializeViews();
        setupClickListeners();
        configureRecentListsFeed();
        fetchBusinessFiltersFromDb();

        // Check and prompt for Android 13+ Push Notification Permission runtime request
        checkNotificationPermission();
    }

    /**
     * Inspects target framework build conditions and requests runtime notification permission if needed.
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request the permission using the matching request code (101) handled below
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    /**
     * Maps and initializes layout components safely.
     */
    private void initializeViews() {
        // Cloud Firestore Setup
        db = FirebaseFirestore.getInstance();

        // Header Binds
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        // Core Balance Summary Info
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);

        // Horizontal Business Filters Bindings
        cardStaticBizAdd = findViewById(R.id.cardStaticBizAdd);
        recyclerBusinessFilters = findViewById(R.id.recyclerBusinessFilters);
        if (recyclerBusinessFilters != null) {
            recyclerBusinessFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        // Grid Actions
        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardB2BInvoice = findViewById(R.id.B2BInvoice);
        cardAnalytics = findViewById(R.id.cardAnalytics);

        // Secondary List Containers
        recentSection = findViewById(R.id.recentSection);
        txtViewAll = findViewById(R.id.txtViewAll);
        SampleRecycler = findViewById(R.id.recyclerRecent);
    }

    /**
     * Chains UI component event loop triggers using optimal Java lambda blocks.
     */
    private void setupClickListeners() {
        if (cardStaticBizAdd != null) {
            cardStaticBizAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddBusinessActivity.class);
                startActivity(intent);
            });
        }

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Opening corporate notification stream...", Toast.LENGTH_SHORT).show()
        );

        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardManageLoan.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, LoanFormActivity.class);
            startActivity(intent);
        });

        cardManageUtility.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, UtilityManagerActivity.class);
            startActivity(intent);
        });

        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SavingManagerActivity.class);
            startActivity(intent);
        });

        cardManageSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });

        cardB2BInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, InvoiceHubActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });

        txtViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Fetches dynamic filter tabs directly from Cloud Firestore
     */
    private void fetchBusinessFiltersFromDb() {
        if (recyclerBusinessFilters == null) return;

        db.collection("businesses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BusinessModel> businessList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        BusinessModel biz = doc.toObject(BusinessModel.class);
                        if (biz != null) {
                            businessList.add(biz);
                        }
                    }

                    BusinessFilterAdapter adapter = new BusinessFilterAdapter(businessList);
                    recyclerBusinessFilters.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Filter Sync Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Initializes structural linear state layouts safely for the recent view.
     */
    private void configureRecentListsFeed() {
        if (SampleRecycler != null) {
            SampleRecycler.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    // Dynamic Filter Inline Recycler Component Implementation
    private static class BusinessFilterAdapter extends RecyclerView.Adapter<BusinessFilterAdapter.FilterViewHolder> {
        private final List<BusinessModel> items;

        public BusinessFilterAdapter(List<BusinessModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_business_filter, parent, false);
            return new FilterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
            BusinessModel item = items.get(position);
            holder.name.setText(item.getBusinessName());
            holder.amount.setText(" (" + item.getBusinessCategory() + ")");

            String normalizedName = item.getBusinessName().toLowerCase();
            if (normalizedName.contains("flower")) {
                holder.icon.setText("💐");
            } else if (normalizedName.contains("fleet") || normalizedName.contains("logistics")) {
                holder.icon.setText("🚚");
            } else {
                holder.icon.setText("💼");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class FilterViewHolder extends RecyclerView.ViewHolder {
            TextView icon, name, amount;
            FilterViewHolder(View v) {
                super(v);
                icon = v.findViewById(R.id.txtFilterIcon);
                name = v.findViewById(R.id.txtFilterBusinessName);
                amount = v.findViewById(R.id.txtFilterDueAmount);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification reminders enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Reminders disabled. You won't get bill alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }
}