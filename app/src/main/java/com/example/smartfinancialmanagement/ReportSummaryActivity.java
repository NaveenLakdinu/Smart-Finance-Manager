package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ReportSummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerSummary;
    private Button btnAddMore, btnGenerateReport;
    private ArrayList<BillReportItem> stagedReportItems;
    private BillSummaryAdapter adapter; // Reference to our updated adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report_summary);

        // Safely extract serializable data array from prior form intent bundles
        stagedReportItems = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("STAGED_ITEMS");
        if (stagedReportItems == null) {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupRecyclerView();
        setupActions();
    }

    private void initViews() {
        recyclerSummary = findViewById(R.id.recyclerSummaryItems);
        btnAddMore = findViewById(R.id.btnAddMore);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
    }

    private void setupRecyclerView() {
        recyclerSummary.setLayoutManager(new LinearLayoutManager(this));

        // Initialize our new SummaryAdapter with the item list containing the remove logic
        adapter = new BillSummaryAdapter(stagedReportItems);
        recyclerSummary.setAdapter(adapter);
    }

    private void setupActions() {
        // Redirect Route 1: Direct user back to the form layout input screen
        btnAddMore.setOnClickListener(v -> {
            if (stagedReportItems.size() >= 5) {
                Toast.makeText(this, "Limit reached! Maximum of 5 total bills can be chosen.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportFormActivity.class);
            intent.putExtra("STAGED_ITEMS", stagedReportItems); // Pass current active list backward safely
            startActivity(intent);
            finish();
        });

        // Redirect Route 2: Package final collection metrics forward to compilation tree
        btnGenerateReport.setOnClickListener(v -> {
            // Safety Check: Verify the user didn't cancel/remove all items using the card's cancel button
            if (stagedReportItems.isEmpty()) {
                Toast.makeText(this, "Your list is empty! Please add at least one bill entry first.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportActivity.class);
            intent.putExtra("FINAL_REPORT_ITEMS", stagedReportItems);
            startActivity(intent);
        });
    }
}