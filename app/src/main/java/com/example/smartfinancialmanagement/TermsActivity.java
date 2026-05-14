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

        // Views Bind කිරීම
        agreeCheckbox = findViewById(R.id.agreeCheckbox);
        nextButton    = findViewById(R.id.nextButton);
        backButton    = findViewById(R.id.backButton);

        // කලින් Agree වෙලා හිටියා නම් ඒක පෙන්වන්න
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
                // ✅ Singleton එකේ අගය Update කිරීම
                UserRegistrationData.getInstance().isTermsAccepted = true;

                Toast.makeText(this, "Terms Accepted", Toast.LENGTH_SHORT).show();

                // ආපහු Main Register page එකට යනවා
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // User එකඟ නොවී ආපසු ගියොත් checkbox එක uncheck විය යුතු නම්:
        if (!agreeCheckbox.isChecked()) {
            UserRegistrationData.getInstance().isTermsAccepted = false;
        }
    }
}