package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.itextpdf.text.BaseColor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtilityReportActivity extends AppCompatActivity {

    private FrameLayout backButtonContainer;
    private TextView txtTotalExpenses;
    private RecyclerView recyclerReportItems;
    private BarChart barChartAnalytic;
    private Button btnGeneratePdf;

    private ArrayList<BillReportItem> receivedReportItems = new ArrayList<>();
    private double grandTotal = 0.0;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report);

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                receivedReportItems = savedInstanceState.getSerializable("SAVED_REPORT_ITEMS", ArrayList.class);
            } else {
                receivedReportItems = (ArrayList<BillReportItem>) savedInstanceState.getSerializable("SAVED_REPORT_ITEMS");
            }
        }

        if (receivedReportItems == null || receivedReportItems.isEmpty()) {
            if (getIntent() != null) {
                String targetKey = null;
                if (getIntent().hasExtra("FINAL_REPORT_ITEMS")) {
                    targetKey = "FINAL_REPORT_ITEMS";
                } else if (getIntent().hasExtra("STAGED_ITEMS")) {
                    targetKey = "STAGED_ITEMS";
                }

                if (targetKey != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        receivedReportItems = getIntent().getSerializableExtra(targetKey, ArrayList.class);
                    } else {
                        receivedReportItems = (ArrayList<BillReportItem>) getIntent().getSerializableExtra(targetKey);
                    }
                }
            }
        }

        initViews();
        calculateAndDisplayReportData();
        setupClickListeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("SAVED_REPORT_ITEMS", receivedReportItems);
    }

    private void initViews() {
        backButtonContainer = findViewById(R.id.backButtonContainer);
        txtTotalExpenses = findViewById(R.id.txtTotalAmount);
        recyclerReportItems = findViewById(R.id.recyclerReportItems);
        barChartAnalytic = findViewById(R.id.barChartAnalytic);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
    }

    private void calculateAndDisplayReportData() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) {
            if (txtTotalExpenses != null) txtTotalExpenses.setText("Rs. 0.00");
            Toast.makeText(this, "No bills data received for report!", Toast.LENGTH_LONG).show();
            return;
        }

        grandTotal = 0.0;
        for (BillReportItem item : receivedReportItems) {
            grandTotal += item.getAmount();
        }

        if (txtTotalExpenses != null) {
            txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
        } else {
            TextView txtBackup = findViewById(R.id.txtTotalAmount);
            if (txtBackup != null) {
                txtBackup.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
            }
        }

        recyclerReportItems.setLayoutManager(new LinearLayoutManager(this));
        ReportDisplayAdapter adapter = new ReportDisplayAdapter(receivedReportItems);
        recyclerReportItems.setAdapter(adapter);

        barChartAnalytic.post(this::renderBeautifulBarChart);
    }

    /**
     * Helper method to split multi-word labels (e.g., "Water Bill" -> "Water\nBill")
     */
    private String formatMultiLineLabel(String rawText) {
        if (rawText == null) return "";
        return rawText.replace(" ", "\n");
    }

    private void renderBeautifulBarChart() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) return;

        List<String> uniqueBillNames = new ArrayList<>();
        List<String> uniqueMonths = new ArrayList<>();

        for (BillReportItem item : receivedReportItems) {
            if (!uniqueBillNames.contains(item.getBillName())) uniqueBillNames.add(item.getBillName());
            if (!uniqueMonths.contains(item.getTargetMonth())) uniqueMonths.add(item.getTargetMonth());
        }

        if (uniqueBillNames.isEmpty() || uniqueMonths.isEmpty()) {
            barChartAnalytic.clear();
            return;
        }

        List<com.github.mikephil.charting.interfaces.datasets.IBarDataSet> dataSets = new ArrayList<>();
        int[] monthColors = {
                Color.parseColor("#9B8BFA"), Color.parseColor("#38BDF8"),
                Color.parseColor("#F59E0B"), Color.parseColor("#F43F5E"),
                Color.parseColor("#10B981"), Color.parseColor("#EC4899")
        };

        for (int m = 0; m < uniqueMonths.size(); m++) {
            String currentMonth = uniqueMonths.get(m);
            List<BarEntry> entriesForMonth = new ArrayList<>();

            for (int b = 0; b < uniqueBillNames.size(); b++) {
                String currentBillName = uniqueBillNames.get(b);
                float amount = 0f;

                for (BillReportItem item : receivedReportItems) {
                    if (item.getBillName().equals(currentBillName) && item.getTargetMonth().equals(currentMonth)) {
                        amount = (float) item.getAmount();
                        break;
                    }
                }
                entriesForMonth.add(new BarEntry(b, amount));
            }

            BarDataSet dataSet = new BarDataSet(entriesForMonth, currentMonth);
            dataSet.setColor(monthColors[m % monthColors.length]);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(10f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value > 0 ? String.format(Locale.getDefault(), "%.0f", value) : "";
                }
            });
            dataSets.add(dataSet);
        }

        BarData barData = new BarData(dataSets);
        float groupSpace = 0.30f;
        float barSpace = 0.05f;
        int numMonths = uniqueMonths.size();

        float barWidth = ((1.0f - groupSpace) / (numMonths > 0 ? numMonths : 1)) - barSpace;
        if (barWidth <= 0) barWidth = 0.1f;

        barData.setBarWidth(barWidth);
        barChartAnalytic.setData(barData);
        barChartAnalytic.groupBars(0f, groupSpace, barSpace);

        XAxis xAxis = barChartAnalytic.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < uniqueBillNames.size()) {
                    // Line break for label names (e.g., Water Bill -> Water\nBill)
                    return formatMultiLineLabel(uniqueBillNames.get(index));
                }
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#7A9CC0"));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.parseColor("#52759A"));
        xAxis.setAxisLineWidth(2f);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);

        float groupWidth = barData.getGroupWidth(groupSpace, barSpace);
        xAxis.setAxisMaximum(groupWidth * uniqueBillNames.size());

        barChartAnalytic.getAxisLeft().setTextColor(Color.parseColor("#7A9CC0"));
        barChartAnalytic.getAxisLeft().setGridColor(Color.parseColor("#1A3050"));
        barChartAnalytic.getAxisRight().setEnabled(false);
        barChartAnalytic.getDescription().setEnabled(false);
        barChartAnalytic.getLegend().setTextColor(Color.WHITE);
        barChartAnalytic.setExtraBottomOffset(25f); // Increased offset for multiline text
        barChartAnalytic.animateY(1000);
        barChartAnalytic.invalidate();
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> finish());
        btnGeneratePdf.setOnClickListener(v -> generatePdfReportFile());
    }

    private void generatePdfReportFile() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) return;

        Document document = new Document();
        String filename = "Utility_Report_" + System.currentTimeMillis() + ".pdf";

        try {
            OutputStream outputStream;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = (uri != null) ? getContentResolver().openOutputStream(uri) : null;
            } else {
                java.io.File file = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                outputStream = new java.io.FileOutputStream(file);
            }

            if (outputStream == null) throw new Exception("Output stream opening failed.");

            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLACK);
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);

            document.add(new Paragraph("Utility Bills Summary Report", titleFont));
            document.add(new Paragraph(" ", normalFont));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addTableCell(table, "Bill Name", boldFont, true);
            addTableCell(table, "Month", boldFont, true);
            addTableCell(table, "Amount", boldFont, true);

            for (BillReportItem item : receivedReportItems) {
                addTableCell(table, item.getBillName(), normalFont, false);
                addTableCell(table, item.getTargetMonth(), normalFont, false);
                addTableCell(table, String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()), normalFont, false);
            }
            addTableCell(table, "Total", boldFont, false);
            addTableCell(table, "", normalFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %.2f", grandTotal), boldFont, false);

            document.add(table);
            document.add(new Paragraph(" ", normalFont));

            int darkCardBgColor = Color.parseColor("#1E1E2F");

            // ── 1. Bar Chart with Dark Background ──
            document.add(new Paragraph("Monthly Utility Expense Comparison", sectionFont));
            document.add(new Paragraph(" ", normalFont));

            int w = barChartAnalytic.getWidth() > 0 ? barChartAnalytic.getWidth() : 600;
            int h = barChartAnalytic.getHeight() > 0 ? barChartAnalytic.getHeight() : 400;

            Bitmap barBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas barCanvas = new Canvas(barBitmap);

            Paint darkPaint = new Paint();
            darkPaint.setColor(darkCardBgColor);
            darkPaint.setStyle(Paint.Style.FILL);
            barCanvas.drawRect(0, 0, w, h, darkPaint);

            barChartAnalytic.draw(barCanvas);

            ByteArrayOutputStream barStream = new ByteArrayOutputStream();
            barBitmap.compress(Bitmap.CompressFormat.PNG, 100, barStream);
            Image chartImage = Image.getInstance(barStream.toByteArray());
            chartImage.scaleToFit(500, 300);
            chartImage.setAlignment(Element.ALIGN_CENTER);
            document.add(chartImage);

            document.add(new Paragraph(" ", normalFont));

            // ── 2. Pie Chart with Dark Background ──
            document.add(new Paragraph("Bill Distribution Breakdown", sectionFont));
            document.add(new Paragraph(" ", normalFont));

            PieChart pieChart = new PieChart(this);
            pieChart.setDrawEntryLabels(true);
            pieChart.setEntryLabelTextSize(11f);
            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setHoleColor(darkCardBgColor);
            pieChart.setCenterText("Bill\nDistribution");
            pieChart.setCenterTextColor(Color.WHITE);
            pieChart.setCenterTextSize(12f);
            pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setTextColor(Color.WHITE);
            pieChart.getLegend().setTextSize(10f);

            List<PieEntry> pieEntries = new ArrayList<>();
            for (BillReportItem item : receivedReportItems) {
                // Line break applied to Pie chart slice labels
                pieEntries.add(new PieEntry((float) item.getAmount(), formatMultiLineLabel(item.getBillName())));
            }
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            int[] pieColors = {
                    Color.parseColor("#9B8BFA"),
                    Color.parseColor("#38BDF8"),
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#F43F5E"),
                    Color.parseColor("#10B981"),
                    Color.parseColor("#EC4899")
            };
            pieDataSet.setColors(pieColors);
            pieDataSet.setValueTextColor(Color.WHITE);
            pieDataSet.setValueTextSize(11f);
            pieDataSet.setSliceSpace(3f);
            PieData pieData = new PieData(pieDataSet);
            pieChart.setData(pieData);

            pieChart.measure(
                    View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY));
            pieChart.layout(0, 0, 500, 500);

            Bitmap pieBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            Canvas pieCanvas = new Canvas(pieBitmap);

            pieCanvas.drawRect(0, 0, 500, 500, darkPaint);

            pieChart.draw(pieCanvas);

            ByteArrayOutputStream pieStream = new ByteArrayOutputStream();
            pieBitmap.compress(Bitmap.CompressFormat.PNG, 100, pieStream);
            Image pieImage = Image.getInstance(pieStream.toByteArray());
            pieImage.scaleToFit(450, 450);
            pieImage.setAlignment(Element.ALIGN_CENTER);
            document.add(pieImage);

            document.close();
            Toast.makeText(this, "PDF Saved to Downloads Folder", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to compile PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        if (isHeader) cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static class ReportDisplayAdapter extends RecyclerView.Adapter<ReportDisplayAdapter.ViewHolder> {
        private final ArrayList<BillReportItem> items;

        public ReportDisplayAdapter(ArrayList<BillReportItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_bill, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BillReportItem item = items.get(position);
            holder.name.setText(item.getBillName());
            holder.month.setText("Month: " + item.getTargetMonth());
            holder.amount.setText(String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()));

            if (holder.btnRemove != null) {
                holder.btnRemove.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, month, amount;
            View btnRemove;
            ViewHolder(View iv) {
                super(iv);
                name = iv.findViewById(R.id.txtSummaryName);
                month = iv.findViewById(R.id.txtSummaryMonth);
                amount = iv.findViewById(R.id.txtSummaryAmount);
                btnRemove = iv.findViewById(R.id.btnRemoveStagedItem);
            }
        }
    }
}