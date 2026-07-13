package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;

public class SavingDetailsActivity extends AppCompatActivity {

    private TextView tvGoalName, tvStatus, tvProgress, tvCurrentAmount;
    private TextView tvTargetAmount, tvRemainingAmount, tvMonthlyAmount, tvStartDate, tvTargetDate;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private MaterialButton btnEdit, btnDelete;

    private DatabaseReference databaseReference;
    private String userId;
    private String savingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_details);

        savingId = getIntent().getStringExtra("SAVING_ID");

        initViews();
        setupFirebase();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        tvGoalName = findViewById(R.id.tvGoalName);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        tvCurrentAmount = findViewById(R.id.tvCurrentAmount);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        tvMonthlyAmount = findViewById(R.id.tvMonthlyAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvTargetDate = findViewById(R.id.tvTargetDate);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "test_user";
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Savings").child(userId);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingUpdateGoalActivity.class);
            intent.putExtra("SAVING_ID", savingId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void loadData() {
        if (savingId == null) return;

        databaseReference.child(savingId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                
                SavingModel saving = snapshot.getValue(SavingModel.class);
                if (saving != null) {
                    tvGoalName.setText(saving.getSavingTitle());
                    
                    String status = saving.getStatus() != null ? saving.getStatus() : "Active";
                    tvStatus.setText(status);
                    
                    tvCurrentAmount.setText(String.format(Locale.getDefault(), "$%.2f", saving.getCurrentAmount()));
                    tvTargetAmount.setText(String.format(Locale.getDefault(), "$%.2f", saving.getTargetAmount()));
                    
                    double remaining = saving.getTargetAmount() - saving.getCurrentAmount();
                    if (remaining < 0) remaining = 0;
                    tvRemainingAmount.setText(String.format(Locale.getDefault(), "$%.2f", remaining));
                    
                    tvMonthlyAmount.setText(String.format(Locale.getDefault(), "$%.2f", saving.getMonthlySavingAmount()));
                    tvStartDate.setText(saving.getStartDate());
                    tvTargetDate.setText(saving.getTargetDate());

                    int progress = saving.getTargetAmount() > 0 ? 
                            (int) ((saving.getCurrentAmount() / saving.getTargetAmount()) * 100) : 0;
                    if (progress > 100) progress = 100;
                    
                    tvProgress.setText(progress + "%");
                    progressBar.setProgress(progress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavingDetailsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this saving goal?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSavingGoal())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSavingGoal() {
        databaseReference.child(savingId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Saving goal deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
