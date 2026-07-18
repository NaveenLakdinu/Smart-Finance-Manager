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

// Pure iText 7 Engine Imports
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

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

            if (outputStream == null) {
                throw new Exception("Failed to open output stream.");
            }

            // iText 7 Initialization Layout
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // iText 7 Font Setup
            PdfFont fontHelvetica = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontHelveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontHelveticaOblique = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // App Heading
            Paragraph appTitle = new Paragraph("SMART FINANCIAL MANAGEMENT")
                    .setFont(fontHelveticaOblique)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY);
            document.add(appTitle);

            // Report Title
            Paragraph title = new Paragraph("Business Analytics Report")
                    .setFont(fontHelveticaBold)
                    .setFontSize(22)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(5);
            document.add(title);

            // Scope Title
            Paragraph filterScope = new Paragraph("Filtered Scope: " + selectedBusinessFilter)
                    .setFont(fontHelveticaBold)
                    .setFontSize(12)
                    .setMarginBottom(20);
            document.add(filterScope);

            // iText 7 Table Structure (using column widths instead of percentages)
            float[] columnWidths = {250f, 250f};
            Table table = new Table(columnWidths);
            table.setMarginBottom(25);

            // Headings
            addTableCell(table, "Metric Description", fontHelveticaBold, true);
            addTableCell(table, "Amount (Rs.)", fontHelveticaBold, true);

            // Rows
            addTableCell(table, "Total Revenue", fontHelvetica, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthRevenue), fontHelvetica, false);

            addTableCell(table, "Total Expenses", fontHelvetica, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthExpense), fontHelvetica, false);

            addTableCell(table, "Net Profit / Loss", fontHelveticaBold, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %,.2f", currentMonthProfit), fontHelveticaBold, false);

            addTableCell(table, "Profit Margin Percentage", fontHelvetica, false);
            addTableCell(table, String.format(Locale.getDefault(), "%.2f%%", profitPercentage), fontHelvetica, false);

            document.add(table);

            // Chart Title Description
            Paragraph chartDesc = new Paragraph("Visual Analytics Workspace Chart Summary:")
                    .setFont(fontHelveticaBold)
                    .setFontSize(12);
            document.add(chartDesc);

            // Chart Processing to Image
            Bitmap bitmap = Bitmap.createBitmap(barChartAnalytic.getWidth(), barChartAnalytic.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            barChartAnalytic.draw(canvas);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // iText 7 Image Handling
            Image chartImage = new Image(ImageDataFactory.create(byteArray));
            chartImage.setMaxWidth(500f);
            chartImage.setMaxHeight(300f);
            chartImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            chartImage.setMarginTop(15);

            document.add(chartImage);

            // Close handles safely
            document.close();
            outputStream.close();

            Toast.makeText(this, "PDF Report saved to Downloads folder!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableCell(Table table, String text, PdfFont font, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font).setFontSize(12));
        cell.setPadding(10f);
        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(211, 211, 211)); // Light Gray Background
        }
        table.addCell(cell);
    }
}