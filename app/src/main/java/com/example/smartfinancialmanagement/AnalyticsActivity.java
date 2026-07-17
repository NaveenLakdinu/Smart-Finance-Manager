package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView txtRevenueDisplay, txtExpenseDisplay, txtProfitDisplay, txtPercentageDisplay;
    private View btnGoToRevenue, btnGoToExpense;
    private Button btnGeneratePDF;
    private BarChart barChartAnalytic;
    private Spinner spinnerBusinessFilter;

    private FirebaseFirestore db;
    private List<String> filterOptionsList = new ArrayList<>();
    private String selectedBusinessFilter = "All Businesses"; // Default Option

    private double currentMonthRevenue = 0.0;
    private double currentMonthExpense = 0.0;
    private double currentMonthProfit = 0.0;
    private double profitPercentage = 0.0;

    private SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_analytic);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupFilterSpinner();
    }

    private void initializeViews() {
        txtRevenueDisplay = findViewById(R.id.txtRevenueDisplay);
        txtExpenseDisplay = findViewById(R.id.txtExpenseDisplay);
        txtProfitDisplay = findViewById(R.id.txtProfitDisplay);
        txtPercentageDisplay = findViewById(R.id.txtPercentageDisplay);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);
        barChartAnalytic = findViewById(R.id.barChartAnalytic);
        spinnerBusinessFilter = findViewById(R.id.spinnerBusinessFilter);
        btnGoToRevenue = findViewById(R.id.layoutRevenueCard);
        btnGoToExpense = findViewById(R.id.layoutExpenseCard);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnGeneratePDF.setOnClickListener(v -> generateAnalyticsPDF());

        btnGoToRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(AnalyticsActivity.this, RevenueManagementActivity.class);
            startActivity(intent);
        });

        btnGoToExpense.setOnClickListener(v -> {
            Intent intent = new Intent(AnalyticsActivity.this, ExpenseManagementActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterSpinner() {
        filterOptionsList.clear();
        filterOptionsList.add("All Businesses");

        db.collection("businesses").get().addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String bName = doc.getString("businessName");
                if (bName != null) {
                    filterOptionsList.add(bName);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filterOptionsList);
            spinnerBusinessFilter.setAdapter(adapter);

            spinnerBusinessFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedBusinessFilter = filterOptionsList.get(position);
                    fetchMonthlyAnalytics();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    private void fetchMonthlyAnalytics() {
        String currentMonthToken = yearMonthFormat.format(Calendar.getInstance().getTime());

        // 1. Get Revenues
        db.collection("revenues").get().addOnSuccessListener(revSnapshots -> {
            currentMonthRevenue = 0.0;
            for (DocumentSnapshot doc : revSnapshots.getDocuments()) {
                String dateStr = doc.getString("date");
                String bName = doc.getString("selectedBusiness");
                Double amount = doc.getDouble("amount");

                if (amount != null && dateStr != null && dateStr.startsWith(currentMonthToken)) {
                    if (selectedBusinessFilter.equals("All Businesses") || selectedBusinessFilter.equals(bName)) {
                        currentMonthRevenue += amount;
                    }
                }
            }

            // 2. Get Expenses
            db.collection("expenses").get().addOnSuccessListener(expSnapshots -> {
                currentMonthExpense = 0.0;
                for (DocumentSnapshot doc : expSnapshots.getDocuments()) {
                    String dateStr = doc.getString("date");
                    String bName = doc.getString("selectedBusiness");
                    Double amount = doc.getDouble("amount");

                    if (amount != null && dateStr != null && dateStr.startsWith(currentMonthToken)) {
                        if (selectedBusinessFilter.equals("All Businesses") || selectedBusinessFilter.equals(bName)) {
                            currentMonthExpense += amount;
                        }
                    }
                }

                calculateFinalMetrics();
            });
        });
    }

    private void calculateFinalMetrics() {
        currentMonthProfit = currentMonthRevenue - currentMonthExpense;

        if (currentMonthRevenue > 0) {
            profitPercentage = (currentMonthProfit / currentMonthRevenue) * 100;
        } else {
            profitPercentage = 0.0;
        }

        txtRevenueDisplay.setText(String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthRevenue));
        txtExpenseDisplay.setText(String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthExpense));
        txtProfitDisplay.setText(String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthProfit));

        if (currentMonthProfit >= 0) {
            txtProfitDisplay.setTextColor(Color.parseColor("#00D4AA"));
            txtPercentageDisplay.setText(String.format(Locale.getDefault(), "+%.1f%% Profit Margin", profitPercentage));
            txtPercentageDisplay.setTextColor(Color.parseColor("#00D4AA"));
        } else {
            txtProfitDisplay.setTextColor(Color.parseColor("#FF5555"));
            txtPercentageDisplay.setText(String.format(Locale.getDefault(), "%.1f%% Net Loss", profitPercentage));
            txtPercentageDisplay.setTextColor(Color.parseColor("#FF5555"));
        }

        updateBarChartDisplay();
    }

    private void updateBarChartDisplay() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) currentMonthRevenue));
        entries.add(new BarEntry(1f, (float) currentMonthExpense));
        entries.add(new BarEntry(2f, (float) currentMonthProfit));

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Analytics");

        int[] colors = new int[]{
                Color.parseColor("#4ADE80"),
                Color.parseColor("#FF5555"),
                Color.parseColor("#00D4AA")
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChartAnalytic.setData(barData);

        final String[] labels = new String[]{"Revenue", "Expenses", "Net Profit"};
        XAxis xAxis = barChartAnalytic.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                }
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#7A9CC0"));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChartAnalytic.getAxisLeft().setTextColor(Color.WHITE);
        barChartAnalytic.getAxisLeft().setGridColor(Color.parseColor("#1A3050"));
        barChartAnalytic.getAxisRight().setEnabled(false);
        barChartAnalytic.getDescription().setEnabled(false);
        barChartAnalytic.getLegend().setEnabled(false);

        barChartAnalytic.animateY(1000);
        barChartAnalytic.invalidate();
    }

    private void generateAnalyticsPDF() {
        Document document = new Document();
        String filename = "Analytics_Report_" + selectedBusinessFilter.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";

        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(uri);
            } else {
                java.io.File file = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                outputStream = new java.io.FileOutputStream(file);
            }

            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, com.itextpdf.text.BaseColor.DARK_GRAY);
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, com.itextpdf.text.BaseColor.GRAY);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("SMART FINANCIAL MANAGEMENT", subTitleFont));
            Paragraph title = new Paragraph("Business Analytics Report", titleFont);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph filterScope = new Paragraph("Filtered Scope: " + selectedBusinessFilter, boldFont);
            filterScope.setSpacingAfter(20);
            document.add(filterScope);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(25);

            addTableCell(table, "Metric Description", boldFont, true);
            addTableCell(table, "Amount (Rs.)", boldFont, true);

            addTableCell(table, "Total Revenue", normalFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthRevenue), normalFont, false);

            addTableCell(table, "Total Expenses", normalFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthExpense), normalFont, false);

            addTableCell(table, "Net Profit / Loss", boldFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthProfit), boldFont, false);

            addTableCell(table, "Profit Margin Percentage", normalFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "%.2f%%", profitPercentage), normalFont, false);

            document.add(table);

            document.add(new Paragraph("Visual Analytics Workspace Chart Summary:", boldFont));

            Bitmap bitmap = Bitmap.createBitmap(barChartAnalytic.getWidth(), barChartAnalytic.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            barChartAnalytic.draw(canvas);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Image chartImage = Image.getInstance(byteArray);
            chartImage.scaleToFit(500, 300);
            chartImage.setAlignment(Element.ALIGN_CENTER);
            chartImage.setSpacingBefore(15);

            document.add(chartImage);
            document.close();

            Toast.makeText(this, "PDF Report saved to Downloads folder!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(10);
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        }
        table.addCell(cell);
    }
}