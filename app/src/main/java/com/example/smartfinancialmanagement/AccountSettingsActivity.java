package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountSettingsActivity extends AppCompatActivity {

    private Spinner spinnerCurrency;
    private EditText etNewPassword, etConfirmPassword;
    private MaterialButton btnSaveSettings;
    
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private final String[] currencies = {"LKR", "$", "€", "£", "₹"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        setupCurrencySpinner();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        String currentCurrency = CurrencyHelper.getCurrencySymbol(this);
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currentCurrency)) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }
    }

    private void saveSettings() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        String selectedCurrency = spinnerCurrency.getSelectedItem().toString();

        // 1. Save Currency
        CurrencyHelper.setCurrencySymbol(this, selectedCurrency);

        // 2. Update Password (if provided)
        if (!newPass.isEmpty()) {
            if (newPass.equals(confirmPass)) {
                if (user != null) {
                    user.updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Settings and Password updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            } else {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }
        } else {
            Toast.makeText(this, "Settings updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
