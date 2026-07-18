package com.example.smartfinancialmanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SubscriptionReportActivity extends AppCompatActivity {

    private TextView txtTotalCost, txtTotalCount;
    private BarChart barChart;
    private RecyclerView recyclerReport;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_report);

        initViews();
        loadReport();
    }

    private void initViews() {
        txtTotalCost = findViewById(R.id.txtTotalCost);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        barChart = findViewById(R.id.barChart);
        recyclerReport = findViewById(R.id.recyclerReport);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadReport() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Subscription> allSubs = new ArrayList<>();
                    double totalMonthly = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subscription sub = doc.toObject(Subscription.class);
                        sub.setDocumentId(doc.getId());
                        allSubs.add(sub);

                        double amount = sub.getAmount();
                        if ("Yearly".equalsIgnoreCase(sub.getBillingCycle())) {
                            amount /= 12.0;
                        }
                        totalMonthly += amount;
                    }

                    txtTotalCost.setText(String.format(Locale.US, "LKR %,.2f", totalMonthly));
                    txtTotalCount.setText(allSubs.size() + " active subscription" + (allSubs.size() != 1 ? "s" : ""));

                    setupChart(allSubs);

                    ArrayList<Subscription> sortedList = new ArrayList<>(allSubs);
                    Collections.sort(sortedList, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));

                    RecentSubscriptionAdapter adapter = new RecentSubscriptionAdapter(this, sortedList);
                    recyclerReport.setLayoutManager(new LinearLayoutManager(this));
                    recyclerReport.setAdapter(adapter);
                    recyclerReport.setNestedScrollingEnabled(false);
                });
    }

    private void setupChart(List<Subscription> subs) {
        if (subs.isEmpty()) {
            barChart.setNoDataText("No subscriptions to display");
            barChart.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < subs.size(); i++) {
            entries.add(new BarEntry(i, (float) subs.get(i).getAmount()));
            String name = subs.get(i).getName();
            if (name.length() > 8) name = name.substring(0, 8) + "..";
            labels.add(name);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Cost (LKR)");
        dataSet.setColor(Color.parseColor("#00D4AA"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(800);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#1A2F50"));
        barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#1A2F50"));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGridColor(Color.parseColor("#1A2F50"));
        xAxis.setAxisLineColor(Color.parseColor("#1A2F50"));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45);

        barChart.setExtraBottomOffset(30f);
        barChart.invalidate();
    }
}
