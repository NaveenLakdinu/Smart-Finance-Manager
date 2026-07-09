package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.content.Intent;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
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
    private BarChart barChartAnalytic; // Fix: Switched from ImageView to MPAndroidChart BarChart
    private Button btnGeneratePdf;

    private ArrayList<BillReportItem> receivedReportItems;
    private double grandTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report);

        // Fix: Use a warning suppression block to safely unpack your serializable collection
        if (getIntent().hasExtra("FINAL_REPORT_ITEMS")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @SuppressWarnings("unchecked")
                ArrayList<BillReportItem> items = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("FINAL_REPORT_ITEMS", ArrayList.class);
                receivedReportItems = items != null ? items : new ArrayList<>();
            } else {
                @SuppressWarnings("unchecked")
                ArrayList<BillReportItem> items = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("FINAL_REPORT_ITEMS");
                receivedReportItems = items != null ? items : new ArrayList<>();
            }
        } else {
            receivedReportItems = new ArrayList<>();
        }

        initViews();
        calculateAndDisplayReportData();
        setupClickListeners();
    }

    private void initViews() {
        backButtonContainer = findViewById(R.id.backButtonContainer);
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses);
        recyclerReportItems = findViewById(R.id.recyclerReportItems);
        barChartAnalytic = findViewById(R.id.barChartAnalytic); // Fix: Bind to new BarChart view layout element
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
    }

    private void calculateAndDisplayReportData() {
        grandTotal = 0.0;

        for (BillReportItem item : receivedReportItems) {
            grandTotal += item.getAmount();
        }

        if (txtTotalExpenses != null) {
            txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
        }

        recyclerReportItems.setLayoutManager(new LinearLayoutManager(this));
        ReportDisplayAdapter adapter = new ReportDisplayAdapter(receivedReportItems);
        recyclerReportItems.setAdapter(adapter);

        // Fix: Call updated high-fidelity rendering method
        renderBeautifulBarChart();
    }

    /**
     * Renders a professional, animated bar chart matching your business analytics dashboard style.
     */
    private void renderBeautifulBarChart() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) return;

        // 1. Find all unique Bill Names and unique Months from our dataset
        List<String> uniqueBillNames = new ArrayList<>();
        List<String> uniqueMonths = new ArrayList<>();

        for (BillReportItem item : receivedReportItems) {
            if (!uniqueBillNames.contains(item.getBillName())) {
                uniqueBillNames.add(item.getBillName());
            }
            if (!uniqueMonths.contains(item.getTargetMonth())) {
                uniqueMonths.add(item.getTargetMonth());
            }
        }

        // 2. Build a BarDataSet for EACH unique month found
        List<com.github.mikephil.charting.interfaces.datasets.IBarDataSet> dataSets = new ArrayList<>();

        // Modern color palette matching your premium dashboard UI
        int[] monthColors = {
                Color.parseColor("#A78BFA"), // Purple
                Color.parseColor("#38BDF8"), // Blue
                Color.parseColor("#F59E0B"), // Amber
                Color.parseColor("#F43F5E"), // Rose
                Color.parseColor("#10B981"), // Emerald
                Color.parseColor("#EC4899")  // Pink
        };

        for (int m = 0; m < uniqueMonths.size(); m++) {
            String currentMonth = uniqueMonths.get(m);
            List<BarEntry> entriesForMonth = new ArrayList<>();

            // Loop through each bill name on our x-axis coordinate line
            for (int b = 0; b < uniqueBillNames.size(); b++) {
                String currentBillName = uniqueBillNames.get(b);
                float amount = 0f;

                // Search if we have a record matching this specific bill and specific month
                for (BillReportItem item : receivedReportItems) {
                    if (item.getBillName().equals(currentBillName) && item.getTargetMonth().equals(currentMonth)) {
                        amount = (float) item.getAmount();
                        break;
                    }
                }
                // Add the entry mapped to the index position of the bill
                entriesForMonth.add(new BarEntry(b, amount));
            }

            BarDataSet dataSet = new BarDataSet(entriesForMonth, currentMonth);
            dataSet.setColor(monthColors[m % monthColors.length]);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(10f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value > 0 ? String.format(Locale.getDefault(), "%.0f", value) : ""; // Hide 0 labels
                }
            });

            dataSets.add(dataSet);
        }

        BarData barData = new BarData(dataSets);

        // 3. Configure Group Spacing Math Constraints
        float groupSpace = 0.30f;
        float barSpace = 0.05f;
        int numMonths = uniqueMonths.size();
        // Formula: (barWidth + barSpace) * numMonths + groupSpace MUST EQUAL 1.0f
        float barWidth = ((1.0f - groupSpace) / numMonths) - barSpace;

        barData.setBarWidth(barWidth);
        barChartAnalytic.setData(barData);

        // Apply the mathematical spacing grouped configurations
        barChartAnalytic.groupBars(0f, groupSpace, barSpace);

        // 4. Style the X-Axis with the Bill Names
        XAxis xAxis = barChartAnalytic.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < uniqueBillNames.size()) {
                    return uniqueBillNames.get(index);
                }
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#7A9CC0"));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.parseColor("#52759A"));
        xAxis.setAxisLineWidth(2f);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true); // Centers the Bill Title text right below its group of month bars
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(barData.getGroupWidth(groupSpace, barSpace) * uniqueBillNames.size());

        // 5. Style Y-Axis
        barChartAnalytic.getAxisLeft().setTextColor(Color.parseColor("#7A9CC0"));
        barChartAnalytic.getAxisLeft().setGridColor(Color.parseColor("#1A3050"));
        barChartAnalytic.getAxisRight().setEnabled(false);
        barChartAnalytic.getDescription().setEnabled(false);

        // 6. FIX: Turn ON the Legend to display Months somewhere else
        com.github.mikephil.charting.components.Legend legend = barChartAnalytic.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(11f);
        legend.setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Add extra padding balances to prevent clip issues
        barChartAnalytic.setExtraBottomOffset(15f);
        barChartAnalytic.setExtraTopOffset(10f);

        barChartAnalytic.animateY(1000);
        barChartAnalytic.invalidate();
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityReportActivity.this, UtilityManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        btnGeneratePdf.setOnClickListener(v -> generatePdfReportFile());
    }

    /**
     * Compiles data into an iText PDF document, captures the active BarChart view,
     * and saves it directly to the device's Downloads directory.
     */
    private void generatePdfReportFile() {
        Document document = new Document();
        String filename = "FinGuard_Utility_Report_" + System.currentTimeMillis() + ".pdf";

        try {
            OutputStream outputStream;

            // Handle Scoped Storage path operations based on system build numbers
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

            // Set up document fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, com.itextpdf.text.BaseColor.DARK_GRAY);
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, com.itextpdf.text.BaseColor.GRAY);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            // Document Headers
            document.add(new Paragraph("FINGUARD UTILITY MANAGER", subTitleFont));
            Paragraph title = new Paragraph("Utility Bills Summary Report", titleFont);
            title.setSpacingAfter(20);
            document.add(title);

            // Initialize Grid Data Table matching the 3 structural columns
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingAfter(25);

            addTableCell(table, "Utility Description Category", boldFont, true);
            addTableCell(table, "Target Month", boldFont, true);
            addTableCell(table, "Paid Amount (Rs.)", boldFont, true);

            // Print item entries sequentially
            for (BillReportItem item : receivedReportItems) {
                addTableCell(table, item.getBillName() + " (" + item.getCategory() + ")", normalFont, false);
                addTableCell(table, item.getTargetMonth(), normalFont, false);
                addTableCell(table, String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()), normalFont, false);
            }

            // Print Grand Total row
            addTableCell(table, "TOTAL EXPENSES AMOUNT SUMMARY", boldFont, false);
            addTableCell(table, "", normalFont, false);
            addTableCell(table, String.format(Locale.getDefault(), "Rs. %.2f", grandTotal), boldFont, false);

            document.add(table);

            // Section boundary header for the chart export
            document.add(new Paragraph("Visual Analytics Workspace Chart Summary:", boldFont));

            // Fix: Capture the exact bitmap vector data directly from the interactive BarChart object
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

            Toast.makeText(this, "PDF Saved to Downloads: " + filename, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to compile report PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private static class ReportDisplayAdapter extends RecyclerView.Adapter<ReportDisplayAdapter.ReportViewHolder> {
        private final ArrayList<BillReportItem> displayItems;

        public ReportDisplayAdapter(ArrayList<BillReportItem> displayItems) {
            this.displayItems = displayItems;
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_bill, parent, false);
            return new ReportViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            BillReportItem item = displayItems.get(position);
            holder.name.setText(item.getBillName());
            holder.month.setText("Month: " + item.getTargetMonth());
            holder.amount.setText(String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()));

            if (holder.btnRemove != null) {
                holder.btnRemove.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return displayItems.size();
        }

        static class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView name, month, amount;
            View btnRemove;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.txtSummaryName);
                month = itemView.findViewById(R.id.txtSummaryMonth);
                amount = itemView.findViewById(R.id.txtSummaryAmount);
                btnRemove = itemView.findViewById(R.id.btnRemoveStagedItem);
            }
        }
    }
}