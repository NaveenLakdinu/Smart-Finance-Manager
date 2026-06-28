package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceHubActivity extends AppCompatActivity {

    private TextView txtHeroClientName, txtHeroAmountAndDate, txtHeroIcon;
    private TextView txtFilterPending, txtFilterPaid, txtFilterDue;
    private MaterialCardView cardFilterPending, cardFilterPaid, cardFilterDue;
    private RecyclerView rvInvoices;
    private FloatingActionButton fabAddInvoice;

    // Dropdown structural bindings
    private Spinner spinnerBusinessFilter;
    private List<String> businessDropdownOptions = new ArrayList<>();
    private String currentSelectedBusinessFilter = "All Businesses"; // Default option tracking string reference

    private FirebaseFirestore db;
    private List<InvoiceModel> allInvoicesList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String currentSelectedFilter = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_hub);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupFilterListeners();
        loadBusinessFilterDropdown(); // Fetch workspaces directly from Cloud Firestore Database

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        fabAddInvoice.setOnClickListener(v -> startActivity(new Intent(this, CreateInvoiceActivity.class)));

        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchInvoicesFromDatabase();
    }

    private void initializeViews() {
        txtHeroClientName = findViewById(R.id.txtHeroClientName);
        txtHeroAmountAndDate = findViewById(R.id.txtHeroAmountAndDate);
        txtHeroIcon = findViewById(R.id.txtHeroIcon);

        cardFilterPending = findViewById(R.id.cardFilterPending);
        cardFilterPaid = findViewById(R.id.cardFilterPaid);
        cardFilterDue = findViewById(R.id.cardFilterDue);

        txtFilterPending = findViewById(R.id.txtFilterPending);
        txtFilterPaid = findViewById(R.id.txtFilterPaid);
        txtFilterDue = findViewById(R.id.txtFilterDue);

        spinnerBusinessFilter = findViewById(R.id.spinnerBusinessFilter);
        rvInvoices = findViewById(R.id.rvInvoices);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);
    }

    private void loadBusinessFilterDropdown() {
        db.collection("businesses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    businessDropdownOptions.clear();
                    businessDropdownOptions.add("All Businesses"); // Add master clear fallback selection anchor option

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        BusinessModel business = doc.toObject(BusinessModel.class);
                        if (business != null && business.getBusinessName() != null) {
                            businessDropdownOptions.add(business.getBusinessName());
                        }
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, businessDropdownOptions);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerBusinessFilter.setAdapter(spinnerAdapter);

                    spinnerBusinessFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            currentSelectedBusinessFilter = parent.getItemAtPosition(position).toString();
                            applyFilterAndPopulateList(); // Instantly update view when selections map context changes
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load business profile configurations", Toast.LENGTH_SHORT).show());
    }

    private void setupFilterListeners() {
        cardFilterPending.setOnClickListener(v -> updateFilterUI("pending"));
        cardFilterPaid.setOnClickListener(v -> updateFilterUI("paid"));
        cardFilterDue.setOnClickListener(v -> updateFilterUI("due"));
    }

    private void updateFilterUI(String selectedFilter) {
        currentSelectedFilter = selectedFilter;

        txtFilterPending.setBackgroundResource(R.drawable.bg_glass_card);
        txtFilterPending.setTextColor(Color.parseColor("#F0F6FF"));

        txtFilterPaid.setBackgroundResource(R.drawable.bg_glass_card);
        txtFilterPaid.setTextColor(Color.parseColor("#F0F6FF"));

        txtFilterDue.setBackgroundResource(R.drawable.bg_glass_card);
        txtFilterDue.setTextColor(Color.parseColor("#F0F6FF"));

        if (selectedFilter.equals("pending")) {
            txtFilterPending.setBackgroundColor(Color.parseColor("#00D4AA"));
            txtFilterPending.setTextColor(Color.parseColor("#071A33"));
        } else if (selectedFilter.equals("paid")) {
            txtFilterPaid.setBackgroundColor(Color.parseColor("#00D4AA"));
            txtFilterPaid.setTextColor(Color.parseColor("#071A33"));
        } else if (selectedFilter.equals("due")) {
            txtFilterDue.setBackgroundColor(Color.parseColor("#00D4AA"));
            txtFilterDue.setTextColor(Color.parseColor("#071A33"));
        }

        applyFilterAndPopulateList();
    }

    private void fetchInvoicesFromDatabase() {
        db.collection("invoices")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allInvoicesList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        InvoiceModel item = doc.toObject(InvoiceModel.class);
                        if (item != null) {
                            if (item.getStatus() == null) item.setStatus("pending");
                            allInvoicesList.add(item);
                        }
                    }

                    sortInvoicesChronological(allInvoicesList);
                    populateHeroCardClosestInvoice();
                    applyFilterAndPopulateList();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load invoices", Toast.LENGTH_SHORT).show());
    }

    private void populateHeroCardClosestInvoice() {
        long currentMillis = System.currentTimeMillis();
        InvoiceModel closestInvoice = null;
        long minimumTimeDiff = Long.MAX_VALUE;

        for (InvoiceModel inv : allInvoicesList) {
            if (!inv.getStatus().equalsIgnoreCase("paid") && inv.getPaymentDueDate() != null) {
                try {
                    Date dueDate = dateFormat.parse(inv.getPaymentDueDate());
                    if (dueDate != null) {
                        long timeDiff = Math.abs(dueDate.getTime() - currentMillis);
                        if (timeDiff < minimumTimeDiff) {
                            minimumTimeDiff = timeDiff;
                            closestInvoice = inv;
                        }
                    }
                } catch (ParseException ignored) {}
            }
        }

        if (closestInvoice != null) {
            txtHeroClientName.setText(closestInvoice.getClientName() + " (" + closestInvoice.getSelectedBusiness() + ")");
            txtHeroAmountAndDate.setText(String.format(Locale.getDefault(), "Rs. %.2f\nDue: %s",
                    closestInvoice.getGrandTotal(), closestInvoice.getPaymentDueDate()));
            txtHeroIcon.setText(closestInvoice.getStatus().equals("due") ? "🚨" : "⏳");
        } else {
            txtHeroClientName.setText("All clear!");
            txtHeroAmountAndDate.setText("No upcoming obligations");
            txtHeroIcon.setText("✅");
        }
    }

    private void applyFilterAndPopulateList() {
        List<InvoiceModel> filteredList = new ArrayList<>();

        for (InvoiceModel inv : allInvoicesList) {
            boolean matchesStatus = inv.getStatus().equalsIgnoreCase(currentSelectedFilter);

            // Evaluates workspace criteria parameter contexts
            boolean matchesBusiness = currentSelectedBusinessFilter.equals("All Businesses")
                    || (inv.getSelectedBusiness() != null && inv.getSelectedBusiness().equalsIgnoreCase(currentSelectedBusinessFilter));

            if (matchesStatus && matchesBusiness) {
                filteredList.add(inv);
            }
        }

        InvoiceAdapter adapter = new InvoiceAdapter(filteredList);
        rvInvoices.setAdapter(adapter);
    }

    private void sortInvoicesChronological(List<InvoiceModel> list) {
        Collections.sort(list, (inv1, inv2) -> {
            if (inv1.getPaymentDueDate() == null || inv2.getPaymentDueDate() == null) return 0;
            try {
                Date d1 = dateFormat.parse(inv1.getPaymentDueDate());
                Date d2 = dateFormat.parse(inv2.getPaymentDueDate());
                if (d1 != null && d2 != null) {
                    return d1.compareTo(d2);
                }
            } catch (ParseException ignored) {}
            return 0;
        });
    }
}