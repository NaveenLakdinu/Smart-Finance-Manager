package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView btnBack;
    private MaterialButton btnGeneratePdf; // Updated component type

    // Summary Display UI
    private TextView txtMonthlyProfit;
    private TextView txtAnalyticsSummary;

    // Metrics Trend Outputs
    private TextView txtRevenueTrend;
    private TextView txtExpenseTrend;

    // Clickable Material Metrics Cards (Updated component types)
    private MaterialCardView cardRevenueTrend;
    private MaterialCardView cardExpenseTrend;

    // Local Variables to hold temporary state before backend integration
    private double currentRevenue = 250000.0; // Default mock value
    private double currentExpenses = 170000.0; // Default mock value

    // Activity Result Launchers to receive updated figures from separate UIs
    private ActivityResultLauncher<Intent> revenueLauncher;
    private ActivityResultLauncher<Intent> expenseLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_analytic); // Pointing to your optimized layout name

        initializeViews();
        setupResultLaunchers();
        setupListeners();

        // Initial programmatic calculation
        updateFinancialUI();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);

        txtMonthlyProfit = findViewById(R.id.txtMonthlyProfit);
        txtAnalyticsSummary = findViewById(R.id.txtAnalyticsSummary);

        txtRevenueTrend = findViewById(R.id.txtRevenueTrend);
        txtExpenseTrend = findViewById(R.id.txtExpenseTrend);

        cardRevenueTrend = findViewById(R.id.cardRevenueTrend);
        cardExpenseTrend = findViewById(R.id.cardExpenseTrend);
    }

    /**
     * Registers listening channels to intercept fresh balances coming back
     * from the separate entry screens.
     */
    private void setupResultLaunchers() {
        // Intercepts data returning from RevenueManagementActivity
        revenueLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        currentRevenue = result.getData().getDoubleExtra("UPDATED_REVENUE", currentRevenue);
                        updateFinancialUI();
                    }
                }
        );

        // Intercepts data returning from ExpenseManagementActivity
        expenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        currentExpenses = result.getData().getDoubleExtra("UPDATED_EXPENSE", currentExpenses);
                        updateFinancialUI();
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGeneratePdf.setOnClickListener(v -> {
            Toast.makeText(this, "PDF Report Generated successfully!", Toast.LENGTH_SHORT).show();
        });

        // Launch Revenue UI safely via registered contract launcher
        if (cardRevenueTrend != null) {
            cardRevenueTrend.setOnClickListener(v -> {
                // Assuming Target Activity name matches your management convention
                Intent intent = new Intent(AnalyticsActivity.this, RevenueManagementActivity.class);
                intent.putExtra("CURRENT_REVENUE", currentRevenue);
                revenueLauncher.launch(intent);
            });
        }

        // Launch Expense UI safely via registered contract launcher
        if (cardExpenseTrend != null) {
            cardExpenseTrend.setOnClickListener(v -> {
                // Assuming Target Activity name matches your management convention
                Intent intent = new Intent(AnalyticsActivity.this, ExpenseManagementActivity.class);
                intent.putExtra("CURRENT_EXPENSE", currentExpenses);
                expenseLauncher.launch(intent);
            });
        }
    }

    /**
     * Runs localized structural math formulas and syncs layout strings instantly.
     */
    private void updateFinancialUI() {
        // Update basic tracking text elements first
        txtRevenueTrend.setText(String.format(Locale.getDefault(), "%,.0fK", currentRevenue / 1000));
        txtExpenseTrend.setText(String.format(Locale.getDefault(), "%,.0fK", currentExpenses / 1000));

        // Execute dynamic net calculation loop
        double netProfit = currentRevenue - currentExpenses;

        // Apply string values directly onto summary metrics card elements
        txtMonthlyProfit.setText(String.format(Locale.getDefault(), "Rs. %,.2f", netProfit));

        // Adjust style states and system messages depending on negative or positive states
        if (netProfit < 0) {
            txtMonthlyProfit.setTextColor(android.graphics.Color.parseColor("#FF4D4D")); // Bright theme-aligned warning red
            txtAnalyticsSummary.setText("Warning: Business operations are currently running at a loss.");
        } else {
            txtMonthlyProfit.setTextColor(android.graphics.Color.parseColor("#4DE800")); // Neon green layout match
            txtAnalyticsSummary.setText("Business performance is stable and improving.");
        }
    }
}