package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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

    private TextView txtTotalRevenue;
    private LinearLayout layoutBusinessCardsContainer;
    private Button btnOpenAddRevenue;
    private RecyclerView recyclerRevenue;

    private FirebaseFirestore db;
    private List<String> businessList = new ArrayList<>();
    private List<DocumentSnapshot> currentMonthRevenueDocs = new ArrayList<>();
    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management);

        db = FirebaseFirestore.getInstance();
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
        db.collection("businesses").get().addOnSuccessListener(snapshots -> {
            businessList.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String bName = doc.getString("businessName");
                if (bName != null) businessList.add(bName);
            }
            syncDataPipeline();
        });
    }

    private void syncDataPipeline() {
        db.collection("revenues").get().addOnSuccessListener(snapshots -> {
            currentMonthRevenueDocs.clear();

            // Tracks the combined total for JUST the current calendar month
            double currentMonthHeroGrandTotal = 0.0;

            // Map tracking structures for summing each business's total separately for small cards
            Map<String, Double> businessTotalsMap = new HashMap<>();
            for (String b : businessList) businessTotalsMap.put(b, 0.0);

            // Get current year and month token string (e.g., "2026-06")
            String currentMonthToken = yearMonthFormat.format(Calendar.getInstance().getTime());

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Double amount = doc.getDouble("amount");
                String bName = doc.getString("selectedBusiness");
                String dateStr = doc.getString("date");

                if (amount != null && bName != null) {
                    // Keep building the small cards' overall metrics mapping
                    if (businessTotalsMap.containsKey(bName)) {
                        businessTotalsMap.put(bName, businessTotalsMap.get(bName) + amount);
                    }

                    // Filter logs falling strictly inside current calendar month metric boundaries
                    if (dateStr != null && dateStr.startsWith(currentMonthToken)) {
                        currentMonthRevenueDocs.add(doc);

                        // Add to the Hero Card total ONLY if it belongs to the current month
                        currentMonthHeroGrandTotal += amount;
                    }
                }
            }

            // The main top hero card layout text will now show ONLY this month's sum total
            txtTotalRevenue.setText(String.format(Locale.getDefault(), "Rs. %.2f", currentMonthHeroGrandTotal));

            // Update the horizontal scroll row layout and the history list view
            populateBusinessRowCards(businessTotalsMap);
            setupHistoryRecycler();
        });
    }

    private void populateBusinessRowCards(Map<String, Double> dataMap) {
        layoutBusinessCardsContainer.removeAllViews();

        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(380, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            card.setLayoutParams(params);
            card.setRadius(24f);
            card.setStrokeWidth(0);
            card.setCardBackgroundColor(Color.parseColor("#1A3050"));

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(20, 20, 20, 20);

            TextView lblName = new TextView(this);
            lblName.setText(entry.getKey().toUpperCase());
            lblName.setTextColor(Color.parseColor("#7A9CC0"));
            // FIXED: Explicitly specify COMPLEX_UNIT_SP to protect display layout scaling densities
            lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            lblName.setSingleLine(true);
            lblName.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView lblValue = new TextView(this);
            lblValue.setText(String.format(Locale.getDefault(), "Rs. %,.0f", entry.getValue()));
            lblValue.setTextColor(Color.parseColor("#4ADE80"));
            // FIXED: Explicitly specify COMPLEX_UNIT_SP here as well
            lblValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            lblValue.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            lblValue.setPadding(0, 8, 0, 0);

            innerLayout.addView(lblName);
            innerLayout.addView(lblValue);
            card.addView(innerLayout);
            layoutBusinessCardsContainer.addView(card);
        }
    }

    private void showAddRevenueDialog() {
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

            db.collection("revenues").add(record).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Revenue logged into registry", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                syncDataPipeline();
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
                DocumentSnapshot doc = currentMonthRevenueDocs.get(pos);
                h.t1.setText(String.format("%s • %s", doc.getString("selectedBusiness"), doc.getString("source")));
                h.t1.setTextColor(Color.parseColor("#F0F6FF"));
                h.t2.setText(String.format(Locale.getDefault(), "Rs. %.2f (Logged: %s) [Hold item to delete entry]", doc.getDouble("amount"), doc.getString("date")));
                h.t2.setTextColor(Color.parseColor("#7A9CC0"));

                // Handle corrections seamlessly with long-press deletions
                h.itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Correction Warning")
                            .setMessage("Delete this transaction row item entry permanently?")
                            .setPositiveButton("Delete", (d, w) -> {
                                db.collection("revenues").document(doc.getId()).delete().addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RevenueManagementActivity.this, "Entry removed safely", Toast.LENGTH_SHORT).show();
                                    syncDataPipeline();
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() { return currentMonthRevenueDocs.size(); }
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