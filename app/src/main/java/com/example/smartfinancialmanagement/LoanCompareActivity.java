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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.appcompat.app.AlertDialog;

public class LoanCompareActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout loanCardsContainer;
    private MaterialCardView btnAddOption;
    private int optionCount = 0;

    private com.google.android.material.button.MaterialButton btnCompare;

    private double userMonthlyIncome = 0.0;
    private double activeLoansMonthlyTotal = 0.0;
    private double utilitiesMonthlyTotal = 0.0;
    private double subscriptionsMonthlyTotal = 0.0;
    private FirebaseFirestore db;
    private String uid;

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
        fetchFinancialData();
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
                    showVisualSuitabilityReport(dataList);
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
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText("Detailed Loan Comparison & Suitability Analysis", 40, 50, paint);
        
        paint.setTextSize(11f);
        paint.setFakeBoldText(false);
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated on: " + dateStr, 40, 75, paint);
        canvas.drawLine(40, 85, 555, 85, paint);

        // 2. Financial Context Summary
        paint.setTextSize(13f);
        paint.setFakeBoldText(true);
        canvas.drawText("Your Financial Context (Monthly Basis)", 40, 110, paint);

        paint.setTextSize(11f);
        paint.setFakeBoldText(false);
        canvas.drawText(String.format(Locale.US, "• Monthly Income / Savings: LKR %.2f", userMonthlyIncome), 45, 130, paint);
        
        double existingCommitments = activeLoansMonthlyTotal + utilitiesMonthlyTotal + subscriptionsMonthlyTotal;
        canvas.drawText(String.format(Locale.US, "• Existing Commitments: LKR %.2f", existingCommitments), 45, 150, paint);
        canvas.drawText(String.format(Locale.US, "  - Active Loans EMI: LKR %.2f", activeLoansMonthlyTotal), 55, 170, paint);
        canvas.drawText(String.format(Locale.US, "  - Utility Bills: LKR %.2f", utilitiesMonthlyTotal), 55, 190, paint);
        canvas.drawText(String.format(Locale.US, "  - Subscriptions: LKR %.2f", subscriptionsMonthlyTotal), 55, 210, paint);
        
        canvas.drawLine(40, 225, 555, 225, paint);

        // 3. Side-by-Side Suitability Analysis
        paint.setTextSize(13f);
        paint.setFakeBoldText(true);
        canvas.drawText("Option-by-Option Suitability Assessment", 40, 250, paint);

        int y = 275;
        paint.setTextSize(11f);
        paint.setFakeBoldText(false);

        for (ComparisonData data : dataList) {
            double totalExpenses = existingCommitments + data.emi;
            double remaining = userMonthlyIncome - totalExpenses;
            boolean isSuitable = remaining >= 0;

            paint.setFakeBoldText(true);
            canvas.drawText(data.optionLabel + " (" + data.bankName + ")", 45, y, paint);
            paint.setFakeBoldText(false);

            canvas.drawText(String.format(Locale.US, "EMI: LKR %.2f | Commitment: LKR %.2f | Net Cash Flow: LKR %.2f", 
                    data.emi, totalExpenses, remaining), 45, y + 18, paint);

            paint.setFakeBoldText(true);
            paint.setColor(Color.parseColor(isSuitable ? "#10B981" : "#EF4444"));
            canvas.drawText("STATUS: " + (isSuitable ? "SUITABLE (AFFORDABLE)" : "NOT SUITABLE (BURDEN)"), 45, y + 36, paint);
            paint.setColor(Color.BLACK);
            paint.setFakeBoldText(false);

            y += 55;
            if (y > 780) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        canvas.drawLine(40, y + 5, 555, y + 5, paint);
        y += 25;

        // 4. Detailed Side-by-Side Comparison Table
        paint.setTextSize(13f);
        paint.setFakeBoldText(true);
        canvas.drawText("Side-by-Side Comparison Table", 40, y, paint);
        canvas.drawLine(40, y + 10, 555, y + 10, paint);
        y += 28;

        paint.setTextSize(10f);
        canvas.drawText("Label / Bank", 40, y, paint);
        canvas.drawText("Principal", 180, y, paint);
        canvas.drawText("Rate", 280, y, paint);
        canvas.drawText("Monthly EMI", 345, y, paint);
        canvas.drawText("Total Payable", 445, y, paint);

        canvas.drawLine(40, y + 5, 555, y + 5, paint);
        y += 20;
        paint.setFakeBoldText(false);

        for (ComparisonData data : dataList) {
            if (y > 780) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
            canvas.drawText(data.optionLabel + " - " + data.bankName, 40, y, paint);
            canvas.drawText(String.format(Locale.US, "LKR %.0f", data.principal), 180, y, paint);
            canvas.drawText(String.format(Locale.US, "%.2f%%", data.interestRate), 280, y, paint);
            canvas.drawText(String.format(Locale.US, "LKR %.2f", data.emi), 345, y, paint);
            canvas.drawText(String.format(Locale.US, "LKR %.2f", data.totalPayable), 445, y, paint);
            y += 20;
        }

        document.finishPage(page);
        String fileName = "Loan_Suitability_Report_" + System.currentTimeMillis() + ".pdf";

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
                        Toast.makeText(this, "Analytical Suitability Report saved to Downloads", Toast.LENGTH_LONG).show();
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

    private void fetchFinancialData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            userMonthlyIncome = 85000.0;
            return;
        }
        uid = user.getUid();
        db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String monthlySavingStr = documentSnapshot.getString("monthlySavingAmount");
                if (monthlySavingStr != null && !monthlySavingStr.trim().isEmpty()) {
                    try {
                        userMonthlyIncome = Double.parseDouble(monthlySavingStr.trim());
                    } catch (NumberFormatException ignored) {}
                }
                
                String role = documentSnapshot.getString("role");
                if (userMonthlyIncome <= 0 && "Company worker".equals(role)) {
                    db.collection("users").document(uid).collection("worker_profile").document("profile_data")
                            .get()
                            .addOnSuccessListener(profileDoc -> {
                                if (profileDoc.exists()) {
                                    Double salary = profileDoc.getDouble("monthlySalary");
                                    if (salary != null && salary > 0) {
                                        userMonthlyIncome = salary;
                                    }
                                }
                                if (userMonthlyIncome <= 0) {
                                    userMonthlyIncome = 75000.0;
                                }
                            });
                } else if (userMonthlyIncome <= 0) {
                    userMonthlyIncome = 60000.0;
                }
            }
        });

        db.collection("users").document(uid).collection("loans").get().addOnSuccessListener(queryDocumentSnapshots -> {
            double total = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Double emi = doc.getDouble("monthlyEmi");
                if (emi != null) {
                    total += emi;
                }
            }
            activeLoansMonthlyTotal = total;
        });

        db.collection("users").document(uid).collection("utilities").get().addOnSuccessListener(queryDocumentSnapshots -> {
            double total = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Double amount = doc.getDouble("amount");
                if (amount != null) {
                    total += amount;
                }
            }
            utilitiesMonthlyTotal = total;
        });

        db.collection("users").document(uid).collection("subscriptions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            subscriptionsMonthlyTotal = queryDocumentSnapshots.size() * 1500.0;
        });
    }

    private void showVisualSuitabilityReport(List<ComparisonData> dataList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        TextView titleView = new TextView(this);
        titleView.setText("Loan Suitability & Commitment Analysis");
        titleView.setTextSize(18f);
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleView.setTextColor(Color.parseColor("#FFFFFF"));
        titleView.setPadding(40, 40, 40, 20);
        titleView.setBackgroundColor(Color.parseColor("#071A33"));
        builder.setCustomTitle(titleView);

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(30, 30, 30, 30);
        container.setBackgroundColor(Color.parseColor("#071A33"));

        TextView introText = new TextView(this);
        introText.setText(String.format(Locale.US, "Your Monthly Resource Base: LKR %.2f\nExisting commitments: Loans (LKR %.2f) + Utilities (LKR %.2f) + Subscriptions (LKR %.2f)", 
                userMonthlyIncome, activeLoansMonthlyTotal, utilitiesMonthlyTotal, subscriptionsMonthlyTotal));
        introText.setTextColor(Color.parseColor("#BCE0FF"));
        introText.setTextSize(13f);
        introText.setPadding(0, 0, 0, 30);
        container.addView(introText);

        double existingExpenses = activeLoansMonthlyTotal + utilitiesMonthlyTotal + subscriptionsMonthlyTotal;

        for (ComparisonData data : dataList) {
            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
            card.setCardElevation(6f);
            card.setRadius(24f);
            card.setUseCompatPadding(true);
            
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(32, 32, 32, 32);

            double totalExpenses = existingExpenses + data.emi;
            double remaining = userMonthlyIncome - totalExpenses;
            boolean isSuitable = remaining >= 0;

            card.setCardBackgroundColor(Color.parseColor(isSuitable ? "#0C2E2B" : "#321A1A"));
            card.setStrokeWidth(2);
            card.setStrokeColor(Color.parseColor(isSuitable ? "#10B981" : "#EF4444"));

            TextView nameText = new TextView(this);
            nameText.setText(data.optionLabel + " - " + data.bankName);
            nameText.setTextColor(Color.WHITE);
            nameText.setTextSize(16f);
            nameText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            cardContent.addView(nameText);

            TextView detailsText = new TextView(this);
            detailsText.setText(String.format(Locale.US, 
                    "• Proposed EMI: LKR %.2f/mo\n" +
                    "• Total Monthly Commitment: LKR %.2f/mo\n" +
                    "• Net Cash Flow: LKR %.2f/mo", 
                    data.emi, totalExpenses, remaining));
            detailsText.setTextColor(Color.parseColor("#D8E4FF"));
            detailsText.setTextSize(13f);
            detailsText.setPadding(0, 16, 0, 16);
            cardContent.addView(detailsText);

            TextView badge = new TextView(this);
            badge.setText(isSuitable ? "✅ SUITABLE (AFFORDABLE)" : "❌ NOT RECOMMENDED (BURDEN)");
            badge.setTextColor(Color.parseColor(isSuitable ? "#34D399" : "#F87171"));
            badge.setTextSize(14f);
            badge.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            cardContent.addView(badge);

            card.addView(cardContent);
            container.addView(card);
        }

        scrollView.addView(container);
        builder.setView(scrollView);

        builder.setPositiveButton("Generate PDF Report", (dialog, which) -> {
            generateDetailedComparisonReport(dataList);
        });
        builder.setNegativeButton("Close", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4ADE80"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#071A33")));
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
