package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StudentProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_profile) {
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    String role = getSharedPreferences("UserData", MODE_PRIVATE).getString("user_role", "Student");
                    if ("student_worker_hybrid".equals(role)) {
                        startActivity(new Intent(this, StudentWorkerHybridDashboardActivity.class));
                    } else {
                        startActivity(new Intent(this, StudentDashboardActivity.class));
                    }
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_budget) {
                    startActivity(new Intent(this, StudentBudgetActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_savings) {
                    startActivity(new Intent(this, StudentSavingActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_loans) {
                    startActivity(new Intent(this, StudentLoansActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }

        // Setup Upgrade and Logout cards
        View cardUpgradeHybrid = findViewById(R.id.cardUpgradeHybrid);
        if (cardUpgradeHybrid != null) {
            cardUpgradeHybrid.setOnClickListener(v -> {
                startActivity(new Intent(this, RoleUpgradeActivity.class));
            });
        }

        View cardSignOut = findViewById(R.id.cardSignOut);
        if (cardSignOut != null) {
            cardSignOut.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
