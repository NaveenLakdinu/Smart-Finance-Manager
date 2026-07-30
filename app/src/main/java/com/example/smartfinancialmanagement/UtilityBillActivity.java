package com.example.smartfinancialmanagement;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UtilityBillActivity extends AppCompatActivity {

    private static final String TAG = "UtilityBillActivity";

    private ImageView btnBack;
    private RecyclerView recyclerBills;
    private UtilityAdapter adapter;
    private ArrayList<UtilityBill> billList;
    private FirebaseFirestore db;

    // Supported date formats for automatic parsing fallback
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd"
    };

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

        // Set up RecyclerView
        recyclerBills = findViewById(R.id.recyclerBills);
        recyclerBills.setLayoutManager(new LinearLayoutManager(this));

        // Listener handling: Only delete button and full card click
        adapter = new UtilityAdapter(this, billList, new UtilityAdapter.OnUtilityClickListener() {
            @Override
            public void onDeleteClick(UtilityBill bill) {
                deleteBillFromFirestore(bill);
            }

            @Override
            public void onCardClick(UtilityBill bill) {
                // Tapping anywhere on the item card routes to UpdateBillActivity
                if (bill.getId() != null) {
                    Intent intent = new Intent(UtilityBillActivity.this, UpdateBillActivity.class);
                    intent.putExtra("BILL_ID", bill.getId());
                    intent.putExtra("DOCUMENT_ID", bill.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(UtilityBillActivity.this, "Error: Missing bill document ID", Toast.LENGTH_SHORT).show();
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

                                // Refresh monthly recurring notification alarm
                                scheduleBillNotification(bill.getBillName(), bill.getPaymentDate());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Parses the stored bill due date string using fallback parsers and calculates
     * the exact reminder trigger time (1 day before the monthly due date at 09:00 AM).
     * If the current month's reminder has passed, it rolls forward to next month.
     */
    private void scheduleBillNotification(String billName, String dueDateStr) {
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) return;

        Date originalDate = null;
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.getDefault());
                parser.setLenient(false);
                originalDate = parser.parse(dueDateStr);
                if (originalDate != null) break;
            } catch (ParseException ignored) {
            }
        }

        if (originalDate == null) {
            Log.w(TAG, "Unparseable date format for bill: " + billName + " (" + dueDateStr + ")");
            return;
        }

        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTime(originalDate);

        // Target day of the month (e.g., 25th)
        int targetDayOfMonth = baseCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(Calendar.DAY_OF_MONTH, targetDayOfMonth);
        reminderCalendar.set(Calendar.HOUR_OF_DAY, 9);
        reminderCalendar.set(Calendar.MINUTE, 0);
        reminderCalendar.set(Calendar.SECOND, 0);
        reminderCalendar.set(Calendar.MILLISECOND, 0);

        // Shift back by 1 day (due tomorrow notification)
        reminderCalendar.add(Calendar.DAY_OF_YEAR, -1);

        // If this month's reminder date/time has passed, advance to next month
        if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            reminderCalendar.add(Calendar.MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("BILL_NAME", billName);

        int uniqueIntentId = billName.hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                uniqueIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
                }
            } catch (SecurityException se) {
                Log.e(TAG, "SecurityException scheduling alarm", se);
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}