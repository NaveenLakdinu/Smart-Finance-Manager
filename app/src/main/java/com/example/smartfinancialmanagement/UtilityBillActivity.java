package com.example.smartfinancialmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class UtilityBillActivity extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility_bills);

        btnBack = findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> finish());

        // Initialize Stats (Placeholders)
        TextView txtPaidCount = findViewById(R.id.txtPaidCount);
        TextView txtDueCount = findViewById(R.id.txtDueCount);
        
        if (txtPaidCount != null) txtPaidCount.setText("05");
        if (txtDueCount != null) txtDueCount.setText("03");
        
        // RecyclerView setup placeholder
        RecyclerView recyclerBills = findViewById(R.id.recyclerBills);
        // In a real scenario, you'd set an adapter here.
    }
}
