package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
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
import com.google.firebase.firestore.Query;
import android.widget.ScrollView;
import android.widget.ProgressBar;

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
    private TextView tvInitials, tvStudentName, txtTotalCount, txtSubMessage, btnNotifications;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadAvatarImage();

        NotificationPanelHelper.checkAndShowOnResume(this);

        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        if (txtCurrentSavingsValue != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
        }

        Log.d(TAG, "onResume triggered: Fetching fresh data...");
        loadBusinessWorkspaces();
    }

    // Helper method to get the user's preferred currency
    private String getCurrencySymbol() {
        String rawSymbol = CurrencyHelper.getCurrencySymbol(this);
        return (rawSymbol != null && !rawSymbol.trim().isEmpty()) ? rawSymbol : "LKR";
    }

    private void loadUserData() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        if (userId == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    android.widget.TextView tvStudentName = findViewById(R.id.tvStudentName);
                    android.widget.TextView tvInitials = findViewById(R.id.tvInitials);

                    String displayName = null;
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        displayName = documentSnapshot.getString("name");
                    }

                    if (displayName == null || displayName.trim().isEmpty()) {
                        if (currentUser.getEmail() != null) {
                            displayName = currentUser.getEmail();
                        }
                    }

                    if (displayName != null && !displayName.trim().isEmpty()) {
                        if (tvStudentName != null) {
                            tvStudentName.setText(displayName);
                        }
                        if (tvInitials != null) {
                            tvInitials.setText(displayName.substring(0, 1).toUpperCase());
                        }
                    } else {
                        if (tvStudentName != null) {
                            tvStudentName.setText("User");
                        }
                        if (tvInitials != null) {
                            tvInitials.setText("U");
                        }
                    }
                });
    }

    private void loadAvatarImage() {
        android.content.SharedPreferences prefs = getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE);
        String uriStr = prefs.getString("avatar_uri", null);
        android.widget.ImageView imgDashboardAvatar = findViewById(R.id.imgDashboardAvatar);
        TextView tvInitials = findViewById(R.id.txtProfileLetter);
        if (uriStr != null && imgDashboardAvatar != null) {
            try {
                imgDashboardAvatar.setImageURI(android.net.Uri.parse(uriStr));
            } catch (Exception e) {
                getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE).edit().remove("avatar_uri").apply();
            }
            imgDashboardAvatar.setVisibility(android.view.View.VISIBLE);
            if (tvInitials != null) tvInitials.setVisibility(android.view.View.GONE);
        } else {
            if (imgDashboardAvatar != null) imgDashboardAvatar.setVisibility(android.view.View.GONE);
            if (tvInitials != null) tvInitials.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void initializeViews() {
        tvInitials = findViewById(R.id.txtProfileLetter);
        tvStudentName = findViewById(R.id.tvStudentName);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnTopLogout = findViewById(R.id.btnTopLogout);
        recyclerBusinessFilters = findViewById(R.id.recyclerBusinessFilters);

        ImageView btnManageBusinesses = findViewById(R.id.btnManageBusinesses);
        btnManageBusinesses.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageBusinessActivity.class));
        });

        if (tvInitials != null) {
            tvInitials.setOnClickListener(v -> startActivity(new Intent(this, BusinessProfileActivity.class)));
        }

        recyclerBusinessFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Click listeners
        findViewById(R.id.cardManageLoan).setOnClickListener(v -> startActivity(new Intent(this, LoanFormActivity.class)));
        findViewById(R.id.cardManageSubscription).setOnClickListener(v -> startActivity(new Intent(this, SubscriptionManagerActivity.class)));
        findViewById(R.id.cardManageUtility).setOnClickListener(v -> startActivity(new Intent(this, UtilityManagerActivity.class)));
        findViewById(R.id.cardSavingManager).setOnClickListener(v -> startActivity(new Intent(this, SavingManagerActivity.class)));
        findViewById(R.id.B2BInvoice).setOnClickListener(v -> startActivity(new Intent(this, InvoiceHubActivity.class)));

        // Changed to FinancialReportsActivity
        findViewById(R.id.cardAnalytics).setOnClickListener(v -> startActivity(new Intent(this, FinancialReportsActivity.class)));

        findViewById(R.id.cardStaticBizAdd).setOnClickListener(v -> startActivity(new Intent(this, AddBusinessActivity.class)));

        setupSecurityButton();
        setupSavingsWidget();

        btnNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationListActivity.class)));
        btnTopLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out Safely", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    private void setupSavingsWidget() {
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        android.view.View btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        android.view.View cardSavingsWidget = findViewById(R.id.cardSavingsWidget);

        if (txtCurrentSavingsValue != null && btnUpdateSavings != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
            btnUpdateSavings.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            if (cardSavingsWidget != null) {
                cardSavingsWidget.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            }
        }
    }

    private void loadSavingsFromFirestore(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        final String currencySymbol = getCurrencySymbol(); // Effectively final for lambda

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentSavings = documentSnapshot.getString("currentSavings");
                        if (currentSavings != null && !currentSavings.trim().isEmpty()) {
                            try {
                                double amt = Double.parseDouble(currentSavings.trim());
                                txtValue.setText(String.format(Locale.US, "%s %.2f", currencySymbol, amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText(currencySymbol + " " + currentSavings);
                            }
                        } else {
                            txtValue.setText(currencySymbol + " 0.00");
                        }
                    }
                });
    }

    private void showUpdateSavingsDialog(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        final String currencySymbol = getCurrencySymbol(); // Effectively final for lambda

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Current Savings");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount (" + currencySymbol + ")");

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    double amt = Double.parseDouble(val);
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update("currentSavings", String.valueOf(amt))
                            .addOnSuccessListener(aVoid -> {
                                txtValue.setText(String.format(Locale.US, "%s %.2f", currencySymbol, amt));
                                Toast.makeText(this, "Savings updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void setupUserIdentityProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            tvStudentName.setText(email);
            tvInitials.setText(email.substring(0, 1).toUpperCase(Locale.ROOT));
        } else {
            tvStudentName.setText("guest.workspace@email.com");
            tvInitials.setText("G");
        }
    }

    private void loadBusinessWorkspaces() {
        FirebaseUser user = mAuth.getCurrentUser();
        // 💡 Use UID instead of Email
        if (user == null) return;
        String currentUserId = user.getUid();

        businessNamesList.clear();
        businessIdsList.clear();

        businessNamesList.add("ALL WORKSPACES");
        businessIdsList.add("ALL WORKSPACES");

        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (doc.exists()) {
                            String name = doc.getString("businessName");
                            String id = doc.getId();

                            if (name != null && !name.trim().isEmpty()) {
                                name = name.trim();
                                if (!businessIdsList.contains(id)) {
                                    businessNamesList.add(name);
                                    businessIdsList.add(id);
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Total isolated businesses fetched: " + (businessNamesList.size() - 1));
                    // Pass the UID to the next data segment pipeline
                    calculateInvoiceMetricsPipeline(currentUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Businesses cloud fetch failure: " + e.getMessage());
                    calculateInvoiceMetricsPipeline(currentUserId);
                });
    }

    private void calculateInvoiceMetricsPipeline(String currentUserId) {
        // 💡 Server-Side Filter: Only fetch invoices belonging to this specific user
        db.collection("invoices")
                .whereEqualTo("userId", currentUserId)
                .get()
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
                    Toast.makeText(this, "Invoices Cloud Pipeline Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    // Since both pipelines are filtered to this user on the server,
                    // we can safely accumulate without heavy loops
                    pendingTotal += amount;
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

        // Dynamically apply currency preference to the hero card display
        String currencySymbol = getCurrencySymbol();
        txtTotalCount.setText(String.format(Locale.US, "%s %,.2f", currencySymbol, pendingTotal));

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
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#8EB69B"));
                    holder.textView.setTextColor(Color.parseColor("#0B2B26"));
                } else {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#0B2B26"));
                    holder.textView.setTextColor(Color.parseColor("#B0A8FF"));
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
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        if (which == 0) {
                            Intent intent = new Intent(this, PinSetupActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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



    static class FilterViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textView;
        FilterViewHolder(MaterialCardView v, TextView tv) {
            super(v);
            cardView = v;
            textView = tv;
        }
    }

    private void animateCards(View... cards) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                cards[i].setAlpha(0f);
                cards[i].setTranslationY(40f);
                final int delay = i * 100;
                cards[i].animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(delay)
                        .setInterpolator(new OvershootInterpolator(1.2f))
                        .start();
            }
        }
    }
}