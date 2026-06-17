package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class StudentDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        initViews();
    }

    private void initViews() {
        View btnTopLogout = findViewById(R.id.btnTopLogout);
        if (btnTopLogout != null) {
            btnTopLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Core Manager Cards
        View cardLoanManager = findViewById(R.id.cardLoanManager);
        View cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        View cardSavingManager = findViewById(R.id.cardSavingManager);
        View cardUtilityManager = findViewById(R.id.cardUtilityManager);

        if (cardLoanManager != null) {
            cardLoanManager.setOnClickListener(v -> startActivity(new Intent(this, LoanFormActivity.class)));
        }
        if (cardSubscriptionManager != null) {
            cardSubscriptionManager.setOnClickListener(v -> startActivity(new Intent(this, SubscriptionManagerActivity.class)));
        }
        if (cardSavingManager != null) {
            cardSavingManager.setOnClickListener(v -> startActivity(new Intent(this, SavingManagerActivity.class)));
        }
        if (cardUtilityManager != null) {
            cardUtilityManager.setOnClickListener(v -> startActivity(new Intent(this, UtilityManagerActivity.class)));
        }
    }
}
