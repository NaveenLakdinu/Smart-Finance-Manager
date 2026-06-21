package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class UtilityManagerActivity extends AppCompatActivity {

    private View btnRegisterNewBill;
    private View btnGetReport;
    private View btnViewBill;
    private ImageView backButton;

    // Updated Hero Card View bindings for closest bill detail
    private TextView txtHeroBillName;
    private TextView txtHeroBillDueDate;
    private LinearLayout layoutRecentBills;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_manager);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        fetchDashboardDataFromFirestore();
    }

    private void initViews() {
        btnRegisterNewBill = findViewById(R.id.btnRegisterNewBill);
        btnGetReport = findViewById(R.id.btnGetReport);
        btnViewBill = findViewById(R.id.btnViewBill);
        backButton = findViewById(R.id.backButton);

        // Dynamic fields inside your updated Hero card
        txtHeroBillName = findViewById(R.id.txtHeroBillName);
        txtHeroBillDueDate = findViewById(R.id.txtHeroBillDueDate);
        layoutRecentBills = findViewById(R.id.layoutRecentBills);
    }

    private void fetchDashboardDataFromFirestore() {
        // Query bills sorted by upcoming due date sequentially
        db.collection("bills")
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RegisterBillActivity.BillModel> activeBills = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        RegisterBillActivity.BillModel bill = doc.toObject(RegisterBillActivity.BillModel.class);
                        if (bill != null) {
                            activeBills.add(bill);
                        }
                    }

                    if (!activeBills.isEmpty()) {
                        // 1. POPULATE HERO CARD WITH THE CLOSEST UPCOMING BILL DATA
                        RegisterBillActivity.BillModel closestBill = activeBills.get(0);

                        if (txtHeroBillName != null) {
                            txtHeroBillName.setText(closestBill.getName());
                        }
                        if (txtHeroBillDueDate != null) {
                            txtHeroBillDueDate.setText("Due on: " + closestBill.getDueDate());
                        }

                        // 2. POPULATE THE RECENT UTILITIES LIST (MAX 5 ITEMS)
                        layoutRecentBills.removeAllViews(); // Clear default layout mock samples

                        int itemsToShow = Math.min(activeBills.size(), 5);
                        LayoutInflater inflater = LayoutInflater.from(this);

                        for (int i = 0; i < itemsToShow; i++) {
                            RegisterBillActivity.BillModel currentBill = activeBills.get(i);

                            // Dynamic entry row item inflation
                            View rowCard = inflater.inflate(R.layout.item_bill, layoutRecentBills, false);

                            TextView txtName = rowCard.findViewById(R.id.txtBillItemName);
                            TextView txtAccNo = rowCard.findViewById(R.id.txtBillItemAccNo);
                            TextView txtDate = rowCard.findViewById(R.id.txtBillItemDate);
                            ImageView imgIcon = rowCard.findViewById(R.id.imgCategoryIcon);

                            if (txtName != null) txtName.setText(currentBill.getName());
                            if (txtAccNo != null) txtAccNo.setText("Acc: " + currentBill.getAccountNo());
                            if (txtDate != null) txtDate.setText("Due: " + currentBill.getDueDate());

                            // Set corresponding icons based on asset category text strings
                            if (imgIcon != null) {
                                switch (currentBill.getCategory()) {
                                    case "Electricity":
                                        imgIcon.setImageResource(android.R.drawable.ic_menu_compass);
                                        break;
                                    case "Water":
                                        imgIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                                        break;
                                    default:
                                        imgIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                                        break;
                                }
                            }

                            layoutRecentBills.addView(rowCard);
                        }
                    } else {
                        // Safe fallback layout state configuration if database collection is empty
                        if (txtHeroBillName != null) txtHeroBillName.setText("No Pending Bills");
                        if (txtHeroBillDueDate != null) txtHeroBillDueDate.setText("All caught up!");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Dashboard Sync Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupClickListeners() {
        btnRegisterNewBill.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, RegisterBillActivity.class);
            startActivity(intent);
        });

        btnGetReport.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, UtilityReportFormActivity.class);
            startActivity(intent);
        });

        btnViewBill.setOnClickListener(v -> {
            Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());

        View txtSeeAllRecent = findViewById(R.id.txtSeeAllRecent);
        if (txtSeeAllRecent != null) {
            txtSeeAllRecent.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                startActivity(intent);
            });
        }
    }
}