package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkerPayslipActivity extends AppCompatActivity {

    private TextView tvMonthYear, tvNetSalary, tvDaysWorked, tvOvertimeHours;
    private TextView tvBasicSalary, tvTransportAllowance, tvPerformanceBonus;
    private TextView tvEpfDeduction, tvIncomeTax;
    private TextView tvYtdGross, tvYtdDeductions, tvYtdNet;
    private TextView tvEarningsPercent, tvDeductionsPercent, tvStatusBadge;
    private View viewEarningsBar, viewDeductionsBar, emptyState;
    private Spinner spinnerMonth;

    private FirebaseFirestore db;
    private String uid;
    private double monthlySalary = 0;
    private List<Map<String, Object>> payslipList = new ArrayList<>();
    private List<String> monthLabels = new ArrayList<>();
    private int selectedPayslipIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_payslip);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) uid = user.getUid();

        initViews();
        setupBackNavigation();
        setupDownloadButton();
        setupGenerateButton();
        loadWorkerSalary();
        loadPayslipsFromFirestore();
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvNetSalary = findViewById(R.id.tvNetSalary);
        tvDaysWorked = findViewById(R.id.tvDaysWorked);
        tvOvertimeHours = findViewById(R.id.tvOvertimeHours);
        tvBasicSalary = findViewById(R.id.tvBasicSalary);
        tvTransportAllowance = findViewById(R.id.tvTransportAllowance);
        tvPerformanceBonus = findViewById(R.id.tvPerformanceBonus);
        tvEpfDeduction = findViewById(R.id.tvEpfDeduction);
        tvIncomeTax = findViewById(R.id.tvIncomeTax);
        tvYtdGross = findViewById(R.id.tvYtdGross);
        tvYtdDeductions = findViewById(R.id.tvYtdDeductions);
        tvYtdNet = findViewById(R.id.tvYtdNet);
        tvEarningsPercent = findViewById(R.id.tvEarningsPercent);
        tvDeductionsPercent = findViewById(R.id.tvDeductionsPercent);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        viewEarningsBar = findViewById(R.id.viewEarningsBar);
        viewDeductionsBar = findViewById(R.id.viewDeductionsBar);
        emptyState = findViewById(R.id.emptyState);
        spinnerMonth = findViewById(R.id.spinnerMonth);
    }

    private void setupBackNavigation() {
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { finish(); }
        });
    }

    private void setupDownloadButton() {
        AppCompatButton btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(v -> downloadPayslipPdf());
    }

    private void setupGenerateButton() {
        AppCompatButton btnGenerate = findViewById(R.id.btnGeneratePayslip);
        btnGenerate.setOnClickListener(v -> showGeneratePayslipDialog());
    }

    private void loadWorkerSalary() {
        if (uid == null) return;
        db.collection("users").document(uid)
                .collection("worker_profile").document("profile_data")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double salary = doc.getDouble("monthlySalary");
                        if (salary != null) monthlySalary = salary;
                    }
                });
    }

    private void showGeneratePayslipDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_payslip, null);

        Spinner spinnerMonthSel = dialogView.findViewById(R.id.spinnerMonth);
        EditText etDaysWorked = dialogView.findViewById(R.id.etDaysWorked);
        EditText etOvertimeHours = dialogView.findViewById(R.id.etOvertimeHours);
        EditText etTransportAllowance = dialogView.findViewById(R.id.etTransportAllowance);
        EditText etPerformanceBonus = dialogView.findViewById(R.id.etPerformanceBonus);

        // Populate month spinner
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        List<String> monthOptions = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 1; y--) {
            int endMonth = (y == currentYear) ? currentMonth : 11;
            int startMonth = (y == currentYear) ? currentMonth : 0;
            for (int m = startMonth; m <= endMonth; m++) {
                monthOptions.add(months[m] + " " + y);
            }
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, monthOptions);
        spinnerMonthSel.setAdapter(monthAdapter);

        // Pre-fill transport allowance (10% of salary)
        if (monthlySalary > 0) {
            etTransportAllowance.setText(String.valueOf(Math.round(monthlySalary * 0.10)));
        }

        new AlertDialog.Builder(this)
                .setTitle("Generate Payslip")
                .setView(dialogView)
                .setPositiveButton("Generate", (dialog, which) -> {
                    String selectedMonth = spinnerMonthSel.getSelectedItem().toString();
                    String daysStr = etDaysWorked.getText().toString().trim();
                    String overtimeStr = etOvertimeHours.getText().toString().trim();
                    String transportStr = etTransportAllowance.getText().toString().trim();
                    String bonusStr = etPerformanceBonus.getText().toString().trim();

                    if (daysStr.isEmpty()) {
                        Toast.makeText(this, "Days worked is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int daysWorked;
                    try { daysWorked = Integer.parseInt(daysStr); } catch (NumberFormatException e) { daysWorked = 22; }

                    double overtimeHours = 0;
                    if (!overtimeStr.isEmpty()) {
                        try { overtimeHours = Double.parseDouble(overtimeStr); } catch (NumberFormatException ignored) {}
                    }

                    double transportAllowance = monthlySalary * 0.10;
                    if (!transportStr.isEmpty()) {
                        try { transportAllowance = Double.parseDouble(transportStr); } catch (NumberFormatException ignored) {}
                    }

                    double performanceBonus = 0;
                    if (!bonusStr.isEmpty()) {
                        try { performanceBonus = Double.parseDouble(bonusStr); } catch (NumberFormatException ignored) {}
                    }

                    generateAndSavePayslip(selectedMonth, daysWorked, overtimeHours, transportAllowance, performanceBonus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateAndSavePayslip(String monthYear, int daysWorked, double overtimeHours,
                                         double transportAllowance, double performanceBonus) {
        if (uid == null) return;

        double basicSalary = monthlySalary;
        double overtimePay = (basicSalary / 200) * overtimeHours * 1.5;
        double grossEarnings = basicSalary + transportAllowance + performanceBonus + overtimePay;
        double epfDeduction = basicSalary * 0.08;
        double incomeTax = calculateIncomeTax(grossEarnings);
        double totalDeductions = epfDeduction + incomeTax;
        double netSalary = grossEarnings - totalDeductions;

        Map<String, Object> payslipData = new HashMap<>();
        payslipData.put("monthYear", monthYear);
        payslipData.put("basicSalary", basicSalary);
        payslipData.put("transportAllowance", transportAllowance);
        payslipData.put("performanceBonus", performanceBonus);
        payslipData.put("overtimePay", overtimePay);
        payslipData.put("grossEarnings", grossEarnings);
        payslipData.put("epfDeduction", epfDeduction);
        payslipData.put("incomeTax", incomeTax);
        payslipData.put("totalDeductions", totalDeductions);
        payslipData.put("netSalary", netSalary);
        payslipData.put("daysWorked", daysWorked);
        payslipData.put("overtimeHours", overtimeHours);
        payslipData.put("createdAt", System.currentTimeMillis());

        // Calculate YTD
        calculateYtd(payslipData);

        db.collection("users").document(uid)
                .collection("payslips").add(payslipData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Payslip generated for " + monthYear, Toast.LENGTH_SHORT).show();
                    loadPayslipsFromFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private double calculateIncomeTax(double monthlyGross) {
        // Sri Lankan monthly income tax brackets (2024)
        double annualGross = monthlyGross * 12;
        double annualTax = 0;

        if (annualGross <= 1200000) {
            annualTax = 0;
        } else if (annualGross <= 1800000) {
            annualTax = (annualGross - 1200000) * 0.06;
        } else if (annualGross <= 2400000) {
            annualTax = 36000 + (annualGross - 1800000) * 0.12;
        } else if (annualGross <= 3600000) {
            annualTax = 108000 + (annualGross - 2400000) * 0.18;
        } else if (annualGross <= 4800000) {
            annualTax = 324000 + (annualGross - 3600000) * 0.24;
        } else if (annualGross <= 7200000) {
            annualTax = 612000 + (annualGross - 4800000) * 0.30;
        } else {
            annualTax = 1332000 + (annualGross - 7200000) * 0.36;
        }

        return annualTax / 12.0;
    }

    private void calculateYtd(Map<String, Object> newPayslip) {
        // YTD will be calculated when displaying, based on all payslips in current year
        double ytdGross = getDoubleValue(newPayslip, "grossEarnings");
        double ytdDeductions = getDoubleValue(newPayslip, "totalDeductions");
        double ytdNet = getDoubleValue(newPayslip, "netSalary");

        // Add previous payslips in this year
        String newMonthYear = (String) newPayslip.get("monthYear");
        if (newMonthYear != null) {
            String newYear = newMonthYear.contains(" ") ? newMonthYear.split(" ")[1] : "";
            for (Map<String, Object> existing : payslipList) {
                String existingMonthYear = (String) existing.get("monthYear");
                if (existingMonthYear != null && existingMonthYear.contains(newYear)) {
                    ytdGross += getDoubleValue(existing, "grossEarnings");
                    ytdDeductions += getDoubleValue(existing, "totalDeductions");
                    ytdNet += getDoubleValue(existing, "netSalary");
                }
            }
        }

        newPayslip.put("ytdGross", ytdGross);
        newPayslip.put("ytdDeductions", ytdDeductions);
        newPayslip.put("ytdNet", ytdNet);
    }

    private void loadPayslipsFromFirestore() {
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("payslips")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    payslipList.clear();
                    monthLabels.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        payslipList.add(data);
                        String monthYear = doc.getString("monthYear");
                        if (monthYear != null) {
                            monthLabels.add(monthYear);
                        } else {
                            monthLabels.add("Unknown Month");
                        }
                    }

                    if (payslipList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        findViewById(R.id.btnDownload).setVisibility(View.GONE);
                        spinnerMonth.setVisibility(View.GONE);
                        loadDefaultValues();
                    } else {
                        emptyState.setVisibility(View.GONE);
                        findViewById(R.id.btnDownload).setVisibility(View.VISIBLE);
                        spinnerMonth.setVisibility(View.VISIBLE);
                        setupMonthSelector();
                        displayPayslip(0);
                    }
                });
    }

    private void setupMonthSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, monthLabels);
        spinnerMonth.setAdapter(adapter);
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPayslipIndex = position;
                displayPayslip(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void displayPayslip(int index) {
        if (index < 0 || index >= payslipList.size()) return;
        Map<String, Object> payslip = payslipList.get(index);

        String monthYear = getStringValue(payslip, "monthYear", "-- -- ----");
        double basicSalary = getDoubleValue(payslip, "basicSalary");
        double transportAllowance = getDoubleValue(payslip, "transportAllowance");
        double performanceBonus = getDoubleValue(payslip, "performanceBonus");
        double epfDeduction = getDoubleValue(payslip, "epfDeduction");
        double incomeTax = getDoubleValue(payslip, "incomeTax");
        int daysWorked = getIntValue(payslip, "daysWorked");
        double overtimeHours = getDoubleValue(payslip, "overtimeHours");
        double ytdGross = getDoubleValue(payslip, "ytdGross");
        double ytdDeductions = getDoubleValue(payslip, "ytdDeductions");
        double ytdNet = getDoubleValue(payslip, "ytdNet");

        double grossEarnings = basicSalary + transportAllowance + performanceBonus;
        double totalDeductions = epfDeduction + incomeTax;
        double netSalary = grossEarnings - totalDeductions;

        tvMonthYear.setText(monthYear);
        tvNetSalary.setText(String.format(Locale.US, "LKR %,.2f", netSalary));
        tvDaysWorked.setText(String.valueOf(daysWorked));
        tvOvertimeHours.setText(String.format(Locale.US, "%.1fh", overtimeHours));
        tvBasicSalary.setText(String.format(Locale.US, "LKR %,.2f", basicSalary));
        tvTransportAllowance.setText(String.format(Locale.US, "LKR %,.2f", transportAllowance));
        tvPerformanceBonus.setText(String.format(Locale.US, "LKR %,.2f", performanceBonus));
        tvEpfDeduction.setText(String.format(Locale.US, "LKR %,.2f", epfDeduction));
        tvIncomeTax.setText(String.format(Locale.US, "LKR %,.2f", incomeTax));
        tvYtdGross.setText(String.format(Locale.US, "LKR %,.2f", ytdGross));
        tvYtdDeductions.setText(String.format(Locale.US, "LKR %,.2f", ytdDeductions));
        tvYtdNet.setText(String.format(Locale.US, "LKR %,.2f", ytdNet));

        if (grossEarnings > 0) {
            int earningsPct = (int) ((grossEarnings / (grossEarnings + totalDeductions)) * 100);
            int deductionsPct = 100 - earningsPct;
            tvEarningsPercent.setText(String.format(Locale.US, "%d%% Earnings", earningsPct));
            tvDeductionsPercent.setText(String.format(Locale.US, "%d%% Deductions", deductionsPct));

            android.widget.LinearLayout.LayoutParams earningsParams =
                    (android.widget.LinearLayout.LayoutParams) viewEarningsBar.getLayoutParams();
            earningsParams.weight = earningsPct;
            viewEarningsBar.setLayoutParams(earningsParams);

            android.widget.LinearLayout.LayoutParams deductionsParams =
                    (android.widget.LinearLayout.LayoutParams) viewDeductionsBar.getLayoutParams();
            deductionsParams.weight = deductionsPct;
            viewDeductionsBar.setLayoutParams(deductionsParams);
        }
    }

    private void loadDefaultValues() {
        tvMonthYear.setText("No payslip data");
        tvNetSalary.setText("LKR 0.00");
        tvDaysWorked.setText("0");
        tvOvertimeHours.setText("0h");
        tvBasicSalary.setText("LKR 0.00");
        tvTransportAllowance.setText("LKR 0.00");
        tvPerformanceBonus.setText("LKR 0.00");
        tvEpfDeduction.setText("LKR 0.00");
        tvIncomeTax.setText("LKR 0.00");
        tvYtdGross.setText("LKR 0.00");
        tvYtdDeductions.setText("LKR 0.00");
        tvYtdNet.setText("LKR 0.00");
    }

    private void downloadPayslipPdf() {
        if (selectedPayslipIndex < 0 || selectedPayslipIndex >= payslipList.size()) {
            Toast.makeText(this, "No payslip to download", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> payslip = payslipList.get(selectedPayslipIndex);
        String monthYear = getStringValue(payslip, "monthYear", "Payslip");
        String fileName = "Payslip_" + monthYear.replace(" ", "_") + ".pdf";
        String pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle("Download Payslip");
            request.setDescription("Downloading " + monthYear + " payslip");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private int getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }
}
