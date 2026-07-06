// MainActivity.java
// ══════════════════════════════════════════════════
// Entry point: routes user based on login + PIN status.
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // ── User is already authenticated ─────────────────────────────
            if (PinHelper.isPinSet(this)) {
                // User has a PIN → show PIN lock screen
                Intent intent = new Intent(this, PinLockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Logged in but no PIN set yet → offer PIN setup first
                Intent intent = new Intent(this, PinSetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            return;
        }

        // ── User is NOT logged in → show welcome / login screen ───────────
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        findViewById(R.id.loginButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginFormActivity.class));
        });

        findViewById(R.id.signUpButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChooseRoleActivity.class));
        });
    }
}