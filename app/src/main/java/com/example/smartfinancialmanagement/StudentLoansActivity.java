package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentLoansActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_loans);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_loans);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_loans) {
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
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, StudentProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
}
