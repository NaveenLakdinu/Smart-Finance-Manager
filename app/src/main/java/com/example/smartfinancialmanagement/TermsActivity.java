package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    CheckBox agreeCheckbox;
    Button nextButton;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        agreeCheckbox = findViewById(R.id.agreeCheckbox);
        nextButton    = findViewById(R.id.nextButton);
        backButton    = findViewById(R.id.backButton);

        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Next button
        nextButton.setOnClickListener(v -> {
            if (!agreeCheckbox.isChecked()) {
                Toast.makeText(this,
                        "Please agree to the Terms and Conditions to continue",
                        Toast.LENGTH_SHORT).show();
            } else {

                Intent intent = new Intent(TermsActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}