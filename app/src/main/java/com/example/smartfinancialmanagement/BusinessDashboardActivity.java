package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

public class BusinessDashboardActivity extends AppCompatActivity {

    // Top Header / Profile Info
    private TextView txtProfileLetter;
    private TextView txtGreeting;
    private TextView txtUserEmail;
    private TextView btnNotifications;
    private View btnTopLogout;

    // Summary State Fields
    private TextView txtTotalCount;
    private TextView txtSubMessage;

    // Feature Modules Grid
    private MaterialCardView cardManageLoan;
    private MaterialCardView cardManageSubscription;
    private MaterialCardView cardManageUtility;
    private MaterialCardView cardSavingManager;
    private MaterialCardView cardB2BInvoice;
    private MaterialCardView cardAnalytics;

    // Bottom Feed Lists
    private View recentSection;
    private TextView txtViewAll;
    private RecyclerView SampleRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_owner_dashboard);

        initializeViews();
        setupClickListeners();
        configureRecentListsFeed();
    }

    /**
     * Maps and initializes layout components safely.
     */
    private void initializeViews() {
        // Header Binds
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        // Core Balance Summary Info
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);

        // Grid Actions
        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardB2BInvoice = findViewById(R.id.B2BInvoice);
        cardAnalytics = findViewById(R.id.cardAnalytics);

        // Secondary List Containers
        recentSection = findViewById(R.id.recentSection);
        txtViewAll = findViewById(R.id.txtViewAll);
        SampleRecycler = findViewById(R.id.recyclerRecent);
    }

    /**
     * Chains UI component event loop triggers using optimal Java lambda blocks.
     */
    private void setupClickListeners() {
        // Notification Alert Bell Interaction Handler
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Opening corporate notification stream...", Toast.LENGTH_SHORT).show()
        );

        // Global Sign-Out Logic Route
        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupSecurityButton();

        // Grid Component Intercept Vectors
        cardManageLoan.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, LoanFormActivity.class);
            startActivity(intent);
        });

        cardManageUtility.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, UtilityManagerActivity.class);
            startActivity(intent);
        });
        
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SavingManagerActivity.class);
            startActivity(intent);
        });

        cardManageSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });

        // Direct Core Component Activations
        cardB2BInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, InvoiceHubActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });

        txtViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
        setupSavingsWidget();
    }

    private void setupSavingsWidget() {
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        View btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        View cardSavingsWidget = findViewById(R.id.cardSavingsWidget);

        if (txtCurrentSavingsValue != null && btnUpdateSavings != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
            btnUpdateSavings.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            if (cardSavingsWidget != null) {
                cardSavingsWidget.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            }
        }
    }

    private void loadSavingsFromFirestore(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentSavings = documentSnapshot.getString("currentSavings");
                        if (currentSavings != null && !currentSavings.trim().isEmpty()) {
                            try {
                                double amt = Double.parseDouble(currentSavings.trim());
                                txtValue.setText(String.format(Locale.US, "LKR %.2f", amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText("LKR " + currentSavings);
                            }
                        } else {
                            txtValue.setText("LKR 0.00");
                        }
                    }
                });
    }

    private void showUpdateSavingsDialog(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Current Savings");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount (LKR)");

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    double amt = Double.parseDouble(val);
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update("currentSavings", String.valueOf(amt))
                            .addOnSuccessListener(aVoid -> {
                                txtValue.setText(String.format(Locale.US, "LKR %.2f", amt));
                                Toast.makeText(this, "Savings updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        if (txtCurrentSavingsValue != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
        }
    }

    /**
     * Initializes structural linear state layouts safely for the recent view.
     */
    private void configureRecentListsFeed() {
        SampleRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Standardized message printer shortcut.
     */
    private void showModuleToast(String moduleName) {
        Toast.makeText(this, moduleName + " module integration coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void setupSecurityButton() {
        View btnSecurity = findViewById(R.id.btnSecurity);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                boolean isPinSet = PinHelper.isPinSet(this);
                String[] options;
                if (isPinSet) {
                    options = new String[]{"Change PIN Lock", "Disable PIN Lock"};
                } else {
                    options = new String[]{"Enable PIN Lock"};
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("PIN Lock Security");
                builder.setItems(options, (dialog, which) -> {
                    if (!isPinSet) {
                        Intent intent = new Intent(this, PinSetupActivity.class);
                        startActivity(intent);
                    } else {
                        if (which == 0) {
                            Intent intent = new Intent(this, PinSetupActivity.class);
                            startActivity(intent);
                        } else if (which == 1) {
                            PinHelper.clearPin(this);
                            Toast.makeText(this, "PIN Lock disabled successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }
    }
}