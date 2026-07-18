package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

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

        // 💡 1. savedInstanceState පරීක්ෂාව
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
        // 1. Safe guard against null or empty arrays right out of the gate
        if (receivedReportItems == null || receivedReportItems.isEmpty()) {
            if (txtTotalExpenses != null) txtTotalExpenses.setText("Rs. 0.00");
            Toast.makeText(this, "No bills data received for report!", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Clear out old calculations before iterating
        grandTotal = 0.0;
        for (BillReportItem item : receivedReportItems) {
            if (item != null) {
                grandTotal += item.getAmount();
            }
        }

        // 3. Update the UI text fields reliably
        if (txtTotalExpenses != null) {
            txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
        }

        recyclerReportItems.setLayoutManager(new LinearLayoutManager(this));
        ReportDisplayAdapter adapter = new ReportDisplayAdapter(receivedReportItems);
        recyclerReportItems.setAdapter(adapter);

        barChartAnalytic.post(this::renderBeautifulBarChart);
    }

    private void renderBeautifulBarChart() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) {
            barChartAnalytic.clear();
            return;
        }

        List<String> uniqueBillNames = new ArrayList<>();
        List<String> uniqueMonths = new ArrayList<>();

        // Populate unique lists while strictly avoiding null entries
        for (BillReportItem item : receivedReportItems) {
            if (item == null) continue;
            if (item.getBillName() != null && !uniqueBillNames.contains(item.getBillName())) {
                uniqueBillNames.add(item.getBillName());
            }
            if (item.getTargetMonth() != null && !uniqueMonths.contains(item.getTargetMonth())) {
                uniqueMonths.add(item.getTargetMonth());
            }
        }

        // If there isn't enough data to draw a chart comparison layout, clear it gracefully instead of crashing
        if (uniqueBillNames.isEmpty() || uniqueMonths.isEmpty()) {
            barChartAnalytic.clear();
            return;
        }

        List<com.github.mikephil.charting.interfaces.datasets.IBarDataSet> dataSets = new ArrayList<>();
        int[] monthColors = {
                Color.parseColor("#A78BFA"), Color.parseColor("#38BDF8"),
                Color.parseColor("#F59E0B"), Color.parseColor("#F43F5E"),
                Color.parseColor("#10B981"), Color.parseColor("#EC4899")
        };

        // FIX: Loop safely across all available items without expecting exactly 5 entries
        for (int m = 0; m < uniqueMonths.size(); m++) {
            String currentMonth = uniqueMonths.get(m);
            List<BarEntry> entriesForMonth = new ArrayList<>();

            for (int b = 0; b < uniqueBillNames.size(); b++) {
                String currentBillName = uniqueBillNames.get(b);
                float amount = 0f;

                for (BillReportItem item : receivedReportItems) {
                    if (item != null && currentBillName.equals(item.getBillName()) && currentMonth.equals(item.getTargetMonth())) {
                        amount = (float) item.getAmount();
                        break;
                    }
                }
                // Add a safe fallback entry even if the combination has no values
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
        if (barWidth <= 0) barWidth = 0.05f; // Hard fallback minimum width limit to prevent structural division errors

        barData.setBarWidth(barWidth);
        barChartAnalytic.setData(barData);

        // Only grouping views if we have multiple datasets to showcase
        if (uniqueMonths.size() > 1) {
            barChartAnalytic.groupBars(0f, groupSpace, barSpace);
        }

        XAxis xAxis = barChartAnalytic.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < uniqueBillNames.size()) return uniqueBillNames.get(index);
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#7A9CC0"));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.parseColor("#52759A"));
        xAxis.setAxisLineWidth(2f);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(uniqueMonths.size() > 1); // Only center alignment text if charts group up
        xAxis.setAxisMinimum(0f);

        float groupWidth = barData.getGroupWidth(groupSpace, barSpace);
        xAxis.setAxisMaximum(uniqueMonths.size() > 1 ? (groupWidth * uniqueBillNames.size()) : uniqueBillNames.size());

        barChartAnalytic.getAxisLeft().setTextColor(Color.parseColor("#7A9CC0"));
        barChartAnalytic.getAxisLeft().setGridColor(Color.parseColor("#1A3050"));
        barChartAnalytic.getAxisRight().setEnabled(false);
        barChartAnalytic.getDescription().setEnabled(false);
        barChartAnalytic.getLegend().setTextColor(Color.WHITE);
        barChartAnalytic.setExtraBottomOffset(15f);
        barChartAnalytic.animateY(1000);
        barChartAnalytic.invalidate();
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> finish());
        btnGeneratePdf.setOnClickListener(v -> generatePdfReportFile());
    }

    private void generatePdfReportFile() {
        if (receivedReportItems == null || receivedReportItems.isEmpty()) return;

        // 1. Chart capture must take place directly on the main UI thread canvas
        int w = barChartAnalytic.getWidth() > 0 ? barChartAnalytic.getWidth() : 600;
        int h = barChartAnalytic.getHeight() > 0 ? barChartAnalytic.getHeight() : 400;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        barChartAnalytic.draw(new Canvas(bitmap));

        Toast.makeText(this, "Generating PDF Report (iText 7)...", Toast.LENGTH_SHORT).show();

        // 2. Delegate file writing tasks to a background thread to prevent UI stuttering
        new Thread(() -> {
            String filename = "Utility_Report_" + System.currentTimeMillis() + ".pdf";
            boolean isSuccess = false;

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

                if (outputStream == null) throw new Exception("Failed to open storage output stream.");

                // iText 7 Initializer Structure: Writer -> PdfDocument -> Document layout
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Add clear headline
                Paragraph header = new Paragraph("Utility Bills Summary Report")
                        .setFontSize(20)
                        .setBold()
                        .setMarginBottom(15f);
                document.add(header);

                // Set up a 3-column structural layout grid table
                float[] columnWidths = {2f, 2f, 2f}; // Normalized proportional widths
                Table table = new Table(columnWidths);
                table.useAllAvailableWidth();

                // Headers
                addTableCell7(table, "Bill Name", true);
                addTableCell7(table, "Month", true);
                addTableCell7(table, "Amount", true);

                // Populating row datasets
                for (BillReportItem item : receivedReportItems) {
                    addTableCell7(table, item.getBillName(), false);
                    addTableCell7(table, item.getTargetMonth(), false);
                    addTableCell7(table, String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()), false);
                }

                // Summary Footer Row
                addTableCell7(table, "Total", true);
                addTableCell7(table, "", false);
                addTableCell7(table, String.format(Locale.getDefault(), "Rs. %.2f", grandTotal), true);

                document.add(table);

                // Empty space spacer paragraph
                document.add(new Paragraph("\n"));

                // Convert MPAndroidChart bitmap capture data safely into an iText 7 block element
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapData = stream.toByteArray();

                Image chartImage = new Image(ImageDataFactory.create(bitmapData));
                chartImage.setMaxWidth(500f);
                chartImage.setTextAlignment(TextAlignment.CENTER);

                document.add(chartImage);

                // Explicitly close the layout document context
                document.close();
                isSuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3. Deliver completion updates safely back on Android's main loop UI handler
            boolean finalIsSuccess = isSuccess;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalIsSuccess) {
                    Toast.makeText(UtilityReportActivity.this, "PDF Saved to Downloads Folder via iText 7", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UtilityReportActivity.this, "Failed to compile PDF document", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void addTableCell7(Table table, String text, boolean isBold) {
        Cell cell = new Cell().add(new Paragraph(text));
        cell.setPadding(8f);
        if (isBold) {
            cell.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }
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