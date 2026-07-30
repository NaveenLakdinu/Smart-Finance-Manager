package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ReportSummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerSummary;
    private Button btnAddMore, btnGenerateReport;

    private ArrayList<BillReportItem> stagedReportItems;
    private BillSummaryAdapter adapter;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report_summary);

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                stagedReportItems = savedInstanceState.getSerializable("STAGED_ITEMS", ArrayList.class);
            } else {
                stagedReportItems = (ArrayList<BillReportItem>) savedInstanceState.getSerializable("STAGED_ITEMS");
            }
        }

        if (stagedReportItems == null) {
            if (getIntent() != null && getIntent().hasExtra("STAGED_ITEMS")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stagedReportItems = getIntent().getSerializableExtra("STAGED_ITEMS", ArrayList.class);
                } else {
                    stagedReportItems = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("STAGED_ITEMS");
                }
            }
        }

        if (stagedReportItems == null) {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupRecyclerView();
        setupActions();
        updateButtonStates();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("STAGED_ITEMS", stagedReportItems);
    }

    private void initViews() {
        recyclerSummary = findViewById(R.id.recyclerSummaryItems);
        btnAddMore = findViewById(R.id.btnAddMore);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
    }

    private void setupRecyclerView() {
        recyclerSummary.setLayoutManager(new LinearLayoutManager(this));

        // Pass this::updateButtonStates as the removal listener callback
        adapter = new BillSummaryAdapter(stagedReportItems, this::updateButtonStates);
        recyclerSummary.setAdapter(adapter);
    }

    /**
     * Dynamically updates button states and visual appearance based on list constraints.
     */
    private void updateButtonStates() {
        int count = stagedReportItems.size();

        // Re-enable 'Add More' button whenever count drops under 5
        boolean canAddMore = count < 5;
        btnAddMore.setEnabled(canAddMore);
        btnAddMore.setAlpha(canAddMore ? 1.0f : 0.5f);

        // Keep 'Generate Report' active so user can tap it and receive explanatory feedback
        btnGenerateReport.setEnabled(true);
        btnGenerateReport.setAlpha(count > 1 ? 1.0f : 0.7f);
    }

    private void setupActions() {

        btnAddMore.setOnClickListener(v -> {
            if (stagedReportItems.size() >= 5) {
                Toast.makeText(this, "Limit reached! Maximum of 5 total bills can be added.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportFormActivity.class);
            intent.putExtra("STAGED_ITEMS", stagedReportItems);
            startActivity(intent);
            finish();
        });

        btnGenerateReport.setOnClickListener(v -> {
            if (stagedReportItems.isEmpty()) {
                Toast.makeText(this, "Your list is empty! Please add bill entries first.", Toast.LENGTH_LONG).show();
                return;
            }

            // Notice to user when only 1 bill is added
            if (stagedReportItems.size() == 1) {
                Toast.makeText(this, "Please add at least one more bill. A report requires more than 1 bill to generate a comparison.", Toast.LENGTH_LONG).show();
                return;
            }

            // Proceed when 2 or more bills are staged
            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportActivity.class);
            intent.putExtra("FINAL_REPORT_ITEMS", stagedReportItems);
            startActivity(intent);
            finish();
        });
    }
}