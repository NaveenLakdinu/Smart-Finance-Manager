package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class FinancialReportsActivity extends AppCompatActivity {

    private Spinner spinnerReportType, spinnerMonth;
    private LinearLayout layoutMonthSelector, layoutDateRangePicker, layoutSummaryPreview;
    private MaterialButton btnGenerateReport;

    private final String[] reportTypes = {"Monthly Report", "Custom Range"};
    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_reports);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spinnerReportType = findViewById(R.id.spinnerReportType);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        layoutMonthSelector = findViewById(R.id.layoutMonthSelector);
        layoutDateRangePicker = findViewById(R.id.layoutDateRangePicker);
        layoutSummaryPreview = findViewById(R.id.layoutSummaryPreview);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        setupSpinners();

        btnGenerateReport.setOnClickListener(v -> {
            layoutSummaryPreview.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Report Generated", Toast.LENGTH_SHORT).show();
            // TODO: Bind actual Firestore data here
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reportTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(typeAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
    }
}
