package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class SavingManagerActivity extends AppCompatActivity {

    private View btnAddGoal, btnViewGoals, btnSavingReport;
    private ImageView backButton;
    private TextView txtTotalSavings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_manager_function);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnViewGoals = findViewById(R.id.btnViewGoals);
        btnSavingReport = findViewById(R.id.btnSavingReport);
        backButton = findViewById(R.id.backButton);
        txtTotalSavings = findViewById(R.id.txtTotalSavings);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingPlanActivity.class);
            startActivity(intent);
        });

        btnViewGoals.setOnClickListener(v -> 
            Toast.makeText(this, "View All Goals - Coming Soon", Toast.LENGTH_SHORT).show()
        );

        btnSavingReport.setOnClickListener(v -> 
            Toast.makeText(this, "Generating Saving Report...", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadSavingsData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || txtTotalSavings == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentSavings = documentSnapshot.getString("currentSavings");
                        String targetAmt = documentSnapshot.getString("savingTargetAmount");
                        
                        double current = 0.0;
                        double target = 0.0;
                        
                        try {
                            if (currentSavings != null && !currentSavings.trim().isEmpty()) {
                                current = Double.parseDouble(currentSavings.trim());
                            }
                        } catch (NumberFormatException ignored) {}

                        try {
                            if (targetAmt != null && !targetAmt.trim().isEmpty()) {
                                target = Double.parseDouble(targetAmt.trim());
                            }
                        } catch (NumberFormatException ignored) {}

                        if (target > 0) {
                            txtTotalSavings.setText(String.format(Locale.US, "LKR %.2f / %.2f", current, target));
                        } else {
                            txtTotalSavings.setText(String.format(Locale.US, "LKR %.2f", current));
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavingsData();
    }
}