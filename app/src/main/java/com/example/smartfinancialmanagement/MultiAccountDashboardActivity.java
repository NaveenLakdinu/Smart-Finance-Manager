package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MultiAccountDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtCurrentAccountName, txtAccountBalance, txtAccountNumber;
    private LinearLayout btnSwitchAccount;
    private MaterialButton btnLogout;

    // Simulated account data
    private String[] accounts = {"Personal Account", "Business Account", "Family Savings"};
    private String[] balances = {"LKR 125,400.00", "LKR 2,450,000.00", "LKR 45,000.00"};
    private String[] accountNumbers = {"**** **** 4290", "**** **** 8812", "**** **** 1029"};
    private int currentAccountIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_account_dashboard);

        initViews();
        setupUserDetails();
        updateAccountUI();
        
        btnSwitchAccount.setOnClickListener(v -> showAccountSwitchDialog());
        
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtCurrentAccountName = findViewById(R.id.txtCurrentAccountName);
        txtAccountBalance = findViewById(R.id.txtAccountBalance);
        txtAccountNumber = findViewById(R.id.txtAccountNumber);
        btnSwitchAccount = findViewById(R.id.btnSwitchAccount);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        txtGreeting.setText(getGreetingText());

        if (user != null && user.getEmail() != null) {
            txtProfileLetter.setText(String.valueOf(user.getEmail().charAt(0)).toUpperCase());
        }
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good Morning 👋";
        else if (hour >= 12 && hour < 17) return "Good Afternoon ☀️";
        else if (hour >= 17 && hour < 21) return "Good Evening 🌙";
        else return "Good Night ✨";
    }

    private void updateAccountUI() {
        txtCurrentAccountName.setText(accounts[currentAccountIndex]);
        txtAccountBalance.setText(balances[currentAccountIndex]);
        txtAccountNumber.setText(accountNumbers[currentAccountIndex]);
    }

    private void showAccountSwitchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Switch Account");
        builder.setItems(accounts, (dialog, which) -> {
            currentAccountIndex = which;
            updateAccountUI();
            Toast.makeText(this, "Switched to " + accounts[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}