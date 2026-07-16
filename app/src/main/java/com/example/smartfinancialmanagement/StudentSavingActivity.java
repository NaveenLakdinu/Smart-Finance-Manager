package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class StudentSavingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Redirect to the new Jetpack Compose SavingsPassportActivity
        startActivity(new Intent(this, SavingsPassportActivity.class));
        finish();
    }
}
