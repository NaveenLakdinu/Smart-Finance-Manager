package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BusinessDashboardActivity extends AppCompatActivity {

    private static final String TAG = "BusinessDashboard";
    private TextView txtProfileLetter, txtUserEmail, txtTotalCount, txtSubMessage, btnNotifications;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private RecyclerView recyclerBusinessFilters;
    private View btnTopLogout;
    private ImageView btnManageBusinesses;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<String> businessNamesList = new ArrayList<>();
    private List<String> businessIdsList = new ArrayList<>();
    private String selectedBusinessScope = "ALL WORKSPACES";

    private List<InvoiceModel> cachedInvoices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupUserIdentityProfile();
        checkNotificationPermission();

        // 🛑 loadBusinessWorkspaces(); එක මෙතැනින් ඉවත් කර ඇත.
    }

    // 💡 100% FIX: වෙනත් ක්‍රියාකාරකමක (Activity) සිට නැවත Dashboard එකට එන හැම වෙලාවකම
    // Firebase එකෙන් අලුත්ම දත්ත ඇදලා අරන් තිරය auto-refresh කරන්නේ මෙන්න මේ කොටසින්.
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume triggered: Fetching fresh data...");
        loadBusinessWorkspaces();
    }

    private void initializeViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnTopLogout = findViewById(R.id.btnTopLogout);
        recyclerBusinessFilters = findViewById(R.id.recyclerBusinessFilters);

        // initializeViews() ඇතුළතට මෙය එකතු කරන්න:
        ImageView btnManageBusinesses = findViewById(R.id.btnManageBusinesses);
        btnManageBusinesses.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageBusinessActivity.class));
        });

        recyclerBusinessFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Click listeners
        findViewById(R.id.cardManageLoan).setOnClickListener(v -> startActivity(new Intent(this, LoanFormActivity.class)));
        findViewById(R.id.cardManageSubscription).setOnClickListener(v -> startActivity(new Intent(this, SubscriptionManagerActivity.class)));
        findViewById(R.id.cardManageUtility).setOnClickListener(v -> startActivity(new Intent(this, UtilityManagerActivity.class)));
        findViewById(R.id.cardSavingManager).setOnClickListener(v -> startActivity(new Intent(this, SavingManagerActivity.class)));
        findViewById(R.id.B2BInvoice).setOnClickListener(v -> startActivity(new Intent(this, InvoiceHubActivity.class)));
        findViewById(R.id.cardAnalytics).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.cardStaticBizAdd).setOnClickListener(v -> startActivity(new Intent(this, AddBusinessActivity.class)));

        setupSecurityButton();

        btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());
        btnTopLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out Safely", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupUserIdentityProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            txtUserEmail.setText(email);
            txtProfileLetter.setText(email.substring(0, 1).toUpperCase(Locale.ROOT));
        } else {
            txtUserEmail.setText("guest.workspace@email.com");
            txtProfileLetter.setText("G");
        }
    }

    private void loadBusinessWorkspaces() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        String currentLogInUserEmail = user.getEmail().toLowerCase(Locale.ROOT).trim();

        businessNamesList.clear();
        businessIdsList.clear();

        businessNamesList.add("ALL WORKSPACES");
        businessIdsList.add("ALL WORKSPACES");

        db.collection("businesses")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (doc.exists()) {
                            String name = doc.getString("businessName");
                            String id = doc.getId();

                            String ownerEmailField = doc.getString("ownerEmail");
                            String businessEmailField = doc.getString("businessEmail");

                            if (name != null && !name.trim().isEmpty()) {
                                name = name.trim();

                                String finalOwnerEmail = (ownerEmailField != null) ? ownerEmailField.toLowerCase(Locale.ROOT).trim() : "";
                                String finalBusinessEmail = (businessEmailField != null) ? businessEmailField.toLowerCase(Locale.ROOT).trim() : "";

                                boolean isMyBusiness = false;

                                if (!finalOwnerEmail.isEmpty() && finalOwnerEmail.equals(currentLogInUserEmail)) {
                                    isMyBusiness = true;
                                }
                                else if (!finalBusinessEmail.isEmpty() && finalBusinessEmail.equals(currentLogInUserEmail)) {
                                    isMyBusiness = true;
                                }

                                if (isMyBusiness) {
                                    if (!businessIdsList.contains(id)) {
                                        businessNamesList.add(name);
                                        businessIdsList.add(id);
                                    }
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Total businesses fetched and shown: " + (businessNamesList.size() - 1));
                    calculateInvoiceMetricsPipeline(currentLogInUserEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Businesses load failure: " + e.getMessage());
                    calculateInvoiceMetricsPipeline(currentLogInUserEmail);
                });
    }

    private void calculateInvoiceMetricsPipeline(String ownerEmail) {
        db.collection("invoices").get()
                .addOnSuccessListener(snapshots -> {
                    cachedInvoices.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        InvoiceModel invoice = doc.toObject(InvoiceModel.class);
                        if (invoice != null) {
                            cachedInvoices.add(invoice);
                        }
                    }

                    updateHeroCardDisplay();
                    setupFilterRecyclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Invoices Pipeline Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setupFilterRecyclerView();
                });
    }

    private void updateHeroCardDisplay() {
        double pendingTotal = 0.0;

        for (InvoiceModel invoice : cachedInvoices) {
            String status = invoice.getStatus();
            String bizNameInInvoice = invoice.getSelectedBusiness();
            double amount = invoice.getGrandTotal();

            if (status != null && (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("unpaid"))) {

                if (selectedBusinessScope.equals("ALL WORKSPACES")) {
                    if (bizNameInInvoice != null && (businessNamesList.contains(bizNameInInvoice) || businessIdsList.contains(bizNameInInvoice))) {
                        pendingTotal += amount;
                    }
                } else {
                    int selectedIndex = businessNamesList.indexOf(selectedBusinessScope);
                    if (selectedIndex != -1) {
                        String correspondingId = businessIdsList.get(selectedIndex);

                        if (bizNameInInvoice != null &&
                                (bizNameInInvoice.equalsIgnoreCase(selectedBusinessScope) || bizNameInInvoice.equals(correspondingId))) {
                            pendingTotal += amount;
                        }
                    }
                }
            }
        }

        txtTotalCount.setText(String.format(Locale.getDefault(), "Rs. %,.2f", pendingTotal));

        if (selectedBusinessScope.equals("ALL WORKSPACES")) {
            txtSubMessage.setText("Total pending invoices across all registered workspaces");
        } else {
            txtSubMessage.setText("Pending balances isolated for: " + selectedBusinessScope);
        }
    }

    private void setupFilterRecyclerView() {
        RecyclerView.Adapter<FilterViewHolder> adapter = new RecyclerView.Adapter<FilterViewHolder>() {
            @NonNull
            @Override
            public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                MaterialCardView card = new MaterialCardView(parent.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 0);
                card.setLayoutParams(params);
                card.setRadius(19.0f);
                card.setStrokeWidth(0);

                TextView tv = new TextView(parent.getContext());
                tv.setPadding(34, 16, 34, 16);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
                tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                card.addView(tv);

                return new FilterViewHolder(card, tv);
            }

            @Override
            public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
                String filterName = businessNamesList.get(position);
                holder.textView.setText(filterName);

                if (filterName.equalsIgnoreCase(selectedBusinessScope)) {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#00D4AA"));
                    holder.textView.setTextColor(Color.parseColor("#071A33"));
                } else {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#1E293B"));
                    holder.textView.setTextColor(Color.parseColor("#7A9CC0"));
                }

                holder.itemView.setOnClickListener(v -> {
                    selectedBusinessScope = filterName;
                    updateHeroCardDisplay();
                    notifyDataSetChanged();
                });
            }

            @Override
            public int getItemCount() {
                return businessNamesList.size();
            }
        };

        recyclerBusinessFilters.setAdapter(adapter);
    }

    private void setupSecurityButton() {
        View btnSecurity = findViewById(R.id.btnSecurity);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                boolean isPinSet = PinHelper.isPinSet(this);
                String[] options;
                if (isPinSet) {
                    options = new String[]{"Change PIN Lock", "Disable PIN Lock"};
                } else {
                    options = new String[]{"Enable PIN Lock"};
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("PIN Lock Security");
                builder.setItems(options, (dialog, which) -> {
                    if (!isPinSet) {
                        Intent intent = new Intent(this, PinSetupActivity.class);
                        startActivity(intent);
                    } else {
                        if (which == 0) {
                            Intent intent = new Intent(this, PinSetupActivity.class);
                            startActivity(intent);
                        } else if (which == 1) {
                            PinHelper.clearPin(this);
                            Toast.makeText(this, "PIN Lock disabled successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }
    }

    private void showNotificationPanelDialog() {
        View panelView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null);
        LinearLayout container = panelView.findViewById(R.id.layoutNotificationsContainer);
        Button btnClose = panelView.findViewById(R.id.btnDismissNotifications);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog).setView(panelView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        String[] samples = {
                "⚠️ Stock alert: Inventory limits dropping inside main workspace.",
                "📈 Monthly statement compiled for review inside Business Analytics.",
                "🧾 New B2B client invoice log processed successfully."
        };

        container.removeAllViews();
        for (String msg : samples) {
            TextView row = new TextView(this);
            row.setText(msg);
            row.setTextColor(Color.parseColor("#F0F6FF"));
            row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0f);
            row.setPadding(0, 16, 0, 16);
            container.addView(row);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textView;
        FilterViewHolder(MaterialCardView v, TextView tv) {
            super(v);
            cardView = v;
            textView = tv;
        }
    }
}