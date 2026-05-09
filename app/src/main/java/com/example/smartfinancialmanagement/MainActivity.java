// MainActivity.java
// ══════════════════════════════════════════════════
// 
// ══════════════════════════════════════════════════

package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            // Navigate to sign up functionality
        });
    }
}