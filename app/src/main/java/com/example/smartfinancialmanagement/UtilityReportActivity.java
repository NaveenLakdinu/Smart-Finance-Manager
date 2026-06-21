package com.example.smartfinancialmanagement;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class UtilityReportActivity extends AppCompatActivity {

    private FrameLayout backButtonContainer;
    private TextView txtTotalExpenses;
    private RecyclerView recyclerReportItems;
    private ImageView barChartView;
    private Button btnGeneratePdf;

    private ArrayList<BillReportItem> receivedReportItems;
    private double grandTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_report);

        receivedReportItems = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("FINAL_REPORT_ITEMS");
        if (receivedReportItems == null) {
            receivedReportItems = new ArrayList<>();
        }

        initViews();
        calculateAndDisplayReportData();
        setupClickListeners();
    }

    private void initViews() {
        backButtonContainer = findViewById(R.id.backButtonContainer);
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses); // Main hero card sum display text
        recyclerReportItems = findViewById(R.id.recyclerReportItems); // Dynamic replacement list
        barChartView = findViewById(R.id.barChartView);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
    }

    private void calculateAndDisplayReportData() {
        grandTotal = 0.0;

        // 1. Calculate straightforward grand total for the main hero display card
        for (BillReportItem item : receivedReportItems) {
            grandTotal += item.getAmount();
        }

        if (txtTotalExpenses != null) {
            txtTotalExpenses.setText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal));
        }

        // 2. Load all selected items directly into the report panel layout list
        recyclerReportItems.setLayoutManager(new LinearLayoutManager(this));
        ReportDisplayAdapter adapter = new ReportDisplayAdapter(receivedReportItems);
        recyclerReportItems.setAdapter(adapter);

        // 3. Render vector visualization layout graphics
        renderVectorBarChart();
    }

    private void renderVectorBarChart() {
        barChartView.post(() -> {
            int width = barChartView.getWidth();
            int height = barChartView.getHeight();
            if (width <= 0 || height <= 0 || receivedReportItems.isEmpty()) return;

            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // Draw baseline structural axis layouts
            paint.setColor(Color.parseColor("#52759A"));
            paint.setStrokeWidth(4f);
            canvas.drawLine(80, height - 80, width - 40, height - 80, paint); // X Axis
            canvas.drawLine(80, 40, 80, height - 80, paint);  // Y Axis

            // Determine maximum amount value baseline to safely compute responsive column constraints
            double maxAmount = 0;
            for (BillReportItem item : receivedReportItems) {
                if (item.getAmount() > maxAmount) maxAmount = item.getAmount();
            }
            if (maxAmount == 0) maxAmount = 1.0;

            int numBars = receivedReportItems.size();
            int availableWidth = width - 120;
            int barWidth = (availableWidth / numBars) - 30;
            int maxBarHeight = height - 160;

            int[] palette = {Color.parseColor("#A78BFA"), Color.parseColor("#38BDF8"), Color.parseColor("#F59E0B"), Color.parseColor("#F43F5E"), Color.parseColor("#10B981")};

            for (int i = 0; i < numBars; i++) {
                BillReportItem item = receivedReportItems.get(i);
                int calculatedHeight = (int) ((item.getAmount() / maxAmount) * maxBarHeight);

                int left = 110 + i * (barWidth + 30);
                int top = (height - 80) - calculatedHeight;
                int right = left + barWidth;
                int bottom = height - 80;

                // Render customized column node block rectangles
                paint.setColor(palette[i % palette.length]);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(left, top, right, bottom, paint);

                // Print individual row tag descriptor values under the axis line
                paint.setColor(Color.WHITE);
                paint.setTextSize(22f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(item.getBillName(), (left + right) / 2f, height - 40, paint);
                canvas.drawText(item.getTargetMonth().substring(0, 3), (left + right) / 2f, height - 15, paint); // Short month name (e.g. Jan)

                // Cost values on top of bars
                canvas.drawText(String.format(Locale.getDefault(), "%.0f", item.getAmount()), (left + right) / 2f, top - 15, paint);
            }
            barChartView.setImageBitmap(bitmap);
        });
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> finish());
        btnGeneratePdf.setOnClickListener(v -> generatePdfReportFile());
    }

    private void generatePdfReportFile() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Header Title Context Rendering
        paint.setColor(Color.BLACK);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("finguard utility report pdf", 40, 60, paint);

        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        canvas.drawText("Generated Detailed Breakdown Log Statement", 40, 85, paint);

        // Build structural data layout grid tables
        int startX = 40;
        int startY = 130;
        int rowHeight = 30;
        String[] headers = {"Utility Description Category", "Target Month", "Paid Amount (Rs.)"};

        // Render Table Headers
        paint.setColor(Color.parseColor("#E2E8F0"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(startX, startY, startX + 510, startY + rowHeight, paint);

        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText(headers[0], startX + 10, startY + 20, paint);
        canvas.drawText(headers[1], startX + 220, startY + 20, paint);
        canvas.drawText(headers[2], startX + 380, startY + 20, paint);

        // Render Data Rows dynamically from stacked elements
        paint.setFakeBoldText(false);
        int currentY = startY + rowHeight;
        for (BillReportItem item : receivedReportItems) {
            paint.setColor(Color.parseColor("#CBD5E1"));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(startX, currentY + rowHeight, startX + 510, currentY + rowHeight, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(item.getBillName() + " (" + item.getCategory() + ")", startX + 10, currentY + 20, paint);
            canvas.drawText(item.getTargetMonth(), startX + 220, currentY + 20, paint);
            canvas.drawText(String.format(Locale.getDefault(), "Rs. %.2f", item.getAmount()), startX + 380, currentY + 20, paint);

            currentY += rowHeight;
        }

        // Render Summary Statement Line
        currentY += 25;
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        paint.setTextSize(14f);
        canvas.drawText("TOTAL EXPENSES AMOUNT SUMMARY: ", startX + 10, currentY + 20, paint);
        canvas.drawText(String.format(Locale.getDefault(), "Rs. %.2f", grandTotal), startX + 380, currentY + 20, paint);

        pdfDocument.finishPage(page);

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "FinGuard_Utility_Report.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Saved to Downloads: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to compile report PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    // Clean inline list display presenter adapter component mapping individual entries safely
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

            // Explicitly hide the cancel button icon view since this is a final presentation sheet
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
                btnRemove = itemView.findViewById(R.id.btnRemoveStagedItem); // Match custom layout ID references
            }
        }
    }
}