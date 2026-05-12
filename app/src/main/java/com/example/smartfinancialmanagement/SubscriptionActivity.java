// SubscriptionActivity.java
package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SubscriptionActivity extends AppCompatActivity {

    CheckBox checkEmail, checkSms, checkPush, checkReport, checkPromo;
    MaterialButton nextButton;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        checkEmail  = findViewById(R.id.checkEmail);
        checkSms    = findViewById(R.id.checkSms);
        checkPush   = findViewById(R.id.checkPush);
        checkReport = findViewById(R.id.checkReport);
        checkPromo  = findViewById(R.id.checkPromo);
        nextButton  = findViewById(R.id.nextButton);
        backButton  = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> onBackPressed());

        nextButton.setOnClickListener(v -> {
            // අවම වශයෙන් 1ක් select කළ යුතුයි
            if (!checkEmail.isChecked() && !checkSms.isChecked()
                    && !checkPush.isChecked() && !checkReport.isChecked()
                    && !checkPromo.isChecked()) {
                Toast.makeText(this,
                        "Please select at least one notification type",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // ✅ Next page navigate කරන්න
            Intent intent = new Intent(SubscriptionActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}