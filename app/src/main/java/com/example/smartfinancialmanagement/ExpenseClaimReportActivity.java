package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExpenseClaimReportActivity extends AppCompatActivity {

    private TextView txtTotalAmount, txtTotalCount;
    private TextView txtPendingCount, txtPendingAmount;
    private TextView txtApprovedCount, txtApprovedAmount;
    private TextView txtRejectedCount, txtRejectedAmount;
    private LinearLayout layoutClaimsList;
    private MaterialButton btnDownloadReport;

    private FirebaseFirestore db;
    private String uid;
    private int pendingCount, approvedCount, rejectedCount;
    private double pendingAmount, approvedAmount, rejectedAmount, totalAmount;
    private Map<String, Double> categoryMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_claim_report);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        initViews();
        loadReportData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtPendingAmount = findViewById(R.id.txtPendingAmount);
        txtApprovedCount = findViewById(R.id.txtApprovedCount);
        txtApprovedAmount = findViewById(R.id.txtApprovedAmount);
        txtRejectedCount = findViewById(R.id.txtRejectedCount);
        txtRejectedAmount = findViewById(R.id.txtRejectedAmount);
        layoutClaimsList = findViewById(R.id.layoutClaimsList);
        btnDownloadReport = findViewById(R.id.btnDownloadReport);
        btnDownloadReport.setOnClickListener(v -> downloadReport());
    }

    private void loadReportData() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("expense_claims")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pendingCount = 0; approvedCount = 0; rejectedCount = 0;
                    pendingAmount = 0; approvedAmount = 0; rejectedAmount = 0;
                    totalAmount = 0;
                    categoryMap.clear();
                    layoutClaimsList.removeAllViews();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String status = doc.getString("status");
                        String date = doc.getString("expenseDate");
                        Long amountLong = doc.getLong("amount");
                        double amt = amountLong != null ? amountLong.doubleValue() : 0;
                        totalAmount += amt;

                        if ("PENDING".equalsIgnoreCase(status)) { pendingCount++; pendingAmount += amt; }
                        else if ("APPROVED".equalsIgnoreCase(status)) { approvedCount++; approvedAmount += amt; }
                        else if ("REJECTED".equalsIgnoreCase(status)) { rejectedCount++; rejectedAmount += amt; }

                        if (category != null) categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amt);

                        addClaimItem(title, category, date, status, amt);
                    }

                    updateUI();
                    setupChart();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load report", Toast.LENGTH_SHORT).show());
    }

    private void updateUI() {
        txtTotalAmount.setText(String.format(Locale.US, "Rs %,.2f", totalAmount));
        txtTotalCount.setText(String.format(Locale.US, "%d claims", pendingCount + approvedCount + rejectedCount));
        txtPendingCount.setText(String.valueOf(pendingCount));
        txtPendingAmount.setText(String.format(Locale.US, "Rs %,.2f", pendingAmount));
        txtApprovedCount.setText(String.valueOf(approvedCount));
        txtApprovedAmount.setText(String.format(Locale.US, "Rs %,.2f", approvedAmount));
        txtRejectedCount.setText(String.valueOf(rejectedCount));
        txtRejectedAmount.setText(String.format(Locale.US, "Rs %,.2f", rejectedAmount));
    }

    private void addClaimItem(String title, String category, String date, String status, double amount) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_glass_card);
        card.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 12;
        card.setLayoutParams(params);

        TextView dot = new TextView(this);
        dot.setText("\u25CF");
        dot.setTextSize(18);
        if ("APPROVED".equalsIgnoreCase(status)) dot.setTextColor(Color.parseColor("#34D399"));
        else if ("REJECTED".equalsIgnoreCase(status)) dot.setTextColor(Color.parseColor("#F87171"));
        else dot.setTextColor(Color.parseColor("#FBBF24"));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(24, 0, 0, 0);
        info.setLayoutParams(infoParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(title != null ? title : "Untitled");
        titleTv.setTextColor(getResources().getColor(R.color.text_on_dark_primary));
        titleTv.setTextSize(14);
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(titleTv);

        TextView metaTv = new TextView(this);
        metaTv.setText(String.format("%s \u2022 %s", category != null ? category : "Other", date != null ? date : "N/A"));
        metaTv.setTextColor(getResources().getColor(R.color.text_on_dark_secondary));
        metaTv.setTextSize(12);
        info.addView(metaTv);

        TextView amountTv = new TextView(this);
        amountTv.setText(String.format(Locale.US, "Rs %,.2f", amount));
        amountTv.setTextColor(getResources().getColor(R.color.text_on_dark_primary));
        amountTv.setTextSize(14);
        amountTv.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(dot);
        card.addView(info);
        card.addView(amountTv);
        layoutClaimsList.addView(card);
    }

    private void setupChart() {
        PieChart pieChart = new PieChart(this);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextColor(getResources().getColor(R.color.text_on_dark_secondary));

        if (categoryMap.isEmpty()) {
            ((FrameLayout) findViewById(R.id.chartContainer)).addView(pieChart);
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : categoryMap.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(Color.parseColor("#60A5FA"), Color.parseColor("#FBBF24"), Color.parseColor("#34D399"), Color.parseColor("#A78BFA"));
        ds.setValueTextSize(12f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return String.format(Locale.US, "Rs %,.0f", v); }
        });

        pieChart.setData(new PieData(ds));
        pieChart.invalidate();
        ((FrameLayout) findViewById(R.id.chartContainer)).addView(pieChart);
    }

    private void downloadReport() {
        StringBuilder r = new StringBuilder();
        r.append("EXPENSE CLAIMS REPORT\n====================\n\n");
        r.append(String.format(Locale.US, "Total: %d claims (Rs %,.2f)\n", pendingCount+approvedCount+rejectedCount, totalAmount));
        r.append(String.format(Locale.US, "Pending: %d (Rs %,.2f)\n", pendingCount, pendingAmount));
        r.append(String.format(Locale.US, "Approved: %d (Rs %,.2f)\n", approvedCount, approvedAmount));
        r.append(String.format(Locale.US, "Rejected: %d (Rs %,.2f)\n\n", rejectedCount, rejectedAmount));
        r.append("BY CATEGORY:\n");
        for (Map.Entry<String, Double> e : categoryMap.entrySet()) {
            r.append(String.format(Locale.US, "%s: Rs %,.2f\n", e.getKey(), e.getValue()));
        }

        try {
            String fileName = "ExpenseReport_" + System.currentTimeMillis() + ".txt";
            OutputStream os;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                v.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                os = getContentResolver().openOutputStream(getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v));
            } else {
                os = new java.io.FileOutputStream(new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));
            }
            if (os != null) { os.write(r.toString().getBytes()); os.flush(); os.close(); }
            Toast.makeText(this, "Report saved to Downloads", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }
}
