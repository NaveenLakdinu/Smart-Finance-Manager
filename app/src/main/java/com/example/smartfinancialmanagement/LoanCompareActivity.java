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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoanCompareActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout loanCardsContainer;
    private MaterialCardView btnAddOption;
    private int optionCount = 0;

    private com.google.android.material.button.MaterialButton btnCompare;

    // Helper class for comparison data
    private static class ComparisonData {
        String optionLabel;
        String bankName;
        double principal;
        double interestRate;
        int duration;
        double emi;
        double totalPayable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_compare);

        initViews();
        setupListeners();

        // Start with 3 default options as requested
        for (int i = 0; i < 3; i++) {
            addLoanOptionCard();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        loanCardsContainer = findViewById(R.id.loanCardsContainer);
        btnAddOption = findViewById(R.id.btnAddOption);
        btnCompare = findViewById(R.id.btnCompare);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddOption.setOnClickListener(v -> addLoanOptionCard());
        
        btnCompare.setOnClickListener(v -> {
            if (validateAllCards()) {
                List<ComparisonData> dataList = extractComparisonData();
                if (!dataList.isEmpty()) {
                    generateDetailedComparisonReport(dataList);
                }
            }
        });
    }

    private List<ComparisonData> extractComparisonData() {
        List<ComparisonData> dataList = new ArrayList<>();
        int childCount = loanCardsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View cardView = loanCardsContainer.getChildAt(i);
            ComparisonData data = new ComparisonData();
            
            TextView title = cardView.findViewById(R.id.txtLoanOptionTitle);
            EditText etBank = cardView.findViewById(R.id.etBankName);
            EditText etP = cardView.findViewById(R.id.etPrincipal);
            EditText etI = cardView.findViewById(R.id.etInterest);
            EditText etD = cardView.findViewById(R.id.etDuration);
            TextView txtRes = cardView.findViewById(R.id.txtComparisonResult);

            data.optionLabel = title.getText().toString();
            data.bankName = etBank.getText().toString().trim();
            data.principal = Double.parseDouble(etP.getText().toString().trim());
            data.interestRate = Double.parseDouble(etI.getText().toString().trim());
            data.duration = Integer.parseInt(etD.getText().toString().trim());
            
            // Extract EMI and Total from the result text (e.g., "EMI: $100.00 | Total: $1200.00")
            String resText = txtRes.getText().toString();
            try {
                String emiPart = resText.substring(resText.indexOf("$") + 1, resText.indexOf("|")).trim();
                String totalPart = resText.substring(resText.lastIndexOf("$") + 1).trim();
                data.emi = Double.parseDouble(emiPart);
                data.totalPayable = Double.parseDouble(totalPart);
            } catch (Exception e) {
                // Fallback to calculation if parsing fails
                double r = data.interestRate / (12 * 100);
                if (r == 0) data.emi = data.principal / data.duration;
                else data.emi = (data.principal * r * Math.pow(1 + r, data.duration)) / (Math.pow(1 + r, data.duration) - 1);
                data.totalPayable = data.emi * data.duration;
            }
            
            dataList.add(data);
        }
        return dataList;
    }

    private void generateDetailedComparisonReport(List<ComparisonData> dataList) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. Header
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText("Detailed Loan Comparison Analysis", 50, 60, paint);
        
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated on: " + dateStr, 50, 90, paint);
        canvas.drawLine(50, 100, 550, 100, paint);

        // 2. Analysis & Recommendations
        ComparisonData bestOverall = Collections.min(dataList, Comparator.comparingDouble(d -> d.totalPayable));
        ComparisonData lowestEMI = Collections.min(dataList, Comparator.comparingDouble(d -> d.emi));

        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#10B981")); // Green for recommendation
        canvas.drawText("RECOMMENDED OPTION (Best Overall Value)", 50, 140, paint);
        
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(false);
        paint.setTextSize(14f);
        canvas.drawText("Option: " + bestOverall.optionLabel + " (" + bestOverall.bankName + ")", 50, 165, paint);
        canvas.drawText("Reason: Lowest total repayment amount of $" + String.format(Locale.US, "%.2f", bestOverall.totalPayable), 50, 185, paint);

        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#3B82F6")); // Blue for EMI burden
        canvas.drawText("LOWEST MONTHLY BURDEN", 50, 230, paint);
        
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(false);
        paint.setTextSize(14f);
        canvas.drawText("Option: " + lowestEMI.optionLabel + " (" + lowestEMI.bankName + ")", 50, 255, paint);
        canvas.drawText("Best for: Maintaining immediate monthly cash flow ($" + String.format(Locale.US, "%.2f", lowestEMI.emi) + "/mo)", 50, 275, paint);

        // 3. Detailed Comparison Table
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText("Detailed Side-by-Side Comparison", 50, 330, paint);
        canvas.drawLine(50, 340, 550, 340, paint);

        int y = 370;
        paint.setTextSize(12f);
        // Header row
        canvas.drawText("Label/Bank", 50, y, paint);
        canvas.drawText("Principal", 180, y, paint);
        canvas.drawText("Rate", 280, y, paint);
        canvas.drawText("EMI", 350, y, paint);
        canvas.drawText("Total Payable", 450, y, paint);
        
        canvas.drawLine(50, y + 10, 550, y + 10, paint);
        y += 40;
        paint.setFakeBoldText(false);

        for (ComparisonData data : dataList) {
            if (y > 780) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
            canvas.drawText(data.optionLabel + " / " + data.bankName, 50, y, paint);
            canvas.drawText("$" + (int)data.principal, 180, y, paint);
            canvas.drawText(data.interestRate + "%", 280, y, paint);
            canvas.drawText("$" + String.format(Locale.US, "%.2f", data.emi), 350, y, paint);
            canvas.drawText("$" + String.format(Locale.US, "%.2f", data.totalPayable), 450, y, paint);
            y += 35;
        }

        document.finishPage(page);
        String fileName = "Loan_Comparison_Analysis_" + System.currentTimeMillis() + ".pdf";

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
                        Toast.makeText(this, "Analytical Report saved to Downloads", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Download feature requires Android 10+", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private boolean validateAllCards() {
        boolean allValid = true;
        int childCount = loanCardsContainer.getChildCount();

        if (childCount == 0) {
            android.widget.Toast.makeText(this, "Please add at least one loan option", android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < childCount; i++) {
            View cardView = loanCardsContainer.getChildAt(i);
            if (!validateSingleCard(cardView)) {
                allValid = false;
            }
        }

        if (!allValid) {
            android.widget.Toast.makeText(this, "Please fix the errors in your loan options", android.widget.Toast.LENGTH_SHORT).show();
        }

        return allValid;
    }

    private boolean validateSingleCard(View cardView) {
        EditText etBank = cardView.findViewById(R.id.etBankName);
        EditText etP = cardView.findViewById(R.id.etPrincipal);
        EditText etI = cardView.findViewById(R.id.etInterest);
        EditText etD = cardView.findViewById(R.id.etDuration);

        boolean valid = true;

        if (etBank.getText().toString().trim().isEmpty()) {
            etBank.setError("Bank name is required");
            valid = false;
        }

        String pStr = etP.getText().toString().trim();
        if (pStr.isEmpty() || Double.parseDouble(pStr) <= 0) {
            etP.setError("Enter valid principal");
            valid = false;
        }

        String iStr = etI.getText().toString().trim();
        if (iStr.isEmpty() || Double.parseDouble(iStr) < 0) {
            etI.setError("Enter valid interest");
            valid = false;
        }

        String dStr = etD.getText().toString().trim();
        if (dStr.isEmpty() || Integer.parseInt(dStr) <= 0) {
            etD.setError("Enter valid months");
            valid = false;
        }

        return valid;
    }

    private void addLoanOptionCard() {
        optionCount++;
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_loan_compare_card, loanCardsContainer, false);

        TextView title = cardView.findViewById(R.id.txtLoanOptionTitle);
        title.setText("Option " + (char) ('A' + (optionCount - 1)));

        ImageView btnRemove = cardView.findViewById(R.id.btnRemoveLoan);
        
        // Only show remove button for options after the first 3
        if (optionCount > 3) {
            btnRemove.setVisibility(View.VISIBLE);
            btnRemove.setOnClickListener(v -> {
                loanCardsContainer.removeView(cardView);
                // Note: Title letters won't auto-update without re-iterating, 
                // but this keeps implementation simple.
            });
        }

        setupCardLogic(cardView);
        loanCardsContainer.addView(cardView);
    }

    private void setupCardLogic(View cardView) {
        EditText etPrincipal = cardView.findViewById(R.id.etPrincipal);
        EditText etInterest = cardView.findViewById(R.id.etInterest);
        EditText etDuration = cardView.findViewById(R.id.etDuration);
        TextView txtResult = cardView.findViewById(R.id.txtComparisonResult);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateCardEMI(etPrincipal, etInterest, etDuration, txtResult);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPrincipal.addTextChangedListener(watcher);
        etInterest.addTextChangedListener(watcher);
        etDuration.addTextChangedListener(watcher);
    }

    private void calculateCardEMI(EditText etP, EditText etI, EditText etD, TextView txtRes) {
        try {
            String pStr = etP.getText().toString();
            String iStr = etI.getText().toString();
            String dStr = etD.getText().toString();

            if (!pStr.isEmpty() && !iStr.isEmpty() && !dStr.isEmpty()) {
                double p = Double.parseDouble(pStr);
                double annualRate = Double.parseDouble(iStr);
                int n = Integer.parseInt(dStr);

                double r = annualRate / (12 * 100);
                double emi;
                if (r == 0) {
                    emi = p / n;
                } else {
                    emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                }

                double total = emi * n;
                txtRes.setText(String.format(Locale.US, "EMI: $%.2f | Total: $%.2f", emi, total));
            } else {
                txtRes.setText("EMI: $0.00 | Total: $0.00");
            }
        } catch (Exception e) {
            txtRes.setText("EMI: $0.00 | Total: $0.00");
        }
    }
}
