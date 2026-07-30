package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import com.example.smartfinancialmanagement.CurrencyHelper;

public class SavingReportResultActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvReportPeriod, tvHealthScore, tvHealthScoreValue;
    private TextView tvTotalGoals, tvActiveGoals, tvCompletedGoals;
    private TextView tvTotalTarget, tvTotalSaved, tvTotalRemaining, tvAvgProgress;
    private ProgressBar pbAvgProgress;

    private CollectionReference databaseReference;
    private String userId;
    private String reportType, monthYear, startDateStr, endDateStr;
    private SimpleDateFormat dateFormat;
    private List<SavingModel> filteredSavings = new ArrayList<>();
    
    private ActivityResultLauncher<String> createPdfLauncher;
    private ActivityResultLauncher<String> createCsvLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_report_result);

        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> {
                    if (uri != null) {
                        savePdfToUri(uri);
                    } else {
                        Toast.makeText(this, "Export cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        createCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) {
                        saveCsvToUri(uri);
                    } else {
                        Toast.makeText(this, "Export cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        reportType = getIntent().getStringExtra("REPORT_TYPE");
        monthYear = getIntent().getStringExtra("MONTH_YEAR");
        startDateStr = getIntent().getStringExtra("START_DATE");
        endDateStr = getIntent().getStringExtra("END_DATE");

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupFirebase();
        setupListeners();
        setReportPeriod();
        fetchDataAndCalculate();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvReportPeriod = findViewById(R.id.tvReportPeriod);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        tvHealthScoreValue = findViewById(R.id.tvHealthScoreValue);
        tvTotalGoals = findViewById(R.id.tvTotalGoals);
        tvActiveGoals = findViewById(R.id.tvActiveGoals);
        tvCompletedGoals = findViewById(R.id.tvCompletedGoals);
        tvTotalTarget = findViewById(R.id.tvTotalTarget);
        tvTotalSaved = findViewById(R.id.tvTotalSaved);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvAvgProgress = findViewById(R.id.tvAvgProgress);
        pbAvgProgress = findViewById(R.id.pbAvgProgress);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "test_user";
        }
        databaseReference = FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        findViewById(R.id.btnExportPdf).setOnClickListener(v -> {
            if (filteredSavings.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "Saving_Report_" + System.currentTimeMillis() + ".pdf";
            createPdfLauncher.launch(fileName);
        });

        findViewById(R.id.btnExportCsv).setOnClickListener(v -> {
            if (filteredSavings.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
                return;
            }
            String fileName = "Saving_Report_" + System.currentTimeMillis() + ".csv";
            createCsvLauncher.launch(fileName);
        });
    }

    private void setReportPeriod() {
        if ("MONTH".equals(reportType)) {
            if (monthYear == null || monthYear.isEmpty() || monthYear.equals("MM/YYYY")) {
                tvReportPeriod.setText("Period: All Time");
            } else {
                tvReportPeriod.setText("Period: " + monthYear);
            }
        } else if ("CUSTOM".equals(reportType)) {
            if (startDateStr == null || startDateStr.isEmpty() || endDateStr == null || endDateStr.isEmpty()) {
                tvReportPeriod.setText("Period: All Time");
            } else {
                tvReportPeriod.setText("Period: " + startDateStr + " to " + endDateStr);
            }
        } else {
            tvReportPeriod.setText("Period: All Time");
        }
    }

    private void fetchDataAndCalculate() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int totalGoals = 0;
                int activeGoals = 0;
                int completedGoals = 0;
                double totalTarget = 0;
                double totalSaved = 0;
                double totalProgressSum = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    SavingModel saving = document.toObject(SavingModel.class);
                    if (saving != null && isWithinDateRange(saving.getStartDate(), saving.getTargetDate())) {
                        filteredSavings.add(saving);
                        totalGoals++;
                        
                        String status = saving.getStatus() != null ? saving.getStatus() : "Active";
                        if ("Completed".equalsIgnoreCase(status) || saving.getCurrentAmount() >= saving.getTargetAmount()) {
                            completedGoals++;
                        } else {
                            activeGoals++;
                        }

                        totalTarget += saving.getTargetAmount();
                        totalSaved += saving.getCurrentAmount();

                        double progress = 0;
                        if (saving.getTargetAmount() > 0) {
                            progress = (saving.getCurrentAmount() / saving.getTargetAmount()) * 100;
                        }
                        if (progress > 100) progress = 100;
                        totalProgressSum += progress;
                    }
                }

                if (totalGoals == 0) {
                    findViewById(R.id.layoutContent).setVisibility(android.view.View.GONE);
                    findViewById(R.id.layoutEmptyState).setVisibility(android.view.View.VISIBLE);
                    Toast.makeText(SavingReportResultActivity.this, "No data found for this period", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    findViewById(R.id.layoutContent).setVisibility(android.view.View.VISIBLE);
                    findViewById(R.id.layoutEmptyState).setVisibility(android.view.View.GONE);
                }

                double avgProgress = totalGoals > 0 ? (totalProgressSum / totalGoals) : 0;
                double completedGoalsPercent = totalGoals > 0 ? ((double) completedGoals / totalGoals) * 100 : 0;

                double healthScore = (avgProgress * 0.7) + (completedGoalsPercent * 0.3);

                updateUI(totalGoals, activeGoals, completedGoals, totalTarget, totalSaved, avgProgress, healthScore);
            } else {
                Toast.makeText(SavingReportResultActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithinDateRange(String start, String end) {
        try {
            Date sStart = dateFormat.parse(start);
            Date sEnd = dateFormat.parse(end);
            
            if (sStart == null || sEnd == null) return false;

            if ("MONTH".equals(reportType)) {
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                Date reportMonth = monthFormat.parse(monthYear);
                if (reportMonth == null) return true;
                
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(reportMonth);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                Date rStart = cal.getTime();
                
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                Date rEnd = cal.getTime();
                
                return !sStart.after(rEnd) && !sEnd.before(rStart);

            } else if ("CUSTOM".equals(reportType)) {
                Date rStart = dateFormat.parse(startDateStr);
                Date rEnd = dateFormat.parse(endDateStr);
                
                if (rStart != null && rEnd != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(rEnd);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    cal.set(java.util.Calendar.MINUTE, 59);
                    cal.set(java.util.Calendar.SECOND, 59);
                    rEnd = cal.getTime();
                    return !sStart.after(rEnd) && !sEnd.before(rStart);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void updateUI(int totalGoals, int activeGoals, int completedGoals, double totalTarget, double totalSaved, double avgProgress, double healthScore) {
        tvTotalGoals.setText(String.valueOf(totalGoals));
        tvActiveGoals.setText(String.valueOf(activeGoals));
        tvCompletedGoals.setText(String.valueOf(completedGoals));
        
        tvTotalTarget.setText(CurrencyHelper.formatMoney(this, totalTarget));
        tvTotalSaved.setText(CurrencyHelper.formatMoney(this, totalSaved));
        
        double remaining = totalTarget - totalSaved;
        if (remaining < 0) remaining = 0;
        tvTotalRemaining.setText(CurrencyHelper.formatMoney(this, remaining));
        
        tvAvgProgress.setText(String.format(Locale.getDefault(), "%.0f%%", avgProgress));
        pbAvgProgress.setProgress((int) avgProgress);
        
        tvHealthScoreValue.setText(String.format(Locale.getDefault(), "Score: %.0f/100", healthScore));
        
        if (healthScore >= 80) {
            tvHealthScore.setText("Excellent");
            tvHealthScore.setTextColor(getResources().getColor(R.color.pill_positive_text, null));
        } else if (healthScore >= 60) {
            tvHealthScore.setText("Good");
            tvHealthScore.setTextColor(getResources().getColor(R.color.qa_secondary_icon, null));
        } else {
            tvHealthScore.setText("Needs Improvement");
            tvHealthScore.setTextColor(getResources().getColor(R.color.danger_text, null));
        }
    }

    private void saveCsvToUri(android.net.Uri uri) {
        StringBuilder csvData = new StringBuilder();
        
        // Add summary to CSV
        csvData.append("Saving Report Summary\n");
        csvData.append("Period,").append(tvReportPeriod.getText().toString().replace("Period: ", "")).append("\n");
        csvData.append("Health Score,").append(tvHealthScore.getText().toString()).append("\n");
        csvData.append("Total Goals,").append(tvTotalGoals.getText().toString()).append("\n");
        csvData.append("Active Goals,").append(tvActiveGoals.getText().toString()).append("\n");
        csvData.append("Completed Goals,").append(tvCompletedGoals.getText().toString()).append("\n");
        
        // Remove currency symbols and commas for raw numbers in CSV, or just put the raw text
        csvData.append("Total Target,").append("\"").append(tvTotalTarget.getText().toString()).append("\"\n");
        csvData.append("Total Saved,").append("\"").append(tvTotalSaved.getText().toString()).append("\"\n");
        csvData.append("Average Progress,").append(tvAvgProgress.getText().toString()).append("\n\n");

        csvData.append("Saving Name,Target Amount,Current Amount,Start Date,Target Date,Status\n");

        for (SavingModel saving : filteredSavings) {
            csvData.append(String.format(Locale.US, "\"%s\",%.2f,%.2f,%s,%s,%s\n",
                    saving.getSavingTitle() != null ? saving.getSavingTitle().replace("\"", "\"\"") : "",
                    saving.getTargetAmount(),
                    saving.getCurrentAmount(),
                    saving.getStartDate(),
                    saving.getTargetDate(),
                    saving.getStatus() != null ? saving.getStatus() : "Active"));
        }

        try {
            java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(csvData.toString().getBytes());
                outputStream.close();
                Toast.makeText(this, "CSV saved successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfToUri(android.net.Uri uri) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Title
        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        canvas.drawText("Smart Finance Manager - Saving Report", 50, 50, paint);

        // Date
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated on: " + dateStr, 50, 80, paint);
        
        // Report Period
        canvas.drawText(tvReportPeriod.getText().toString(), 50, 100, paint);

        // Table Headers
        paint.setFakeBoldText(true);
        int y = 130;
        canvas.drawText("Summary", 50, y, paint);
        
        paint.setFakeBoldText(false);
        y += 20;
        canvas.drawText("Health Score: " + tvHealthScore.getText().toString() + " (" + tvHealthScoreValue.getText().toString() + ")", 50, y, paint);
        y += 20;
        canvas.drawText("Total Goals: " + tvTotalGoals.getText().toString() + " | Active: " + tvActiveGoals.getText().toString() + " | Done: " + tvCompletedGoals.getText().toString(), 50, y, paint);
        y += 20;
        canvas.drawText("Total Target: " + tvTotalTarget.getText().toString(), 50, y, paint);
        y += 20;
        canvas.drawText("Total Saved: " + tvTotalSaved.getText().toString(), 50, y, paint);
        y += 20;
        canvas.drawText("Average Progress: " + tvAvgProgress.getText().toString(), 50, y, paint);
        
        y += 40;
        paint.setFakeBoldText(true);
        canvas.drawText("Saving Name", 50, y, paint);
        canvas.drawText("Target", 250, y, paint);
        canvas.drawText("Current", 350, y, paint);
        canvas.drawText("Status", 450, y, paint);

        // Divider
        canvas.drawLine(50, y + 10, 550, y + 10, paint);

        // Table Content
        paint.setFakeBoldText(false);
        y += 40;
        for (SavingModel saving : filteredSavings) {
            if (y > 800) { 
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
            String title = saving.getSavingTitle() != null ? saving.getSavingTitle() : "Unnamed";
            if (title.length() > 20) title = title.substring(0, 17) + "...";
            canvas.drawText(title, 50, y, paint);
            canvas.drawText(CurrencyHelper.formatMoney(this, saving.getTargetAmount()), 250, y, paint);
            canvas.drawText(CurrencyHelper.formatMoney(this, saving.getCurrentAmount()), 350, y, paint);
            canvas.drawText(saving.getStatus() != null ? saving.getStatus() : "Active", 450, y, paint);
            y += 30;
        }

        document.finishPage(page);

        try {
            java.io.OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                document.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}
