package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilityReportFormActivity extends AppCompatActivity {

    private FrameLayout backButtonContainer;
    private Spinner spinnerChooseBill, spinnerChooseMonth;
    private EditText editBillAmount;
    private Button btnNextReport;

    private FirebaseFirestore db;
    private List<UtilityBillActivity.BillWithId> firebaseBillsList;
    private List<String> billSpinnerNames;
    private ArrayList<BillReportItem> stagedReportItems; // Holds currently added entries

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_utilityreport_form); // Your provided XML filename

        db = FirebaseFirestore.getInstance();
        firebaseBillsList = new ArrayList<>();
        billSpinnerNames = new ArrayList<>();

        // Fix: Type-safe, deprecation-free Intent bundle unpacking
        if (getIntent().hasExtra("STAGED_ITEMS")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ type-safe alternative
                @SuppressWarnings("unchecked")
                ArrayList<BillReportItem> items = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("STAGED_ITEMS", ArrayList.class);
                stagedReportItems = items != null ? items : new ArrayList<>();
            } else {
                // Backward compatibility fallback for older APIs
                @SuppressWarnings("unchecked")
                ArrayList<BillReportItem> items = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("STAGED_ITEMS");
                stagedReportItems = items != null ? items : new ArrayList<>();
            }
        } else {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupMonthSpinner();
        fetchSavedBillsFromFirestore();
        setupClickListeners();
    }

    private void initViews() {
        backButtonContainer = findViewById(R.id.backButtonContainer);
        spinnerChooseBill = findViewById(R.id.ChooseBill);
        spinnerChooseMonth = findViewById(R.id.ChooseMonth);
        editBillAmount = findViewById(R.id.Bill);
        btnNextReport = findViewById(R.id.NextReport);
    }

    private void setupMonthSpinner() {
        List<String> months = new ArrayList<>();
        months.add("January"); months.add("February"); months.add("March");
        months.add("April");   months.add("May");      months.add("June");
        months.add("July");    months.add("August");   months.add("September");
        months.add("October"); months.add("November"); months.add("December");

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChooseMonth.setAdapter(monthAdapter);
    }

    private void fetchSavedBillsFromFirestore() {
        db.collection("bills").get().addOnSuccessListener(queryDocumentSnapshots -> {
            firebaseBillsList.clear();
            billSpinnerNames.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                RegisterBillActivity.BillModel bill = doc.toObject(RegisterBillActivity.BillModel.class);
                if (bill != null) {
                    firebaseBillsList.add(new UtilityBillActivity.BillWithId(doc.getId(), bill));
                    // Display name and account number clearly inside the dropdown menu element
                    billSpinnerNames.add(bill.getName() + " (" + bill.getAccountNo() + ")");
                }
            }

            ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, billSpinnerNames);
            billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChooseBill.setAdapter(billAdapter);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load saved bills", Toast.LENGTH_SHORT).show());
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> finish());

        btnNextReport.setOnClickListener(v -> {
            if (validateAndStageItem()) {
                // Head directly into summary layout view processing
                Intent intent = new Intent(UtilityReportFormActivity.this, ReportSummaryActivity.class);
                intent.putExtra("STAGED_ITEMS", stagedReportItems);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateAndStageItem() {
        if (firebaseBillsList.isEmpty() || spinnerChooseBill.getSelectedItem() == null) {
            Toast.makeText(this, "No valid bill selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editBillAmount.getText().toString().trim().isEmpty()) {
            editBillAmount.setError("Please supply the bill payment amount");
            return false;
        }

        // 1. Check Total Constraint Limit: Max 5 items total allowed
        if (stagedReportItems.size() >= 6) {
            Toast.makeText(this, "Limit reached! Maximum of 6 total bills can be chosen.", Toast.LENGTH_LONG).show();
            return false;
        }

        int selectedIndex = spinnerChooseBill.getSelectedItemPosition();
        UtilityBillActivity.BillWithId selectedBillWrapper = firebaseBillsList.get(selectedIndex);
        String selectedMonth = spinnerChooseMonth.getSelectedItem().toString();
        double amount = Double.parseDouble(editBillAmount.getText().toString().trim());

        // Process existing validation conditions across our active stack entries
        Set<String> uniqueMonths = new HashSet<>();
        for (BillReportItem item : stagedReportItems) {
            uniqueMonths.add(item.getTargetMonth());

            // 2. Check Duplication Constraint: Ensure this specific bill isn't already added for this specific month
            if (item.getBillId().equals(selectedBillWrapper.id) && item.getTargetMonth().equals(selectedMonth)) {
                Toast.makeText(this, "This bill has already been added for " + selectedMonth, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        // 3. Check Month Constraint Limit: Max 3 unique months across entries
        if (!uniqueMonths.contains(selectedMonth) && uniqueMonths.size() >= 3) {
            Toast.makeText(this, "Limit reached! You can select a maximum of 3 distinct months.", Toast.LENGTH_LONG).show();
            return false;
        }

        // If constraints clear, package input into staging collection list
        BillReportItem newItem = new BillReportItem(
                selectedBillWrapper.id,
                selectedBillWrapper.billData.getName(),
                selectedBillWrapper.billData.getAccountNo(),
                selectedBillWrapper.billData.getCategory(),
                amount,
                selectedMonth
        );

        stagedReportItems.add(newItem);
        return true;
    }
}