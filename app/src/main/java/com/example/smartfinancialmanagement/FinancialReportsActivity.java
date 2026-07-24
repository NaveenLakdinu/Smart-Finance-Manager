package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FinancialReportsActivity extends AppCompatActivity {

    private Spinner spinnerReportType, spinnerMonth;
    private LinearLayout layoutMonthSelector, layoutDateRangePicker, layoutSummaryPreview;
    private MaterialButton btnGenerateReport;
    
    private PieChart chartLoans, chartSubscriptions;
    private BarChart chartUtilityBills;
    private HorizontalBarChart chartSavings;

    private TextView txtReportBalance, txtReportIncome, txtReportSavings, txtReportLoans, txtReportSubscriptions, txtReportUtilities;

    private double mTotalIncome = 0;
    private double mTotalSavings = 0;
    private double mTotalLoans = 0;
    private double mTotalSubscriptions = 0;
    private double mTotalUtilities = 0;
    private double mDirectIncome = 0;
    private double mBudgetIncome = 0;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String[] reportTypes = {"Monthly Report", "Yearly Report"};
    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String[] years;

    private ActivityResultLauncher<String> createPdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_reports);

        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> {
                    if (uri != null) {
                        savePdfToUri(uri);
                    } else {
                        Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spinnerReportType = findViewById(R.id.spinnerReportType);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        layoutMonthSelector = findViewById(R.id.layoutMonthSelector);
        layoutDateRangePicker = findViewById(R.id.layoutDateRangePicker);
        layoutSummaryPreview = findViewById(R.id.layoutSummaryPreview);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        chartLoans = findViewById(R.id.chartLoans);
        chartSubscriptions = findViewById(R.id.chartSubscriptions);
        chartUtilityBills = findViewById(R.id.chartUtilityBills);
        chartSavings = findViewById(R.id.chartSavings);

        txtReportBalance = findViewById(R.id.txtReportBalance);
        txtReportIncome = findViewById(R.id.txtReportIncome);
        txtReportSavings = findViewById(R.id.txtReportSavings);
        txtReportLoans = findViewById(R.id.txtReportLoans);
        txtReportSubscriptions = findViewById(R.id.txtReportSubscriptions);
        txtReportUtilities = findViewById(R.id.txtReportUtilities);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupSpinners();

        btnGenerateReport.setOnClickListener(v -> {
            layoutSummaryPreview.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Fetching Report Data...", Toast.LENGTH_SHORT).show();
            fetchAndDisplayData();
        });

        MaterialButton btnDownloadPdfReport = findViewById(R.id.btnDownloadPdfReport);
        if (btnDownloadPdfReport != null) {
            btnDownloadPdfReport.setOnClickListener(v -> generatePdfReport());
        }
    }

    private void setupSpinners() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        years = new String[]{String.valueOf(currentYear - 2), String.valueOf(currentYear - 1), String.valueOf(currentYear), String.valueOf(currentYear + 1), String.valueOf(currentYear + 2)};

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reportTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(typeAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinnerMonth.setAdapter(monthAdapter);
                    spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));
                } else {
                    spinnerMonth.setAdapter(yearAdapter);
                    spinnerMonth.setSelection(2); // Index 2 is currentYear in the years array
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));
    }

    private void fetchAndDisplayData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        boolean isYearly = spinnerReportType.getSelectedItemPosition() == 1;
        int selectedMonth = spinnerMonth.getSelectedItemPosition();
        int selectedYear = isYearly ? Integer.parseInt(spinnerMonth.getSelectedItem().toString()) : Calendar.getInstance().get(Calendar.YEAR);

        // Reset totals
        mTotalIncome = 0;
        mTotalSavings = 0;
        mTotalLoans = 0;
        mTotalSubscriptions = 0;
        mTotalUtilities = 0;
        mDirectIncome = 0;
        mBudgetIncome = 0;
        updateSummaryUI();

        // Fetch Direct Incomes
        db.collection("users").document(uid).collection("incomes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double amount = doc.getDouble("amount");
                if (amount != null) mDirectIncome += amount;
            }
            recalculateTotalIncome();
        });

        // Fetch Budget Incomes
        db.collection("users").document(uid).collection("budgetPlans").get().addOnSuccessListener(queryDocumentSnapshots -> {
            BudgetModel latestBudget = null;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                BudgetModel budget = doc.toObject(BudgetModel.class);
                if (latestBudget == null) {
                    latestBudget = budget;
                } else if (budget.getCreatedAt() != null && latestBudget.getCreatedAt() != null) {
                    if (budget.getCreatedAt().after(latestBudget.getCreatedAt())) {
                        latestBudget = budget;
                    }
                }
            }
            if (latestBudget != null) {
                mBudgetIncome = latestBudget.getSemesterIncome();
            }
            recalculateTotalIncome();
        });

        // 1. Fetch Loans
        db.collection("users").document(uid).collection("loans").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<PieEntry> entries = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                String name = doc.getString("loanName");
                Double amount = doc.getDouble("principalAmount");
                if (name != null && amount != null) {
                    entries.add(new PieEntry(amount.floatValue(), name));
                }
            }
            if (!entries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(entries, "Loans");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                chartLoans.setData(new PieData(dataSet));
            } else {
                chartLoans.clear();
            }
            chartLoans.getDescription().setEnabled(false);
            chartLoans.invalidate();

            mTotalLoans = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double amount = doc.getDouble("principalAmount");
                if (amount != null) mTotalLoans += amount;
            }
            updateSummaryUI();
        });

        // 2. Fetch Subscriptions
        db.collection("users").document(uid).collection("subscriptions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<PieEntry> entries = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                String name = doc.getString("serviceName");
                Double amount = doc.getDouble("amount");
                if (name != null && amount != null) {
                    entries.add(new PieEntry(amount.floatValue(), name));
                }
            }
            if (!entries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(entries, "Subscriptions");
                dataSet.setColors(ColorTemplate.PASTEL_COLORS);
                chartSubscriptions.setData(new PieData(dataSet));
            } else {
                chartSubscriptions.clear();
            }
            chartSubscriptions.getDescription().setEnabled(false);
            chartSubscriptions.invalidate();

            mTotalSubscriptions = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double amount = doc.getDouble("amount");
                if (amount != null) mTotalSubscriptions += amount;
            }
            updateSummaryUI();
        });

        // 3. Fetch Utility Bills
        db.collection("users").document(uid).collection("utility_bills").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<BarEntry> entries = new ArrayList<>();
            float i = 0f;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double amount = doc.getDouble("amount");
                if (amount != null) {
                    entries.add(new BarEntry(i++, amount.floatValue()));
                }
            }
            if (!entries.isEmpty()) {
                BarDataSet dataSet = new BarDataSet(entries, "Utility Bills");
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                chartUtilityBills.setData(new BarData(dataSet));
            } else {
                chartUtilityBills.clear();
            }
            chartUtilityBills.getDescription().setEnabled(false);
            chartUtilityBills.invalidate();

            mTotalUtilities = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double amount = doc.getDouble("amount");
                if (amount != null) mTotalUtilities += amount;
            }
            updateSummaryUI();
        });

        // 4. Fetch Savings
        db.collection("users").document(uid).collection("savings").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<BarEntry> entries = new ArrayList<>();
            float i = 0f;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double target = doc.getDouble("targetAmount");
                Double current = doc.getDouble("currentAmount");
                if (target != null && current != null) {
                    float remaining = Math.max(0f, target.floatValue() - current.floatValue());
                    entries.add(new BarEntry(i++, new float[]{current.floatValue(), remaining}));
                }
            }
            if (!entries.isEmpty()) {
                BarDataSet dataSet = new BarDataSet(entries, "Current vs Remaining");
                dataSet.setColors(new int[]{ColorTemplate.VORDIPLOM_COLORS[0], ColorTemplate.VORDIPLOM_COLORS[1]});
                dataSet.setStackLabels(new String[]{"Current", "Remaining"});
                chartSavings.setData(new BarData(dataSet));
            } else {
                chartSavings.clear();
            }
            chartSavings.getDescription().setEnabled(false);
            chartSavings.invalidate();

            mTotalSavings = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (!matchesDateFilter(doc, isYearly, selectedMonth, selectedYear)) continue;
                Double current = doc.getDouble("currentAmount");
                if (current != null) mTotalSavings += current;
            }
            updateSummaryUI();
        });
    }

    private void recalculateTotalIncome() {
        mTotalIncome = mDirectIncome > 0 ? mDirectIncome : mBudgetIncome;
        updateSummaryUI();
    }

    private void updateSummaryUI() {
        if (txtReportIncome != null) txtReportIncome.setText(CurrencyHelper.formatMoney(this, mTotalIncome));
        if (txtReportSavings != null) txtReportSavings.setText(CurrencyHelper.formatMoney(this, mTotalSavings));
        if (txtReportLoans != null) txtReportLoans.setText(CurrencyHelper.formatMoney(this, mTotalLoans));
        if (txtReportSubscriptions != null) txtReportSubscriptions.setText(CurrencyHelper.formatMoney(this, mTotalSubscriptions));
        if (txtReportUtilities != null) txtReportUtilities.setText(CurrencyHelper.formatMoney(this, mTotalUtilities));

        double currentBalance = mTotalIncome - mTotalSavings - mTotalLoans - mTotalSubscriptions - mTotalUtilities;
        if (txtReportBalance != null) txtReportBalance.setText(CurrencyHelper.formatMoney(this, currentBalance));
    }

    private boolean matchesDateFilter(QueryDocumentSnapshot doc, boolean isYearly, int selectedMonth, int selectedYear) {
        Object createdAtObj = doc.get("createdAt");
        if (createdAtObj == null) {
            createdAtObj = doc.get("timestamp"); // fallback
        }
        
        if (createdAtObj == null) return true; // Include if no date field is found just to be safe

        long timeInMillis = 0;
        if (createdAtObj instanceof Number) {
            timeInMillis = ((Number) createdAtObj).longValue();
        } else if (createdAtObj instanceof com.google.firebase.Timestamp) {
            timeInMillis = ((com.google.firebase.Timestamp) createdAtObj).toDate().getTime();
        } else if (createdAtObj instanceof java.util.Date) {
            timeInMillis = ((java.util.Date) createdAtObj).getTime();
        } else {
            return true; // Unknown type, just include it
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        int docYear = cal.get(Calendar.YEAR);
        int docMonth = cal.get(Calendar.MONTH);

        if (isYearly) {
            return docYear == selectedYear;
        } else {
            return docYear == selectedYear && docMonth == selectedMonth;
        }
    }

    private void generatePdfReport() {
        if (layoutSummaryPreview.getVisibility() != View.VISIBLE) {
            Toast.makeText(this, "Please generate a report first", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "Financial_Report_" + System.currentTimeMillis() + ".pdf";
        createPdfLauncher.launch(fileName);
    }

    private void savePdfToUri(Uri uri) {
        PdfDocument document = new PdfDocument();
        int width = layoutSummaryPreview.getWidth();
        int height = layoutSummaryPreview.getHeight();

        if (width == 0 || height == 0) {
            Toast.makeText(this, "Layout is not fully rendered yet", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Draw the layout onto the PDF page
        layoutSummaryPreview.draw(page.getCanvas());
        document.finishPage(page);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                document.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
        
        document.close();
    }
}
