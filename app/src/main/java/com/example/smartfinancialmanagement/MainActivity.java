// MainActivity.java
// Entry point: routes user based on login + PIN status.

package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (PinHelper.isPinSet(this)) {
                Intent intent = new Intent(this, PinLockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, PinSetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            return;
        }

        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation scaleFadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideUpFromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_up_from_bottom);
        Animation expandWidth = AnimationUtils.loadAnimation(this, R.anim.expand_width);
        Animation pulseRepeat = AnimationUtils.loadAnimation(this, R.anim.pulse_repeat);

        // Logo: scale + fade in
        View logoContainer = findViewById(R.id.logoContainer);
        logoContainer.setVisibility(View.VISIBLE);
        logoContainer.startAnimation(scaleFadeIn);

        // Brand label: slide up with delay
        TextView brandLabel = findViewById(R.id.brandLabel);
        if (brandLabel != null) {
            slideUp.setStartOffset(200);
            brandLabel.startAnimation(slideUp);
        }

        // Divider line: expand with delay
        View dividerLine = findViewById(R.id.dividerLine);
        if (dividerLine != null) {
            expandWidth.setStartOffset(400);
            dividerLine.startAnimation(expandWidth);
        }

        // Bottom panel: slide up from bottom
        View bottomPanel = findViewById(R.id.bottomPanel);
        bottomPanel.setVisibility(View.VISIBLE);
        slideUpFromBottom.setStartOffset(300);
        bottomPanel.startAnimation(slideUpFromBottom);

        // Login button: pulse after appearing
        View loginButton = findViewById(R.id.loginButton);
        pulseRepeat.setStartOffset(1200);
        loginButton.startAnimation(pulseRepeat);

        // Sign up button: fade in with delay
        View signUpButton = findViewById(R.id.signUpButton);
        fadeIn.setStartOffset(800);
        signUpButton.startAnimation(fadeIn);

        // Set click listeners
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginFormActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChooseRoleActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
