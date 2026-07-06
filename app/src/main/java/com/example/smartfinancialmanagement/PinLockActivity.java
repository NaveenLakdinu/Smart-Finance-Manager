package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/**
 * PinLockActivity
 * Displayed when the app is opened and the user already has a PIN set.
 * The user must enter their correct 4-digit PIN to proceed to the dashboard.
 * "Forgot PIN?" clears the stored PIN and routes to normal email/password login.
 */
public class PinLockActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private View[] dots;
    private TextView tvStatus;
    private LinearLayout dotsContainer;

    // ── State ──────────────────────────────────────────────────────────────
    private final StringBuilder pinBuffer = new StringBuilder();
    private int wrongAttempts = 0;

    private static final int PIN_LENGTH  = 4;
    private static final int MAX_ATTEMPTS = 5;

    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        // Dots
        dots = new View[]{
                findViewById(R.id.pinDot1),
                findViewById(R.id.pinDot2),
                findViewById(R.id.pinDot3),
                findViewById(R.id.pinDot4)
        };

        tvStatus       = findViewById(R.id.tvPinLockStatus);
        dotsContainer  = findViewById(R.id.pinDotsRow);   // parent LinearLayout of the dots

        wireNumpad();

        // "Forgot PIN?" → clears PIN and falls back to normal login
        TextView tvForgot = findViewById(R.id.tvForgotPin);
        tvForgot.setOnClickListener(v -> forgotPin());
    }

    // ── Numpad wiring ──────────────────────────────────────────────────────
    private void wireNumpad() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9
        };
        String[] digits = {"0","1","2","3","4","5","6","7","8","9"};

        for (int i = 0; i < buttonIds.length; i++) {
            final String digit = digits[i];
            MaterialButton btn = findViewById(buttonIds[i]);
            btn.setOnClickListener(v -> onDigitPressed(digit));
        }

        MaterialButton btnBackspace = findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(v -> onBackspacePressed());
    }

    // ── Input handlers ─────────────────────────────────────────────────────
    private void onDigitPressed(String digit) {
        if (pinBuffer.length() >= PIN_LENGTH) return;
        pinBuffer.append(digit);
        updateDots();

        if (pinBuffer.length() == PIN_LENGTH) {
            verifyPin();
        }
    }

    private void onBackspacePressed() {
        if (pinBuffer.length() > 0) {
            pinBuffer.deleteCharAt(pinBuffer.length() - 1);
            updateDots();
        }
    }

    // ── PIN verification ───────────────────────────────────────────────────
    private void verifyPin() {
        String entered = pinBuffer.toString();

        if (PinHelper.verifyPin(this, entered)) {
            // ✅ Correct PIN
            redirectToDashboard();
        } else {
            // ❌ Wrong PIN
            wrongAttempts++;
            pinBuffer.setLength(0);
            updateDots();

            if (wrongAttempts >= MAX_ATTEMPTS) {
                // Too many failures – clear PIN and force password login
                tvStatus.setText("Too many attempts. PIN removed.");
                PinHelper.clearPin(this);
                // Short delay then go to login
                tvStatus.postDelayed(this::goToLogin, 1500);
            } else {
                int remaining = MAX_ATTEMPTS - wrongAttempts;
                tvStatus.setText("Wrong PIN – " + remaining + " attempt(s) left");
                shakeDotsRow();
            }
        }
    }

    // ── Dot indicator ─────────────────────────────────────────────────────
    private void updateDots() {
        int filled = pinBuffer.length();
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i < filled ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty
            );
        }
    }

    /** Small horizontal shake animation when PIN is wrong. */
    private void shakeDotsRow() {
        if (dotsContainer == null) return;
        // Programmatic translate shake (no anim XML needed)
        dotsContainer.animate()
                .translationX(16f)
                .setDuration(60)
                .withEndAction(() -> dotsContainer.animate()
                        .translationX(-16f)
                        .setDuration(60)
                        .withEndAction(() -> dotsContainer.animate()
                                .translationX(12f)
                                .setDuration(50)
                                .withEndAction(() -> dotsContainer.animate()
                                        .translationX(0f)
                                        .setDuration(50)
                                        .start())
                                .start())
                        .start())
                .start();
    }

    // ── Navigation ────────────────────────────────────────────────────────
    private void redirectToDashboard() {
        String role = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("user_role", "Student");

        Intent intent;
        switch (role) {
            case "Company worker":
                intent = new Intent(this, WorkerDashboardActivity.class); break;
            case "Multiple account holder":
                intent = new Intent(this, MultiAccountDashboardActivity.class); break;
            case "Business owner":
                intent = new Intent(this, BusinessDashboardActivity.class); break;
            default:
                intent = new Intent(this, StudentDashboardActivity.class); break;
        }
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void forgotPin() {
        // Clear the stored PIN so next launch routes to normal login
        PinHelper.clearPin(this);
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginFormActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /** Prevent back-press from bypassing the lock. */
    @Override
    public void onBackPressed() {
        // Do nothing – user must unlock or use forgot-PIN
    }
}
