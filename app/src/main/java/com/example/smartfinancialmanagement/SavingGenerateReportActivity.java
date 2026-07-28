package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;
import java.util.Locale;

public class SavingGenerateReportActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RadioGroup rgReportType;
    private RadioButton rbAllTime, rbSelectedMonth, rbCustomDate;
    private LinearLayout layoutMonthSelection, layoutCustomDate;
    private TextView tvSelectedMonth, tvStartDate, tvEndDate;
    private MaterialButton btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_generate_report);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgReportType = findViewById(R.id.rgReportType);
        rbAllTime = findViewById(R.id.rbAllTime);
        rbSelectedMonth = findViewById(R.id.rbSelectedMonth);
        rbCustomDate = findViewById(R.id.rbCustomDate);
        layoutMonthSelection = findViewById(R.id.layoutMonthSelection);
        layoutCustomDate = findViewById(R.id.layoutCustomDate);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnGenerate = findViewById(R.id.btnGenerate);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgReportType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAllTime) {
                layoutMonthSelection.setVisibility(View.GONE);
                layoutCustomDate.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbSelectedMonth) {
                layoutMonthSelection.setVisibility(View.VISIBLE);
                layoutCustomDate.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbCustomDate) {
                layoutMonthSelection.setVisibility(View.GONE);
                layoutCustomDate.setVisibility(View.VISIBLE);
            }
        });

        tvSelectedMonth.setOnClickListener(v -> showDatePicker(tvSelectedMonth, true));
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate, false));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, false));

        btnGenerate.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingReportResultActivity.class);
            if (rbAllTime.isChecked()) {
                intent.putExtra("REPORT_TYPE", "ALL");
            } else if (rbSelectedMonth.isChecked()) {
                String monthYear = tvSelectedMonth.getText().toString();
                if (monthYear.equals("MM/YYYY") || monthYear.isEmpty()) {
                    Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("REPORT_TYPE", "MONTH");
                intent.putExtra("MONTH_YEAR", monthYear);
            } else {
                String start = tvStartDate.getText().toString();
                String end = tvEndDate.getText().toString();
                if (start.equals("Select") || end.equals("Select")) {
                    Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("REPORT_TYPE", "CUSTOM");
                intent.putExtra("START_DATE", start);
                intent.putExtra("END_DATE", end);
            }
            startActivity(intent);
        });
    }

    private void showDatePicker(TextView targetView, boolean isMonthOnly) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, R.style.Theme_SmartFinance_DatePicker,
                (view, year, month, day) -> {
                    String date;
                    if (isMonthOnly) {
                        date = String.format(Locale.getDefault(), "%02d/%d", month + 1, year);
                    } else {
                        date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    }
                    targetView.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
