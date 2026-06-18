package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WorkerPayslipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_payslip);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnDownload).setOnClickListener(v -> 
            Toast.makeText(this, "Downloading Payslip...", Toast.LENGTH_SHORT).show()
        );
    }
}