package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class WorkerDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtEarnings, txtPayrollStatus;
    private MaterialCardView cardWorkTasks, cardExpenseClaims, cardPayslips, cardLoanManager;
    private MaterialCardView cardSubscriptionManager, cardSavingManager, cardUtilityManager;
    private android.view.View btnTopLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        initViews();
        setupUserDetails();
        setupClickListeners();
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtEarnings = findViewById(R.id.txtEarnings);
        txtPayrollStatus = findViewById(R.id.txtPayrollStatus);

        cardWorkTasks = findViewById(R.id.cardWorkTasks);
        cardExpenseClaims = findViewById(R.id.cardExpenseClaims);
        cardPayslips = findViewById(R.id.cardPayslips);
        cardLoanManager = findViewById(R.id.cardLoanManager);
        cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardUtilityManager = findViewById(R.id.cardUtilityManager);
        
        btnTopLogout = findViewById(R.id.btnTopLogout);
    }

    private void setupUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        txtGreeting.setText(getGreetingText());

        if (user != null) {
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                txtUserEmail.setText(email);
                txtProfileLetter.setText(String.valueOf(email.charAt(0)).toUpperCase());
            }
        }
        
        // Mock data for earnings
        txtEarnings.setText("LKR 75,000.00");
        txtPayrollStatus.setText("Next payday: June 30th");
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "Good Morning, Worker 👋";
        else if (hour >= 12 && hour < 17) return "Good Afternoon, Worker ☀️";
        else if (hour >= 17 && hour < 21) return "Good Evening, Worker 🌙";
        else return "Good Night, Worker ✨";
    }

    private void setupClickListeners() {
        cardWorkTasks.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkerTasksActivity.class);
            startActivity(intent);
        });
        cardExpenseClaims.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimsActivity.class);
            startActivity(intent);
        });
        cardPayslips.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkerPayslipActivity.class);
            startActivity(intent);
        });
        cardLoanManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanFormActivity.class);
            startActivity(intent);
        });
        cardSubscriptionManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingManagerActivity.class);
            startActivity(intent);
        });
        cardUtilityManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, UtilityManagerActivity.class);
            startActivity(intent);
        });

        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}