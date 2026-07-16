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
import androidx.annotation.NonNull;
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

    // 💡 DataBridge වෙනුවට Intent මඟින් එහා මෙහා යන ලැයිස්තුව
    private ArrayList<BillReportItem> stagedReportItems;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_utilityreport_form);

        db = FirebaseFirestore.getInstance();
        firebaseBillsList = new ArrayList<>();
        billSpinnerNames = new ArrayList<>();

        // 💡 FIX 1: පෙර තිරයෙන් (ReportSummaryActivity එකෙන්) එවූ දැනට එකතු කර ඇති දත්ත ලැයිස්තුව Intent එකෙන් ආරක්ෂිතව කියවීම
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                stagedReportItems = savedInstanceState.getSerializable("STAGED_ITEMS", ArrayList.class);
            } else {
                stagedReportItems = (ArrayList<BillReportItem>) savedInstanceState.getSerializable("STAGED_ITEMS");
            }
        }

        if (stagedReportItems == null) {
            if (getIntent() != null && getIntent().hasExtra("STAGED_ITEMS")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    stagedReportItems = getIntent().getSerializableExtra("STAGED_ITEMS", ArrayList.class);
                } else {
                    stagedReportItems = (ArrayList<BillReportItem>) getIntent().getSerializableExtra("STAGED_ITEMS");
                }
            }
        }

        // තවමත් ලැයිස්තුව හිස් නම් අලුත් එකක් සාදන්න
        if (stagedReportItems == null) {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupMonthSpinner();
        fetchSavedBillsFromFirestore();
        setupClickListeners();
    }

    // Screen rotation වලදී දත්ත තාවකාලිකව රඳවා ගැනීම
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("STAGED_ITEMS", stagedReportItems);
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
                // 💡 FIX 2: DataBridge වෙනුවට කෙලින්ම Intent එක හරහා Summary තිරයට ලැයිස්තුව Pass කිරීම
                Intent intent = new Intent(UtilityReportFormActivity.this, ReportSummaryActivity.class);
                intent.putExtra("STAGED_ITEMS", stagedReportItems);
                startActivity(intent);
                finish(); // පැරණි Form එක මතකයෙන් ඉවත් කරයි
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

        // උපරිම බිල්පත් 5කට සීමා කිරීම
        if (stagedReportItems.size() >= 5) {
            Toast.makeText(this, "Limit reached! Maximum of 5 total bills can be chosen.", Toast.LENGTH_LONG).show();
            return false;
        }

        int selectedIndex = spinnerChooseBill.getSelectedItemPosition();
        UtilityBillActivity.BillWithId selectedBillWrapper = firebaseBillsList.get(selectedIndex);
        String selectedMonth = spinnerChooseMonth.getSelectedItem().toString();

        double amount;
        try {
            amount = Double.parseDouble(editBillAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            editBillAmount.setError("Invalid amount entered");
            return false;
        }

        Set<String> uniqueMonths = new HashSet<>();
        for (BillReportItem item : stagedReportItems) {
            uniqueMonths.add(item.getTargetMonth());

            if (item.getBillId().equals(selectedBillWrapper.id) && item.getTargetMonth().equals(selectedMonth)) {
                Toast.makeText(this, "This bill has already been added for " + selectedMonth, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (!uniqueMonths.contains(selectedMonth) && uniqueMonths.size() >= 3) {
            Toast.makeText(this, "Limit reached! You can select a maximum of 3 distinct months.", Toast.LENGTH_LONG).show();
            return false;
        }

        BillReportItem newItem = new BillReportItem(
                selectedBillWrapper.id,
                selectedBillWrapper.billData.getName(),
                selectedBillWrapper.billData.getAccountNo(),
                selectedBillWrapper.billData.getCategory(),
                amount,
                selectedMonth
        );

        stagedReportItems.add(newItem);
        Toast.makeText(this, "Bill added to list", Toast.LENGTH_SHORT).show();
        return true;
    }
}