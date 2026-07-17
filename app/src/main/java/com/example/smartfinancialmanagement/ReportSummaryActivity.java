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

    // 💡 DataBridge වෙනුවට Intent මඟින් හුවමාරු වන ආරක්ෂිත දත්ත ලැයිස්තුව
    private ArrayList<BillReportItem> stagedReportItems;
    private BillSummaryAdapter adapter;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report_summary);

        // 💡 FIX 1: Screen rotation වලදී හෝ පෙර තිරයෙන් එවූ දත්ත ලැයිස්තුව Intent එකෙන් ආරක්ෂිතව කියවීම
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

        // කිසිදු දත්තයක් ලැබී නොමැති නම් ආරක්ෂිතව අලුත් ලැයිස්තුවක් සෑදීම
        if (stagedReportItems == null) {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupRecyclerView();
        setupActions();
    }

    // 💡 Screen rotation සිදු වුවහොත් දත්ත ලැයිස්තුව මතකයේ රඳවා ගැනීම
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

        // 💡 List එක adapter එකට ලබා දීම (මෙහිදී අයිතමයක් මකා දැමුවහොත් මෙම Activity එකේ stagedReportItems ලැයිස්තුවෙන්ම ඉවත් වේ)
        adapter = new BillSummaryAdapter(stagedReportItems);
        recyclerSummary.setAdapter(adapter);
    }

    private void setupActions() {
        // තවත් බිල්පතක් ඇතුළත් කිරීමට නැවත Form එක වෙත යාම
        btnAddMore.setOnClickListener(v -> {
            if (stagedReportItems.size() >= 5) {
                Toast.makeText(this, "Limit reached! Maximum of 5 total bills can be chosen.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 💡 FIX 2: දැනට පවතින දත්ත ලැයිස්තුව නැවත Form එකට යැවීම
            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportFormActivity.class);
            intent.putExtra("STAGED_ITEMS", stagedReportItems);
            startActivity(intent);
            finish(); // SummaryActivity එක තාවකාලිකව වසා දමයි
        });

        // අවසාන නිල වාර්තාව සහ ප්‍රස්තාරය (Chart) පෙන්වන තිරයට යාම
        btnGenerateReport.setOnClickListener(v -> {
            if (stagedReportItems.isEmpty()) {
                Toast.makeText(this, "Your list is empty! Please add at least one bill entry first.", Toast.LENGTH_LONG).show();
                return;
            }

            // 💡 FIX 3: අවසාන වශයෙන් සකස් කරගත් මුළු දත්ත ලැයිස්තුවම UtilityReportActivity (Report Panel) එකට Pass කිරීම
            Intent intent = new Intent(ReportSummaryActivity.this, UtilityReportActivity.class);
            intent.putExtra("FINAL_REPORT_ITEMS", stagedReportItems);
            startActivity(intent);
            finish(); // දත්ත සියල්ල ආරක්ෂිතව යවා අවසන් බැවින් මෙම තිරය වසා දමයි
        });
    }
}