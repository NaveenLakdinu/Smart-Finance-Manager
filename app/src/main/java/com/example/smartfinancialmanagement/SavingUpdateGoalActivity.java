package com.example.smartfinancialmanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SavingUpdateGoalActivity extends AppCompatActivity {

    private EditText etGoalName, etTargetAmount, etCurrentAmount;
    private TextView tvStartDate, tvTargetDate, tvDuration, tvMonthlyRequirement, tvProgress;
    private ImageView btnBack;
    private MaterialButton btnCancel, btnUpdate;

    private CollectionReference databaseReference;
    private String userId;
    private String savingId;
    private SimpleDateFormat dateFormat;
    private long createdAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_update_goal);

        savingId = getIntent().getStringExtra("SAVING_ID");

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupFirebase();
        setupListeners();
        loadExistingData();
    }

    private void initViews() {
        etGoalName = findViewById(R.id.etGoalName);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etCurrentAmount = findViewById(R.id.etCurrentAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvTargetDate = findViewById(R.id.tvTargetDate);
        tvDuration = findViewById(R.id.tvDuration);
        tvMonthlyRequirement = findViewById(R.id.tvMonthlyRequirement);
        tvProgress = findViewById(R.id.tvProgress);
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "test_user";
        }
        databaseReference = FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings");
    }

    private void loadExistingData() {
        if (savingId == null) return;

        databaseReference.document(savingId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                SavingModel saving = task.getResult().toObject(SavingModel.class);
                if (saving != null) {
                    etGoalName.setText(saving.getSavingTitle());
                    etTargetAmount.setText(String.valueOf(saving.getTargetAmount()));
                    etCurrentAmount.setText(String.valueOf(saving.getCurrentAmount()));
                    tvStartDate.setText(saving.getStartDate());
                    tvTargetDate.setText(saving.getTargetDate());
                    createdAt = saving.getCreatedAt();
                    calculateRequirement();
                }
            } else {
                Toast.makeText(SavingUpdateGoalActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> navigateToManager());
        btnCancel.setOnClickListener(v -> navigateToManager());

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvTargetDate.setOnClickListener(v -> showDatePicker(tvTargetDate));

        TextWatcher calculationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRequirement();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTargetAmount.addTextChangedListener(calculationWatcher);
        etCurrentAmount.addTextChangedListener(calculationWatcher);
        tvStartDate.addTextChangedListener(calculationWatcher);
        tvTargetDate.addTextChangedListener(calculationWatcher);

        btnUpdate.setOnClickListener(v -> updateGoal());
    }

    private void navigateToManager() {
        Intent intent = new Intent(this, SavingManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    targetView.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void calculateRequirement() {
        String targetStr = etTargetAmount.getText().toString().trim();
        String currentStr = etCurrentAmount.getText().toString().trim();
        String startStr = tvStartDate.getText().toString().trim();
        String endStr = tvTargetDate.getText().toString().trim();

        if (targetStr.isEmpty() || startStr.equals("Select") || endStr.equals("Select") || startStr.isEmpty() || endStr.isEmpty()) {
            return;
        }

        try {
            double target = Double.parseDouble(targetStr);
            double current = currentStr.isEmpty() ? 0 : Double.parseDouble(currentStr);

            int progress = target > 0 ? (int) ((current / target) * 100) : 0;
            if (progress > 100) progress = 100;
            tvProgress.setText(progress + "%");

            Date startDate = dateFormat.parse(startStr);
            Date endDate = dateFormat.parse(endStr);

            if (startDate != null && endDate != null) {
                long diffInMillis = endDate.getTime() - startDate.getTime();
                long diffInMonths = diffInMillis / (1000L * 60 * 60 * 24 * 30);
                if (diffInMonths <= 0) diffInMonths = 1;

                tvDuration.setText(diffInMonths + " Months");

                double required = (target - current) / diffInMonths;
                if (required < 0) required = 0;

                tvMonthlyRequirement.setText(String.format(Locale.getDefault(), "$%.2f", required));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGoal() {
        String name = etGoalName.getText().toString().trim();
        String targetStr = etTargetAmount.getText().toString().trim();
        String currentStr = etCurrentAmount.getText().toString().trim();
        String startStr = tvStartDate.getText().toString().trim();
        String endStr = tvTargetDate.getText().toString().trim();

        if (name.isEmpty() || targetStr.isEmpty() || startStr.equals("Select") || endStr.equals("Select") || savingId == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double target = Double.parseDouble(targetStr);
            double current = currentStr.isEmpty() ? 0 : Double.parseDouble(currentStr);

            if (target <= current) {
                Toast.makeText(this, "Target must be greater than current amount", Toast.LENGTH_SHORT).show();
                return;
            }

            Date startDate = dateFormat.parse(startStr);
            Date endDate = dateFormat.parse(endStr);

            if (endDate != null && startDate != null && !endDate.after(startDate)) {
                Toast.makeText(this, "Target date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            long diffInMillis = endDate.getTime() - startDate.getTime();
            long diffInMonths = diffInMillis / (1000L * 60 * 60 * 24 * 30);
            if (diffInMonths <= 0) diffInMonths = 1;
            double monthlyRequirement = (target - current) / diffInMonths;

            String status = current >= target ? "Completed" : "Active";

            SavingModel savingModel = new SavingModel(savingId, name, target, current, monthlyRequirement, 
                    startStr, endStr, status, createdAt);

            databaseReference.document(savingId).set(savingModel).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Goal Updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update goal", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}
