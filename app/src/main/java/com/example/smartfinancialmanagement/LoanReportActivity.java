package com.example.smartfinancialmanagement;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoanReportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RadioButton radioPdf, radioCsv;
    private com.google.android.material.card.MaterialCardView optionPdf, optionCsv;
    private CheckBox checkActiveLoans;
    private MaterialButton btnGenerateReport;
    
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_report);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        radioPdf = findViewById(R.id.radioPdf);
        radioCsv = findViewById(R.id.radioCsv);
        optionPdf = findViewById(R.id.optionPdf);
        optionCsv = findViewById(R.id.optionCsv);
        checkActiveLoans = findViewById(R.id.checkActiveLoans);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        setupFormatSelection();

        btnBack.setOnClickListener(v -> finish());
        
        btnGenerateReport.setOnClickListener(v -> {
            if (uid == null) {
                Toast.makeText(this, "Please login to generate report", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (radioPdf.isChecked()) {
                fetchDataAndGenerateReport(true);
            } else if (radioCsv.isChecked()) {
                fetchDataAndGenerateReport(false);
            }
        });
    }

    private void setupFormatSelection() {
        // Since RadioButtons are nested in cards, RadioGroup won't work automatically.
        // We handle selection manually on card click.
        
        optionPdf.setOnClickListener(v -> {
            radioPdf.setChecked(true);
            radioCsv.setChecked(false);
            updateFormatUI();
        });

        optionCsv.setOnClickListener(v -> {
            radioCsv.setChecked(true);
            radioPdf.setChecked(false);
            updateFormatUI();
        });

        radioPdf.setOnClickListener(v -> {
            radioPdf.setChecked(true);
            radioCsv.setChecked(false);
            updateFormatUI();
        });

        radioCsv.setOnClickListener(v -> {
            radioCsv.setChecked(true);
            radioPdf.setChecked(false);
            updateFormatUI();
        });

        // Initial UI update to apply active/inactive styles correctly
        updateFormatUI();
    }

    private void updateFormatUI() {
        // Highlight the selected card with an accent stroke matching our premium dark theme
        int activeColor = ContextCompat.getColor(this, R.color.hero_accent);
        int inactiveColor = ContextCompat.getColor(this, R.color.glass_card_border);
        int activeBg = Color.parseColor("#223E66"); // Slightly lighter highlighted dark blue
        int inactiveBg = ContextCompat.getColor(this, R.color.glass_card_bg);

        if (radioPdf.isChecked()) {
            optionPdf.setStrokeColor(activeColor);
            optionPdf.setStrokeWidth(4);
            optionPdf.setCardBackgroundColor(activeBg);
            
            optionCsv.setStrokeColor(inactiveColor);
            optionCsv.setStrokeWidth(2);
            optionCsv.setCardBackgroundColor(inactiveBg);
        } else {
            optionCsv.setStrokeColor(activeColor);
            optionCsv.setStrokeWidth(4);
            optionCsv.setCardBackgroundColor(activeBg);
            
            optionPdf.setStrokeColor(inactiveColor);
            optionPdf.setStrokeWidth(2);
            optionPdf.setCardBackgroundColor(inactiveBg);
        }
    }

    private void fetchDataAndGenerateReport(boolean isPdf) {
        btnGenerateReport.setEnabled(false);
        btnGenerateReport.setText("Fetching Data...");

        db.collection("users").document(uid).collection("loans")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Loan> loans = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Loan loan = doc.toObject(Loan.class);
                        loans.add(loan);
                    }

                    if (loans.isEmpty()) {
                        Toast.makeText(this, "No loan data found to generate report", Toast.LENGTH_SHORT).show();
                        btnGenerateReport.setEnabled(true);
                        btnGenerateReport.setText("Generate & Download");
                        return;
                    }

                    if (isPdf) {
                        generatePdfReport(loans);
                    } else {
                        generateCsvReport(loans);
                    }
                    
                    btnGenerateReport.setEnabled(true);
                    btnGenerateReport.setText("Generate & Download");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnGenerateReport.setEnabled(true);
                    btnGenerateReport.setText("Generate & Download");
                });
    }

    private void generateCsvReport(List<Loan> loans) {
        StringBuilder csvData = new StringBuilder();
        csvData.append("Loan Name,Principal Amount,Interest Rate (%),Duration (Months),Monthly EMI,Created At\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (Loan loan : loans) {
            csvData.append(String.format(Locale.US, "%s,%.2f,%.2f,%d,%.2f,%s\n",
                    loan.getLoanName(),
                    loan.getPrincipalAmount(),
                    loan.getInterestRate(),
                    loan.getDurationMonths(),
                    loan.getMonthlyEmi(),
                    sdf.format(new Date(loan.getCreatedAt()))));
        }

        String fileName = "Loan_Report_" + System.currentTimeMillis() + ".csv";
        saveFileToDownloads(fileName, "text/csv", csvData.toString().getBytes());
    }

    private void generatePdfReport(List<Loan> loans) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Title
        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        canvas.drawText("Smart Finance Manager - Loan Report", 50, 50, paint);

        // Date
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated on: " + dateStr, 50, 80, paint);

        // Table Headers
        paint.setFakeBoldText(true);
        int y = 120;
        canvas.drawText("Loan Name", 50, y, paint);
        canvas.drawText("Principal", 200, y, paint);
        canvas.drawText("EMI", 350, y, paint);
        canvas.drawText("Duration", 480, y, paint);

        // Divider
        canvas.drawLine(50, y + 10, 550, y + 10, paint);

        // Table Content
        paint.setFakeBoldText(false);
        y += 40;
        for (Loan loan : loans) {
            if (y > 800) { // Very basic pagination check
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
            canvas.drawText(loan.getLoanName(), 50, y, paint);
            canvas.drawText(String.format(Locale.US, "$%.2f", loan.getPrincipalAmount()), 200, y, paint);
            canvas.drawText(String.format(Locale.US, "$%.2f", loan.getMonthlyEmi()), 350, y, paint);
            canvas.drawText(loan.getDurationMonths() + "m", 480, y, paint);
            y += 30;
        }

        document.finishPage(page);

        String fileName = "Loan_Report_" + System.currentTimeMillis() + ".pdf";
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        document.writeTo(outputStream);
                        outputStream.close();
                        Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // Legacy saving not implemented for brevity, using MediaStore for modern compatibility
                Toast.makeText(this, "Legacy saving not supported in this version", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void saveFileToDownloads(String fileName, String mimeType, byte[] content) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(content);
                        outputStream.close();
                        Toast.makeText(this, "File saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Legacy saving not supported", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
