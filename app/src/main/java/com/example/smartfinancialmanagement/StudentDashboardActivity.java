package com.example.smartfinancialmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.TextView;

public class StudentDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);



        initViews();
    }

    private void initViews() {
        View btnTopLogout = findViewById(R.id.btnTopLogout);
        if (btnTopLogout != null) {
            btnTopLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        View budgetPlan = findViewById(R.id.budget_plan);
        if (budgetPlan != null) {
            budgetPlan.setOnClickListener(v -> startActivity(new Intent(this, BudgetPlannerActivity.class)));
        }

        View btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        if (btnProfileAvatar != null) {
            btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, StudentProfileActivity.class)));
        }

        // Header Cards
        View cardAchievement = findViewById(R.id.cardDashboardHeaderAchievement);
        View cardBudget = findViewById(R.id.cardDashboardHeaderBudget);

        if (cardAchievement != null) {
            cardAchievement.setOnClickListener(v -> startActivity(new Intent(this, SavingsPassportActivity.class)));
        }
        if (cardBudget != null) {
            cardBudget.setOnClickListener(v -> startActivity(new Intent(this, BudgetPlannerActivity.class)));
        }

        // Core Manager Cards
        View cardLoanManager = findViewById(R.id.cardLoanManager);
        View cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        View cardSavingManager = findViewById(R.id.cardSavingManager);
        View cardUtilityManager = findViewById(R.id.cardUtilityManager);

        if (cardLoanManager != null) {
            cardLoanManager.setOnClickListener(v -> startActivity(new Intent(this, LoanFormActivity.class)));
        }
        if (cardSubscriptionManager != null) {
            cardSubscriptionManager.setOnClickListener(v -> startActivity(new Intent(this, SubscriptionManagerActivity.class)));
        }
        if (cardSavingManager != null) {
            cardSavingManager.setOnClickListener(v -> startActivity(new Intent(this, SavingManagerActivity.class)));
        }
        if (cardUtilityManager != null) {
            cardUtilityManager.setOnClickListener(v -> startActivity(new Intent(this, UtilityManagerActivity.class)));
        }

        setupSecurityButton();
        setupSavingsWidget();

        loadAchievementData();
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

        final android.widget.EditText input = new android.widget.EditText(this);
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

    private void loadAchievementData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "test_user";

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                
                List<SavingModel> savings = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    SavingModel saving = doc.toObject(SavingModel.class);
                    savings.add(saving);
                }
                
                double totalSavings = 0;
                for (SavingModel s : savings) {
                    totalSavings += s.getCurrentAmount();
                }

                String level = getSavingsLevel(totalSavings);
                
                TextView txtAchievementPts = findViewById(R.id.txtAchievementPts);
                if (txtAchievementPts != null) {
                    txtAchievementPts.setText(level);
                }
            });
    }

    private String getSavingsLevel(double totalSavings) {
        if (totalSavings >= 50000) return "Gold Saver";
        if (totalSavings >= 25000) return "Silver Saver";
        if (totalSavings >= 5000)  return "Bronze Saver";
        return "Starter";
    }
}
