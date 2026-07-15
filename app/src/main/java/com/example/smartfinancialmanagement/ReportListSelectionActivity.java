package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ReportListSelectionActivity extends AppCompatActivity {

    private CardView btnAddUtilityItem;
    private Button btnGetReportFinal;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list_selection);

        btnAddUtilityItem = findViewById(R.id.btnAddUtilityItem);
        btnGetReportFinal = findViewById(R.id.btnGetReportFinal);
        backButton    = findViewById(R.id.backButton);

        // Circular Add button loops user back to Get Report Form screen to add another entry
        btnAddUtilityItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportListSelectionActivity.this, GetReportActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this screen and goes back
            }
        });

        // Generates the final output summary chart sheet view
        btnGetReportFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportListSelectionActivity.this, ReportPanelActivity.class);
                startActivity(intent);
            }
        });
    }
}
