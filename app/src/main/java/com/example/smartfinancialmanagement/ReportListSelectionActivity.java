package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList; // 💡 ArrayList භාවිතා කිරීම සඳහා අවශ්‍ය Import එක

public class ReportListSelectionActivity extends AppCompatActivity {

    private CardView btnAddUtilityItem;
    private Button btnGetReportFinal;
    private ImageView backButton;

    private ArrayList<BillReportItem> selectedBillsList = new ArrayList<>();

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
                Intent intent = new Intent(ReportListSelectionActivity.this, UtilityReportFormActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this screen and goes back
            }
        });

        btnGetReportFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBillsList == null || selectedBillsList.isEmpty()) {
                    Toast.makeText(ReportListSelectionActivity.this, "Please add at least one bill entry first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ReportListSelectionActivity.this, UtilityReportActivity.class);
                intent.putExtra("FINAL_REPORT_ITEMS", selectedBillsList);
                startActivity(intent);
            }
        });
    }
}