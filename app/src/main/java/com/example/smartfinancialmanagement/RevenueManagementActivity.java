package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RevenueManagementActivity extends AppCompatActivity {

    private static final String TAG = "RevenueManagement";
    private TextView txtTotalRevenue;
    private LinearLayout layoutBusinessCardsContainer;
    private Button btnOpenAddRevenue;
    private RecyclerView recyclerRevenue;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<String> businessList = new ArrayList<>();

    // Split lists to preserve all monthly logs vs dynamic UI active selections
    private List<DocumentSnapshot> allMonthRevenueDocs = new ArrayList<>();
    private List<DocumentSnapshot> filteredRevenueDocs = new ArrayList<>();

    private String selectedWorkspaceFilter = "ALL"; // Tracks active card filter
    private Map<String, Double> businessTotalsMap = new HashMap<>();

    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        loadBusinessProfiles();
    }

    private void initializeViews() {
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        layoutBusinessCardsContainer = findViewById(R.id.layoutBusinessCardsContainer);
        btnOpenAddRevenue = findViewById(R.id.btnOpenAddRevenue);
        recyclerRevenue = findViewById(R.id.recyclerRevenue);

        recyclerRevenue.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnOpenAddRevenue.setOnClickListener(v -> showAddRevenueDialog());
    }

    private void loadBusinessProfiles() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String currentUserId = user.getUid();

        // Server-Side Filter: Load only your specific business configurations
        db.collection("businesses")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    businessList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String bName = doc.getString("businessName");
                        if (bName != null) businessList.add(bName);
                    }
                    syncDataPipeline(currentUserId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching profiles: " + e.getMessage()));
    }

    private void syncDataPipeline(String currentUserId) {
        // Server-Side Filter: Only fetch revenue documents mapped to this userId
        db.collection("revenues")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    allMonthRevenueDocs.clear();
                    businessTotalsMap.clear();

                    for (String b : businessList) businessTotalsMap.put(b, 0.0);

                    double currentMonthHeroGrandTotal = 0.0;
                    String currentMonthToken = yearMonthFormat.format(Calendar.getInstance().getTime());

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Double amount = doc.getDouble("amount");
                        String bName = doc.getString("selectedBusiness");
                        String dateStr = doc.getString("date");

                        if (amount != null && bName != null) {
                            if (businessTotalsMap.containsKey(bName)) {
                                businessTotalsMap.put(bName, businessTotalsMap.get(bName) + amount);
                            }

                            if (dateStr != null && dateStr.startsWith(currentMonthToken)) {
                                allMonthRevenueDocs.add(doc);
                                currentMonthHeroGrandTotal += amount;
                            }
                        }
                    }

                    txtTotalRevenue.setText(String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthHeroGrandTotal));

                    // Build cards layout UI row
                    populateBusinessRowCards();
                    // Render list history items based on filter bounds
                    applyRecyclerFilter();
                });
    }

    private void populateBusinessRowCards() {
        layoutBusinessCardsContainer.removeAllViews();

        // Master "All Workspaces" selection card
        double overallTotal = 0.0;
        for (double val : businessTotalsMap.values()) {
            overallTotal += val;
        }
        createWorkspaceFilterCard("ALL", overallTotal);

        // Individual workspace selection cards
        for (Map.Entry<String, Double> entry : businessTotalsMap.entrySet()) {
            createWorkspaceFilterCard(entry.getKey(), entry.getValue());
        }
    }

    private void createWorkspaceFilterCard(String title, double value) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(380, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 16, 0);
        card.setLayoutParams(params);
        card.setRadius(24f);
        card.setStrokeWidth(0);

        // Highlight if active selection card
        if (selectedWorkspaceFilter.equalsIgnoreCase(title)) {
            card.setCardBackgroundColor(Color.parseColor("#00D4AA")); // Accent Active Highlight
        } else {
            card.setCardBackgroundColor(Color.parseColor("#1A3050")); // Default background
        }

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(24, 24, 24, 24);

        TextView lblName = new TextView(this);
        lblName.setText(title.toUpperCase());
        lblName.setTextColor(selectedWorkspaceFilter.equalsIgnoreCase(title) ? Color.parseColor("#071A33") : Color.parseColor("#7A9CC0"));
        lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        lblName.setSingleLine(true);
        lblName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView lblValue = new TextView(this);
        lblValue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", value));
        lblValue.setTextColor(selectedWorkspaceFilter.equalsIgnoreCase(title) ? Color.parseColor("#071A33") : Color.parseColor("#4ADE80")); // Green UI Indicator for Revenue
        lblValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        lblValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        lblValue.setPadding(0, 8, 0, 0);

        innerLayout.addView(lblName);
        innerLayout.addView(lblValue);
        card.addView(innerLayout);

        // Card Click Handler: Changes dynamic filter state and updates UI lists
        card.setOnClickListener(v -> {
            selectedWorkspaceFilter = title;
            populateBusinessRowCards(); // Rebuild container to switch background colors
            applyRecyclerFilter();
        });

        layoutBusinessCardsContainer.addView(card);
    }

    private void applyRecyclerFilter() {
        filteredRevenueDocs.clear();

        for (DocumentSnapshot doc : allMonthRevenueDocs) {
            String bName = doc.getString("selectedBusiness");
            if (selectedWorkspaceFilter.equals("ALL") || (bName != null && bName.equalsIgnoreCase(selectedWorkspaceFilter))) {
                filteredRevenueDocs.add(doc);
            }
        }

        setupHistoryRecycler();
    }

    private void showAddRevenueDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String currentUserId = user.getUid();

        if (businessList.isEmpty()) {
            Toast.makeText(this, "Create a business workspace profile baseline configuration first", Toast.LENGTH_SHORT).show();
            return;
        }

        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_add_revenue, null);

        Spinner spinBus = customView.findViewById(R.id.dialogSpinnerBusiness);
        Spinner spinSrc = customView.findViewById(R.id.dialogSpinnerSource);
        EditText edtAmt = customView.findViewById(R.id.dialogEdtAmount);
        Button btnSave = customView.findViewById(R.id.dialogBtnSubmit);

        spinBus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, businessList));
        String[] streams = {"Invoices", "Cash Sales", "Investments", "B2B Grants", "Other"};
        spinSrc.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streams));

        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                .setView(customView)
                .create();

        btnSave.setOnClickListener(v -> {
            String amtStr = edtAmt.getText().toString().trim();
            if (amtStr.isEmpty()) {
                Toast.makeText(this, "Amount parameter space required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> record = new HashMap<>();
            record.put("selectedBusiness", spinBus.getSelectedItem().toString());
            record.put("source", spinSrc.getSelectedItem().toString());
            record.put("amount", Double.parseDouble(amtStr));
            record.put("date", fullDateFormat.format(Calendar.getInstance().getTime()));
            record.put("userId", currentUserId); // Securely bind entry to user's UID profile

            db.collection("revenues").add(record).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Revenue logged into registry", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                syncDataPipeline(currentUserId);
            });
        });

        dialog.show();
    }

    private void setupHistoryRecycler() {
        recyclerRevenue.setAdapter(new RecyclerView.Adapter<HistoryViewHolder>() {
            @NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                View card = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
                return new HistoryViewHolder(card);
            }

            @Override
            public void onBindViewHolder(@NonNull HistoryViewHolder h, int pos) {
                DocumentSnapshot doc = filteredRevenueDocs.get(pos);
                h.t1.setText(String.format("%s • %s", doc.getString("selectedBusiness"), doc.getString("source")));
                h.t1.setTextColor(Color.parseColor("#F0F6FF"));
                h.t2.setText(String.format(Locale.getDefault(), "Rs. %,.2f (Logged: %s) [Hold item to delete entry]", doc.getDouble("amount"), doc.getString("date")));
                h.t2.setTextColor(Color.parseColor("#7A9CC0"));

                h.itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Correction Warning")
                            .setMessage("Delete this transaction row item entry permanently?")
                            .setPositiveButton("Delete", (d, w) -> {
                                db.collection("revenues").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RevenueManagementActivity.this, "Entry removed safely", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) syncDataPipeline(user.getUid());
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() { return filteredRevenueDocs.size(); }
        });
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView t1, t2;
        HistoryViewHolder(View v) {
            super(v);
            t1 = v.findViewById(android.R.id.text1);
            t2 = v.findViewById(android.R.id.text2);
        }
    }
}