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

            // අවශ්‍ය නම් මෙතනින් හැම check එකක්ම සේව් කරන්න පුළුවන් (දැනට සරලව receiveUpdates එක විතරක් දැම්මා)

            Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show();

            // ආපහු Main Register page එකට යනවා
            finish();
        });
    }

    private void loadExistingData() {
        UserRegistrationData data = UserRegistrationData.getInstance();
        // මෙතනදී data.receiveUpdates අනුව කලින් check කරපු දත්ත load කළ හැකියි
        if (data.receiveUpdates) {
            // උදාහරණයකට මම ඔක්කොම check කරනවා, ඔයාට අවශ්‍ය නම් වෙනම variables Singleton එකට දාලා ගන්නත් පුළුවන්
        }
    }
}