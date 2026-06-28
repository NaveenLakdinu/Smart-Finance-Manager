package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private TextView txtProfileLetter, txtUserEmail, txtTotalCount, txtSubMessage, btnNotifications;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private RecyclerView recyclerBusinessFilters;
    private View btnTopLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> businessList = new ArrayList<>();
    private String selectedBusinessScope = "ALL WORKSPACES"; // Default state

    // Invoice Metrics Tracking
    private double allWorkspacesInvoiceTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupUserIdentityProfile();
        loadBusinessWorkspaces();
        checkNotificationPermission();
    }

    private void initializeViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnTopLogout = findViewById(R.id.btnTopLogout);
        recyclerBusinessFilters = findViewById(R.id.recyclerBusinessFilters);

        recyclerBusinessFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Navigation Link Handlers
        findViewById(R.id.cardStaticBizAdd).setOnClickListener(v -> {
            Toast.makeText(this, "Navigate to Add Business Profile Layout Module", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardAnalytics).setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyticsActivity.class));
        });

        findViewById(R.id.B2BInvoice).setOnClickListener(v ->
                Toast.makeText(this, "B2B Invoice Screen", Toast.LENGTH_SHORT).show()
        );

        btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());

        btnTopLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out Safely", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(BusinessDashboardActivity.this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkNotificationPermission() {
        // උපාංගයේ Android සංස්කරණය Android 13 (TIRAMISU) හෝ ඊට ඉහළ නම් පමණක් ක්‍රියාත්මක වේ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // පරිශීලකයා මින් පෙර අවසර දී නොමැති නම් අවසර ඉල්ලන Dialog එක පෙන්වීම
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
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

    // පරිශීලකයා Permission Dialog එකට ලබාදෙන පිළිතුර (Allow / Don't Allow) හැසිරවීම
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                // පරිශීලකයා Permission එක ප්‍රතික්ෂේප කළහොත් පෙන්වන පණිවිඩය
                Toast.makeText(this, "Notifications disabled. You might miss important financial alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadBusinessWorkspaces() {
        db.collection("businesses").get()
                .addOnSuccessListener(snapshots -> {
                    businessList.clear();
                    businessList.add("ALL WORKSPACES");

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String name = doc.getString("businessName");
                        if (name != null) businessList.add(name);
                    }
                    calculateInvoiceMetricsPipeline();
                })
                .addOnFailureListener(e -> {
                    // 💡 Firebase බ්ලොක් කළහොත් ක්‍රෑෂ් නොවී මෙම පණිවිඩය පෙන්වයි
                    Toast.makeText(this, "Firestore Businesses Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // දත්ත නැතත් Dashboard එක විවෘත වීමට හිස් ලිස්ට් එකක් සකසයි
                    setupFilterRecyclerView(new ArrayList<>());
                });
    }
    private void calculateInvoiceMetricsPipeline() {
        db.collection("invoices").get()
                .addOnSuccessListener(snapshots -> {
                    allWorkspacesInvoiceTotal = 0.0;
                    List<DocumentSnapshot> allInvoiceDocs = snapshots.getDocuments();

                    for (DocumentSnapshot doc : allInvoiceDocs) {
                        Double totalAmt = doc.getDouble("totalAmount");
                        if (totalAmt == null) totalAmt = doc.getDouble("amount");

                        if (totalAmt != null) {
                            allWorkspacesInvoiceTotal += totalAmt;
                        }
                    }

                    updateHeroCardDisplay(allInvoiceDocs);
                    setupFilterRecyclerView(allInvoiceDocs);
                })
                .addOnFailureListener(e -> {
                    // 💡 Invoices කියවීමට නොහැකි වුවහොත් පෙන්වන පණිවිඩය
                    Toast.makeText(this, "Firestore Invoices Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setupFilterRecyclerView(new ArrayList<>());
                });
    }

    private void updateHeroCardDisplay(List<DocumentSnapshot> invoiceDocs) {
        if (selectedBusinessScope.equals("ALL WORKSPACES")) {
            txtTotalCount.setText(String.format(Locale.getDefault(), "Rs. %,.2f", allWorkspacesInvoiceTotal));
            txtSubMessage.setText("Sum total across all your registered entities");
        } else {
            double selectedBizTotal = 0.0;
            for (DocumentSnapshot doc : invoiceDocs) {
                String bName = doc.getString("selectedBusiness");
                Double amt = doc.getDouble("totalAmount");
                if (amt == null) amt = doc.getDouble("amount");

                if (bName != null && bName.equalsIgnoreCase(selectedBusinessScope) && amt != null) {
                    selectedBizTotal += amt;
                }
            }
            txtTotalCount.setText(String.format(Locale.getDefault(), "Rs. %,.2f", selectedBizTotal));
            txtSubMessage.setText("Isolated metrics view for matrix pipeline: " + selectedBusinessScope);
        }
    }

    private void setupFilterRecyclerView(List<DocumentSnapshot> invoiceDocs) {
        recyclerBusinessFilters.setAdapter(new RecyclerView.Adapter<FilterViewHolder>() {
            @NonNull
            @Override
            public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                MaterialCardView card = new MaterialCardView(parent.getContext());

                // 💡 මෙතැනදී MATCH_PARENT වෙනුවට WRAP_CONTENT යොදා ක්‍රෑෂ් එක සම්පූර්ණයෙන්ම විසඳා ඇත.
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 0);
                card.setLayoutParams(params);

                card.setRadius(19.0f);
                card.setStrokeWidth(0);

                TextView tv = new TextView(parent.getContext());
                tv.setId(View.generateViewId());
                tv.setPadding(34, 16, 34, 16); // Padding මදක් වැඩි කර පෙනුම ලස්සන කරන ලදී

                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
                tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                card.addView(tv);

                return new FilterViewHolder(card, tv);
            }

            @Override
            public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
                String filterName = businessList.get(position);
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
                    updateHeroCardDisplay(invoiceDocs);
                    notifyDataSetChanged();
                });
            }

            @Override
            public int getItemCount() {
                return businessList.size();
            }
        });
    }

    private void showNotificationPanelDialog() {
        View panelView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null);
        LinearLayout container = panelView.findViewById(R.id.layoutNotificationsContainer);
        Button btnClose = panelView.findViewById(R.id.btnDismissNotifications);

        // 💡 වඩාත් ආරක්ෂිත සාමාන්‍ය AlertDialog ස්ටයිල් එකක් භාවිත කර ඇත
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(panelView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        String[] samples = {
                "⚠️ Stock alert: Inventory limits dropping inside main workspace.",
                "📈 Monthly statement compiled for review inside Business Analytics.",
                "🧾 New B2B client invoice log processed successfully."
        };

        container.removeAllViews(); // පැරණි දත්ත තිබේ නම් ඉවත් කිරීම
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