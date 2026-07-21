package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UtilityBillActivity extends AppCompatActivity {

    private static final String TAG = "UtilityBillActivity";

    private ImageView btnBack;
    private RecyclerView recyclerBills;
    private UtilityAdapter adapter;
    private ArrayList<UtilityBill> billList;
    private FirebaseFirestore db;
    private android.view.View btnManageUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_bills);

        NotificationHelper.createNotificationChannels(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        db = FirebaseFirestore.getInstance();
        billList = new ArrayList<>();

        btnBack = findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> finish());

        btnManageUtility = findViewById(R.id.recyclerBills);
        if (btnManageUtility != null) {
            btnManageUtility.setOnClickListener(v -> {
                // Open your management/form activity layout to add a new bill
                Intent intent = new Intent(UtilityBillActivity.this, UtilityReportFormActivity.class);
                startActivity(intent);
            });
        }

        recyclerBills = findViewById(R.id.recyclerBills);
        recyclerBills.setLayoutManager(new LinearLayoutManager(this));

        // FIX: Replaced the single-parameter lambda with the fully fleshed-out interface listener instance
        adapter = new UtilityAdapter(this, billList, new UtilityAdapter.OnUtilityClickListener() {
            @Override
            public void onDeleteClick(UtilityBill bill) {
                deleteBillFromFirestore(bill);
            }

            @Override
            public void onCardClick(UtilityBill bill) {
                // Tapping anywhere on the item card or the edit button routes here
                if (bill.getId() != null) {
                    Intent intent = new Intent(UtilityBillActivity.this, UpdateBillActivity.class);
                    intent.putExtra("BILL_ID", bill.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(UtilityBillActivity.this, "Error: Missing bill tracking code ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerBills.setAdapter(adapter);

        listenForRealtimeBills();
    }

    private void deleteBillFromFirestore(UtilityBill bill) {
        if (bill.getId() == null) return;

        db.collection("utilityBill")
                .document(bill.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UtilityBillActivity.this, "Bill deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UtilityBillActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForRealtimeBills() {
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Log.e(TAG, "No user logged in.");
            return;
        }

        db.collection("utilityBill")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error fetching records: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        billList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            UtilityBill bill = doc.toObject(UtilityBill.class);

                            if (bill != null) {
                                bill.setId(doc.getId());
                                billList.add(bill);

                                // Refresh system notification engine
                                scheduleBillNotification(bill.getBillName(), bill.getPaymentDate());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * FIX: Calculates the next upcoming occurrence exactly 1 day before the target monthly day.
     * Prevents security crashes while safely rolling over target timelines automatically.
     */
    private void scheduleBillNotification(String billName, String dueDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dueDateStr);
            if (date != null) {
                Calendar baseCalendar = Calendar.getInstance();
                baseCalendar.setTime(date);

                // Get the base day configuration (e.g., the 25th)
                int targetDayOfMonth = baseCalendar.get(Calendar.DAY_OF_MONTH);

                Calendar reminderCalendar = Calendar.getInstance();
                // Match the target day, but place it in the current month and year
                reminderCalendar.set(Calendar.DAY_OF_MONTH, targetDayOfMonth);
                reminderCalendar.set(Calendar.HOUR_OF_DAY, 9);
                reminderCalendar.set(Calendar.MINUTE, 0);
                reminderCalendar.set(Calendar.SECOND, 0);
                reminderCalendar.set(Calendar.MILLISECOND, 0);

                // FIX 1: Shift back by exactly 1 day (due tomorrow logic)
                reminderCalendar.add(Calendar.DAY_OF_YEAR, -1);

                // FIX 2: Monthly rollover logic. If the calculated reminder day for this month
                // has already passed, automatically push it forward to next month.
                if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    reminderCalendar.add(Calendar.MONTH, 1);
                }

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("BILL_NAME", billName);

                int uniqueIntentId = billName.hashCode();

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, uniqueIntentId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
                        }
                    } catch (SecurityException se) {
                        se.printStackTrace();
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}