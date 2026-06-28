package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class UtilityBillActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerBills;
    private BillAdapter adapter;
    private List<BillWithId> billList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_bills);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission has already been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request the permission from the user
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance();
        billList = new ArrayList<>();

        // Handle back navigation button click
        btnBack = findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> finish());

        // Bind and setup the RecyclerView UI component
        recyclerBills = findViewById(R.id.recyclerBills);
        recyclerBills.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BillAdapter(this, billList);
        recyclerBills.setAdapter(adapter);

        // Begin streaming your data in real-time from Firestore
        listenForRealtimeBills();
    }

    /**
     * Listens for changes inside your Firestore "bills" collection in real-time.
     * Updates automatically whenever you add, modify, or delete a bill.
     */
    private void listenForRealtimeBills() {
        db.collection("bills")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error fetching records: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        billList.clear(); // Clear old local list to prevent duplicates

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            // Map the Firestore document data back into our local data model structure
                            RegisterBillActivity.BillModel bill = doc.toObject(RegisterBillActivity.BillModel.class);

                            if (bill != null) {
                                // Add the compiled model along with its unique database ID to our rendering collection
                                billList.add(new BillWithId(doc.getId(), bill));

                                // Schedule or refresh monthly notification alarms 3 days prior to due dates
                                scheduleBillNotification(bill.getName(), bill.getDueDate());
                            }
                        }
                        // Refresh UI presentation changes automatically
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Checks the target bill date and automatically schedules a system broadcast alarm
     * exactly 3 days prior to the payment date every month at 9:00 AM.
     */
    private void scheduleBillNotification(String billName, String dueDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dueDateStr);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Roll back calendar precisely three days prior to specified field data
                calendar.add(Calendar.DAY_OF_YEAR, -3);
                calendar.set(Calendar.HOUR_OF_DAY, 9); // Run alarm routine in morning state
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, NotificationReceiver.class);
                intent.putExtra("BILL_NAME", billName);

                // Unique intent token wrapper using matching numeric hashes
                int uniqueIntentId = billName.hashCode();

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, uniqueIntentId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    // Schedules system alert accurately even when background device execution rests
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom lightweight object container wrapper linking individual data items
     * cleanly with their remote Firestore alphanumeric String keys.
     */
    public static class BillWithId {
        public String id;
        public RegisterBillActivity.BillModel billData;

        public BillWithId(String id, RegisterBillActivity.BillModel billData) {
            this.id = id;
            this.billData = billData;
        }
    }
}