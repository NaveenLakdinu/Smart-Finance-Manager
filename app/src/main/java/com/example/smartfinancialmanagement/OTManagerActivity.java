package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfinancialmanagement.model.OTEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OTManagerActivity extends AppCompatActivity {

    private TextView tvDatePicker, tvStartTime, tvEndTime, tvMonthlyTotal, tvMonthlyHours, tvEmpty, btnAddOt;
    private EditText etRate;
    private RecyclerView recyclerOtEntries;
    private ProgressBar progressLoading;

    private OTRepository repository;
    private OTEntryAdapter adapter;
    private List<OTEntry> entryList = new ArrayList<>();
    
    private String currentMonthKey;
    private String currentDisplayMonth;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat monthKeyFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final SimpleDateFormat displayMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ot_manager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        repository = new OTRepository(user.getUid());

        initViews();
        setupDefaults();
        setupClickListeners();
        loadMonthData();
    }

    private void initViews() {
        tvDatePicker = findViewById(R.id.tv_date_picker);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        etRate = findViewById(R.id.et_rate);
        btnAddOt = findViewById(R.id.btn_add_ot);
        tvMonthlyTotal = findViewById(R.id.tv_monthly_total);
        tvMonthlyHours = findViewById(R.id.tv_monthly_hours);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerOtEntries = findViewById(R.id.recycler_ot_entries);
        progressLoading = findViewById(R.id.progress_loading);
        
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerOtEntries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OTEntryAdapter(this, entryList, this::deleteEntry);
        recyclerOtEntries.setAdapter(adapter);
    }

    private void setupDefaults() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        currentMonthKey = monthKeyFormat.format(now);
        currentDisplayMonth = displayMonthFormat.format(now);
        
        tvDatePicker.setText(dateFormat.format(now));
    }

    private void setupClickListeners() {
        tvDatePicker.setOnClickListener(v -> showDatePicker());
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));
        
        btnAddOt.setOnClickListener(v -> addOtEntry());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(tvDatePicker.getText().toString()));
        } catch (Exception ignored) {}
        
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            tvDatePicker.setText(dateFormat.format(calendar.getTime()));
            
            // Note: If they pick a date in a different month, they'd be adding it 
            // to that month bucket. But for simplicity, we keep showing the current 
            // month's loaded list unless we implement a month-picker. 
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(timeFormat.parse(target.getText().toString()));
        } catch (Exception ignored) {}

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            target.setText(timeFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void addOtEntry() {
        String dateStr = tvDatePicker.getText().toString();
        String startStr = tvStartTime.getText().toString();
        String endStr = tvEndTime.getText().toString();
        String rateStr = etRate.getText().toString();

        if (rateStr.isEmpty()) {
            Toast.makeText(this, "Please enter hourly rate", Toast.LENGTH_SHORT).show();
            return;
        }

        double rate = Double.parseDouble(rateStr);
        if (rate <= 0) {
            Toast.makeText(this, "Rate must be > 0", Toast.LENGTH_SHORT).show();
            return;
        }

        double hours = calculateHours(startStr, endStr);
        if (hours <= 0) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        Date entryDate;
        try {
            entryDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            entryDate = new Date();
        }
        
        String entryMonthKey = monthKeyFormat.format(entryDate);
        String entryDisplayMonth = displayMonthFormat.format(entryDate);

        progressLoading.setVisibility(View.VISIBLE);
        
        OTEntry entry = new OTEntry(user.getUid(), dateStr, entryMonthKey, entryDisplayMonth, 
                                   startStr, endStr, hours, rate, "");

        repository.addEntry(entry, 
            docId -> {
                Toast.makeText(this, "OT Logged Successfully", Toast.LENGTH_SHORT).show();
                etRate.setText("");
                // Reload if it belongs to currently displayed month
                if (entryMonthKey.equals(currentMonthKey)) {
                    loadMonthData();
                } else {
                    progressLoading.setVisibility(View.GONE);
                }
            }, 
            e -> {
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private double calculateHours(String startStr, String endStr) {
        try {
            Date start = timeFormat.parse(startStr);
            Date end = timeFormat.parse(endStr);
            if (start != null && end != null) {
                long diffMs = end.getTime() - start.getTime();
                if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000; // Assume crossed midnight
                return (double) diffMs / (1000 * 60 * 60);
            }
        } catch (ParseException ignored) {}
        return 0;
    }

    private void deleteEntry(OTEntry entry) {
        new AlertDialog.Builder(this)
            .setTitle("Delete OT Entry")
            .setMessage("Are you sure you want to delete this overtime entry? This will update your monthly summary.")
            .setPositiveButton("Delete", (dialog, which) -> {
                progressLoading.setVisibility(View.VISIBLE);
                repository.deleteEntry(entry.docId, entry, 
                    aVoid -> {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadMonthData();
                    }, 
                    e -> {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadMonthData() {
        progressLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repository.getEntriesForMonth(currentMonthKey)
            .addOnSuccessListener(queryDocumentSnapshots -> {
                entryList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    OTEntry entry = doc.toObject(OTEntry.class);
                    if (entry != null) {
                        entry.docId = doc.getId();
                        entryList.add(entry);
                    }
                }
                
                // Sort descending locally to avoid requiring a composite index in Firestore
                java.util.Collections.sort(entryList, (a, b) -> Long.compare(b.createdAt, a.createdAt));
                
                adapter.notifyDataSetChanged();
                
                if (entryList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvMonthlyTotal.setText("Rs 0.00");
                    tvMonthlyHours.setText("0.0");
                    progressLoading.setVisibility(View.GONE);
                } else {
                    loadSummary();
                }
            })
            .addOnFailureListener(e -> {
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load entries", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadSummary() {
        repository.getMonthlySummary(currentMonthKey)
            .addOnSuccessListener(documentSnapshot -> {
                progressLoading.setVisibility(View.GONE);
                if (documentSnapshot.exists()) {
                    Double totalPay = documentSnapshot.getDouble("totalOtPay");
                    Double totalHours = documentSnapshot.getDouble("totalOtHours");
                    
                    if (totalPay != null) {
                        tvMonthlyTotal.setText(String.format(Locale.US, "Rs %.2f", totalPay));
                    }
                    if (totalHours != null) {
                        tvMonthlyHours.setText(String.format(Locale.US, "%.1f", totalHours));
                    }
                }
            })
            .addOnFailureListener(e -> progressLoading.setVisibility(View.GONE));
    }
}
