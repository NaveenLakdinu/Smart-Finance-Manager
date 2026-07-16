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

    // 💡 වර්තමාන තිරයේ තෝරාගෙන ඇති බිල්පත් ලැයිස්තුව මතක තබා ගන්නා ArrayList එක
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

        // ReportListSelectionActivity.java හි btnGetReportFinal ක්ලික් ලිස්නර් එක මෙලෙස වෙනස් කරන්න:
        btnGetReportFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBillsList == null || selectedBillsList.isEmpty()) {
                    Toast.makeText(ReportListSelectionActivity.this, "Please add at least one bill entry first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 💡 DataBridge වෙනුවට කෙලින්ම Intent එකට ලැයිස්තුව ඇතුළත් කිරීම
                Intent intent = new Intent(ReportListSelectionActivity.this, UtilityReportActivity.class);
                intent.putExtra("FINAL_REPORT_ITEMS", selectedBillsList);
                startActivity(intent);
            }
        });
    }

    // 💡 ක්‍රමෝපායික එකතු කිරීමක්:
    // GetReportActivity එකෙන් බිල්පතක් සාදා intent.putExtra() හරහා දත්ත ලැබෙන්නේ නම්,
    // එය මෙමonResume() හෝ onNewIntent() මඟින් selectedBillsList එකට එකතු කරගත හැක.
}