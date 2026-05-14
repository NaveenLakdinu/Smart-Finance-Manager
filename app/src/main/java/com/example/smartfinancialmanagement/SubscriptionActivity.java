package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SubscriptionActivity extends AppCompatActivity {

    private CheckBox checkEmail, checkSms, checkPush, checkReport, checkPromo;
    private MaterialButton nextButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_subscription);

        // 1. Initialize Views
        checkEmail  = findViewById(R.id.checkEmail);
        checkSms    = findViewById(R.id.checkSms);
        checkPush   = findViewById(R.id.checkPush);
        checkReport = findViewById(R.id.checkReport);
        checkPromo  = findViewById(R.id.checkPromo);
        nextButton  = findViewById(R.id.nextButton);
        backButton  = findViewById(R.id.backButton);

        // 2. Load existing data (ආයෙත් ආවොත් කලින් ටික් කරපුවා පෙන්වන්න)
        loadExistingData();

        // 3. Back Button
        backButton.setOnClickListener(v -> finish());

        // 4. Next Button
        nextButton.setOnClickListener(v -> {
            // අවම වශයෙන් 1ක් select කළ යුතුයි (Validation)
            if (!checkEmail.isChecked() && !checkSms.isChecked()
                    && !checkPush.isChecked() && !checkReport.isChecked()
                    && !checkPromo.isChecked()) {
                Toast.makeText(this,
                        "Please select at least one notification type",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Singleton එකට දත්ත සේව් කිරීම
            UserRegistrationData data = UserRegistrationData.getInstance();
            data.receiveUpdates = true; // Main Page එකේ checkbox එක ටික් වෙන්න
            data.checkEmail = checkEmail.isChecked();
            data.checkSms = checkSms.isChecked();
            data.checkPush = checkPush.isChecked();
            data.checkReport = checkReport.isChecked();
            data.checkPromo = checkPromo.isChecked();

            Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show();

            // ආපහු Main Register page එකට යනවා
            finish();
        });
    }

    private void loadExistingData() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        // Restore checkbox states from singleton
        checkEmail.setChecked(data.checkEmail);
        checkSms.setChecked(data.checkSms);
        checkPush.setChecked(data.checkPush);
        checkReport.setChecked(data.checkReport);
        checkPromo.setChecked(data.checkPromo);
    }
}