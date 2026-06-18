// MainActivity.java
// ══════════════════════════════════════════════════
// 
// ══════════════════════════════════════════════════

package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            redirectLoggedInUser();
            return;
        }

        // Make the activity full screen (no status bar)
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        // Button click listeners
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginFormActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChooseRoleActivity.class);
            startActivity(intent);
        });
    }

    private void redirectLoggedInUser() {
        String role = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("user_role", "Student");

        Intent intent;
        switch (role) {
            case "Company worker":
                intent = new Intent(MainActivity.this, WorkerDashboardActivity.class);
                break;
            case "Multiple account holder":
                intent = new Intent(MainActivity.this, MultiAccountDashboardActivity.class);
                break;
            case "Business owner":
                intent = new Intent(MainActivity.this, BusinessDashboardActivity.class);
                break;
            case "Student":
            default:
                intent = new Intent(MainActivity.this, DashboardActivity.class);
                break;
        }

        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}