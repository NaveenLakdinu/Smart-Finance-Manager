package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilityReportFormActivity extends AppCompatActivity {

    private static final String TAG = "UtilityReportForm";
    private FrameLayout backButtonContainer;
    private Spinner spinnerChooseBill, spinnerChooseMonth;
    private EditText editBillAmount;
    private Button btnNextReport;

    private FirebaseFirestore db;
    private List<UtilityBill> firebaseBillsList;
    private List<String> billSpinnerNames;

    private ArrayList<BillReportItem> stagedReportItems;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_utilityreport_form);

        db = FirebaseFirestore.getInstance();
        firebaseBillsList = new ArrayList<>();
        billSpinnerNames = new ArrayList<>();

        // Intent bundle deserialization data recovery fallback block
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

        if (stagedReportItems == null) {
            stagedReportItems = new ArrayList<>();
        }

        initViews();
        setupMonthSpinner();
        fetchSavedBillsFromFirestore();
        setupClickListeners();
    }

    // Screen rotation
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
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e(TAG, "No user logged in.");
            Toast.makeText(this, "User session not found", Toast.LENGTH_SHORT).show();
            return;
        }
        // FIX: Collection switched to "utilityBill" and added user validation query filtering
        db.collection("utilityBill")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    firebaseBillsList.clear();
                    billSpinnerNames.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // FIX: Parsed using unified UtilityBill class structure directly
                        UtilityBill bill = doc.toObject(UtilityBill.class);
                        if (bill != null) {
                            bill.setId(doc.getId()); // Inject collection document ID tracking string
                            firebaseBillsList.add(bill);
                            billSpinnerNames.add(bill.getBillName() + " (" + bill.getAccountNo() + ")");
                        }
                    }

                    ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, billSpinnerNames);
                    billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerChooseBill.setAdapter(billAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load saved bills", Toast.LENGTH_SHORT).show());
    }

    private void setupClickListeners() {
        backButtonContainer.setOnClickListener(v -> finish());

        btnNextReport.setOnClickListener(v -> {
            if (validateAndStageItem()) {

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

        if (stagedReportItems.size() >= 5) {
            Toast.makeText(this, "Limit reached! Maximum of 5 total bills can be chosen.", Toast.LENGTH_LONG).show();
            return false;
        }

        int selectedIndex = spinnerChooseBill.getSelectedItemPosition();
        UtilityBill selectedBill = firebaseBillsList.get(selectedIndex);
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

            if (item.getBillId().equals(selectedBill.getId()) && item.getTargetMonth().equals(selectedMonth)) {
                Toast.makeText(this, "This bill has already been added for " + selectedMonth, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (!uniqueMonths.contains(selectedMonth) && uniqueMonths.size() >= 3) {
            Toast.makeText(this, "Limit reached! You can select a maximum of 3 distinct months.", Toast.LENGTH_LONG).show();
            return false;
        }

        BillReportItem newItem = new BillReportItem(
                selectedBill.getId(),
                selectedBill.getBillName(),
                selectedBill.getAccountNo(),
                selectedBill.getCategory(),
                amount,
                selectedMonth
        );

        stagedReportItems.add(newItem);
        Toast.makeText(this, "Bill added to list", Toast.LENGTH_SHORT).show();
        return true;
    }
}