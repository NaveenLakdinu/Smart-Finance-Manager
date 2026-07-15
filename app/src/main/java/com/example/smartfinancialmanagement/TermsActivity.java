package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    private CheckBox agreeCheckbox;
    private Button nextButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_terms);

        // Views Bind
        agreeCheckbox = findViewById(R.id.agreeCheckbox);
        nextButton    = findViewById(R.id.nextButton);
        backButton    = findViewById(R.id.backButton);


        if (UserRegistrationData.getInstance().isTermsAccepted) {
            agreeCheckbox.setChecked(true);
        }

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Next button
        nextButton.setOnClickListener(v -> {
            if (!agreeCheckbox.isChecked()) {
                Toast.makeText(this,
                        "Please agree to the Terms and Conditions to continue",
                        Toast.LENGTH_SHORT).show();
            } else {
                // ✅ Singleton value Update
                UserRegistrationData.getInstance().isTermsAccepted = true;

                Toast.makeText(this, "Terms Accepted", Toast.LENGTH_SHORT).show();

                // back to Main Register page
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (!agreeCheckbox.isChecked()) {
            UserRegistrationData.getInstance().isTermsAccepted = false;
        }
    }
}