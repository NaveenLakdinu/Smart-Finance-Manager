package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/**
 * PinSetupActivity
 * Allows the user to set a new 4-digit PIN for quick app access.
 * The flow has two stages:
 *   1. Enter new PIN  →  2. Confirm PIN
 * On match the PIN is saved via PinHelper and the user is routed to their dashboard.
 */
public class PinSetupActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private View[] dots;
    private TextView tvStatus;

    // ── State ──────────────────────────────────────────────────────────────
    private final StringBuilder pinBuffer = new StringBuilder();
    private String firstPin = null;            // stores PIN from step 1 for confirmation
    private boolean confirmingPin = false;      // true when we are in confirm step

    private static final int PIN_LENGTH = 4;

    // ──────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        // Dots
        dots = new View[]{
                findViewById(R.id.pinDot1),
                findViewById(R.id.pinDot2),
                findViewById(R.id.pinDot3),
                findViewById(R.id.pinDot4)
        };

        tvStatus = findViewById(R.id.tvPinSetupStatus);

        // Back button
        ImageView btnBack = findViewById(R.id.btnPinSetupBack);
        btnBack.setOnClickListener(v -> finish());

        // Skip link
        TextView tvSkip = findViewById(R.id.tvSkipPin);
        tvSkip.setOnClickListener(v -> {
            // User does not want a PIN – go straight to dashboard
            redirectToDashboard();
        });

        // Wire numpad
        wireNumpad();

        updateDots();
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
            processCompletedPin();
        }
    }

    private void onBackspacePressed() {
        if (pinBuffer.length() > 0) {
            pinBuffer.deleteCharAt(pinBuffer.length() - 1);
            updateDots();
        }
    }

    // ── PIN Logic ──────────────────────────────────────────────────────────
    private void processCompletedPin() {
        String entered = pinBuffer.toString();

        if (!confirmingPin) {
            // Step 1 done – ask for confirmation
            firstPin = entered;
            confirmingPin = true;
            pinBuffer.setLength(0);
            updateDots();
            tvStatus.setText("Confirm your PIN");
            tvStatus.setTextColor(getResources().getColor(R.color.text_on_dark_secondary, null));
        } else {
            // Step 2 – check match
            if (entered.equals(firstPin)) {
                PinHelper.savePin(this, entered);
                Toast.makeText(this, "PIN set successfully! 🔒", Toast.LENGTH_SHORT).show();
                redirectToDashboard();
            } else {
                // Mismatch – restart from step 1
                firstPin = null;
                confirmingPin = false;
                pinBuffer.setLength(0);
                updateDots();
                tvStatus.setText("PINs didn't match. Try again.");
                tvStatus.setTextColor(getResources().getColor(R.color.danger_text, null));
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
        // Default label when buffer is empty and not yet confirming
        if (filled == 0 && !confirmingPin) {
            tvStatus.setText("Enter your new PIN");
            tvStatus.setTextColor(getResources().getColor(R.color.text_on_dark_secondary, null));
        }
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
            case "student_worker_hybrid":
                intent = new Intent(this, StudentWorkerHybridDashboardActivity.class); break;
            default:
                intent = new Intent(this, StudentDashboardActivity.class); break;
        }
        intent.putExtra("CURRENT_USER_ROLE", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
