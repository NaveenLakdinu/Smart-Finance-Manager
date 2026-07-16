package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UtilityManagerActivity extends AppCompatActivity {

    private static final String TAG = "UtilityManager";

    private View btnRegisterNewBill;
    private View btnGetReport;
    private View btnViewBill;
    private ImageView backButton;

    private TextView txtHeroBillName;
    private TextView txtHeroBillDueDate;
    private LinearLayout layoutRecentBills;

    private FirebaseFirestore db;

    // Supported date formats for automatic parsing fallback
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd"
    };

    // Helper class to associate a BillModel with its dynamically calculated next due date
    private static class CalculatedBill implements Comparable<CalculatedBill> {
        RegisterBillActivity.BillModel bill;
        Date nextDueDate;

        CalculatedBill(RegisterBillActivity.BillModel bill, Date nextDueDate) {
            this.bill = bill;
            this.nextDueDate = nextDueDate;
        }

        @Override
        public int compareTo(CalculatedBill o) {
            return this.nextDueDate.compareTo(o.nextDueDate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_manager);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDashboardDataFromFirestore();
    }

    private void initViews() {
        btnRegisterNewBill = findViewById(R.id.btnRegisterNewBill);
        btnGetReport = findViewById(R.id.btnGetReport);
        btnViewBill = findViewById(R.id.btnViewBill);
        backButton = findViewById(R.id.backButton);

        txtHeroBillName = findViewById(R.id.txtHeroBillName);
        txtHeroBillDueDate = findViewById(R.id.txtHeroBillDueDate);
        layoutRecentBills = findViewById(R.id.layoutRecentBills);
    }

    private void fetchDashboardDataFromFirestore() {
        db.collection("bills")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CalculatedBill> calculatedBills = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        RegisterBillActivity.BillModel bill = doc.toObject(RegisterBillActivity.BillModel.class);
                        if (bill != null && bill.getDueDate() != null) {
                            Date nextDue = calculateNextDueDate(bill.getDueDate());
                            if (nextDue != null) {
                                calculatedBills.add(new CalculatedBill(bill, nextDue));
                            }
                        }
                    }

                    if (!calculatedBills.isEmpty()) {
                        // FIX: Sort the bills in memory by their true upcoming rolled-forward date
                        Collections.sort(calculatedBills);

                        // 1. POPULATE HERO CARD WITH THE TRUE CLOSEST UPCOMING BILL
                        CalculatedBill closestCalculated = calculatedBills.get(0);
                        RegisterBillActivity.BillModel closestBill = closestCalculated.bill;

                        if (txtHeroBillName != null) {
                            txtHeroBillName.setText(closestBill.getName());
                        }

                        if (txtHeroBillDueDate != null) {
                            // Format to only "dd MMMM" (e.g. "18 July")
                            SimpleDateFormat heroFormatter = new SimpleDateFormat("dd MMMM", Locale.getDefault());
                            txtHeroBillDueDate.setText("Due on: " + heroFormatter.format(closestCalculated.nextDueDate));
                        }

                        // Schedule the monthly 1-day-before warning notification
                        scheduleNotification(closestBill, closestCalculated.nextDueDate);

                        // 2. POPULATE THE RECENT UTILITIES LIST (MAX 5 ITEMS)
                        layoutRecentBills.removeAllViews();

                        int itemsToShow = Math.min(calculatedBills.size(), 5);
                        LayoutInflater inflater = LayoutInflater.from(this);

                        for (int i = 0; i < itemsToShow; i++) {
                            CalculatedBill item = calculatedBills.get(i);
                            RegisterBillActivity.BillModel currentBill = item.bill;

                            View rowCard = inflater.inflate(R.layout.item_bill, layoutRecentBills, false);

                            TextView txtName = rowCard.findViewById(R.id.txtBillItemName);
                            TextView txtAccNo = rowCard.findViewById(R.id.txtBillItemAccNo);
                            TextView txtDate = rowCard.findViewById(R.id.txtBillItemDate);
                            ImageView imgIcon = rowCard.findViewById(R.id.imgCategoryIcon);

                            if (txtName != null) txtName.setText(currentBill.getName());
                            if (txtAccNo != null) txtAccNo.setText("Acc: " + currentBill.getAccountNo());

                            if (txtDate != null) {
                                SimpleDateFormat listFormatter = new SimpleDateFormat("dd MMMM", Locale.getDefault());
                                txtDate.setText("Due: " + listFormatter.format(item.nextDueDate));
                            }

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

                            // FIX: Make recent bill layout row clickable to open details/view list
                            rowCard.setOnClickListener(v -> {
                                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                                startActivity(intent);
                            });

                            layoutRecentBills.addView(rowCard);
                        }
                    } else {
                        if (txtHeroBillName != null) txtHeroBillName.setText("No Pending Bills");
                        if (txtHeroBillDueDate != null) txtHeroBillDueDate.setText("All caught up!");
                        layoutRecentBills.removeAllViews();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Dashboard Sync Failed", e);
                    Toast.makeText(this, "Dashboard Sync Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Parses the date string and dynamically calculates the nearest upcoming date
     * by rolling the month forward if the date has passed.
     */
    private Date calculateNextDueDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;

        Date originalDate = null;
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.getDefault());
                parser.setLenient(false);
                originalDate = parser.parse(dateStr);
                break;
            } catch (ParseException ignored) {
            }
        }

        if (originalDate == null) {
            Log.w(TAG, "Unparseable date: " + dateStr);
            return null;
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar billCal = Calendar.getInstance();
        billCal.setTime(originalDate);

        // Bring year and month up to the current period to evaluate
        billCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
        billCal.set(Calendar.MONTH, today.get(Calendar.MONTH));

        // If this month's date has passed, advance to the next month
        if (billCal.before(today)) {
            billCal.add(Calendar.MONTH, 1);
        }

        return billCal.getTime();
    }

    /**
     * Schedules a recurring monthly notification 1 day before the computed next due date.
     */
    private void scheduleNotification(RegisterBillActivity.BillModel bill, Date nextDueDate) {
        Calendar today = Calendar.getInstance();
        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTime(nextDueDate);

        // Set to exactly 1 day before the due date at 09:00 AM
        reminderCal.add(Calendar.DAY_OF_YEAR, -1);
        reminderCal.set(Calendar.HOUR_OF_DAY, 9);
        reminderCal.set(Calendar.MINUTE, 0);
        reminderCal.set(Calendar.SECOND, 0);

        // If the reminder date for this month has already passed, set it for next month's reminder cycle
        if (reminderCal.before(today)) {
            reminderCal.add(Calendar.MONTH, 1);
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("BILL_NAME", bill.getName());

        int pendingIntentId = bill.getName().hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminderCal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 30, // Monthly recurrence
                    pendingIntent
            );
        }
    }

    private void setupClickListeners() {
        if (btnRegisterNewBill != null) {
            btnRegisterNewBill.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, RegisterBillActivity.class);
                startActivity(intent);
            });
        }

        if (btnGetReport != null) {
            btnGetReport.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityReportFormActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewBill != null) {
            btnViewBill.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                startActivity(intent);
            });
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        View txtSeeAllRecent = findViewById(R.id.txtSeeAllRecent);
        if (txtSeeAllRecent != null) {
            txtSeeAllRecent.setOnClickListener(v -> {
                Intent intent = new Intent(UtilityManagerActivity.this, UtilityBillActivity.class);
                startActivity(intent);
            });
        }
    }
}