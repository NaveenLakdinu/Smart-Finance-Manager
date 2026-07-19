package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {
    
    private double mTotalIncome = 0;
    private double mDirectIncome = 0;
    private double mBudgetIncome = 0;
    private double mTotalSavings = 0;
    private double mTotalLoans = 0;
    private double mTotalUtilityBills = 0;
    private double mTotalSubscriptions = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);



        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        recalculateTotalIncome();
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

        loadAchievementData();
        loadCurrentBalance();
        loadUserData();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    String name = documentSnapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        TextView tvStudentName = findViewById(R.id.tvStudentName);
                        TextView tvInitials = findViewById(R.id.tvInitials);
                        
                        if (tvStudentName != null) {
                            tvStudentName.setText(name);
                        }
                        
                        if (tvInitials != null) {
                            // Extract initials
                            String[] words = name.trim().split("\\s+");
                            StringBuilder initials = new StringBuilder();
                            if (words.length > 0 && !words[0].isEmpty()) {
                                initials.append(words[0].charAt(0));
                                if (words.length > 1 && !words[words.length - 1].isEmpty()) {
                                    initials.append(words[words.length - 1].charAt(0));
                                }
                            }
                            tvInitials.setText(initials.toString().toUpperCase(Locale.getDefault()));
                        }
                    }
                }
            });
    }

    private void loadAchievementData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "test_user";

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("savings")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                
                List<SavingModel> savings = new ArrayList<>();
                int activeGoals = 0;
                for (QueryDocumentSnapshot doc : snapshot) {
                    SavingModel saving = doc.toObject(SavingModel.class);
                    savings.add(saving);
                    if (!"COMPLETED".equalsIgnoreCase(saving.getStatus())) {
                        activeGoals++;
                    }
                }
                
                TextView txtSavingBadge = findViewById(R.id.txtSavingBadge);
                if (txtSavingBadge != null) {
                    if (activeGoals > 0) {
                        txtSavingBadge.setText("On Track");
                    } else {
                        txtSavingBadge.setText("No Goals");
                    }
                }
                
                mTotalSavings = 0;
                for (SavingModel s : savings) {
                    mTotalSavings += s.getCurrentAmount();
                }
                
                recalculateTotalIncome();

                String level = getSavingsLevel(mTotalSavings);
                
                TextView txtAchievementPts = findViewById(R.id.txtAchievementPts);
                if (txtAchievementPts != null) {
                    txtAchievementPts.setText(level);
                }
                
                TextView txtTrophyIcon = findViewById(R.id.txtTrophyIcon);
                if (txtTrophyIcon != null) {
                    switch (level) {
                        case "Gold Saver": txtTrophyIcon.setText("🥇"); break;
                        case "Silver Saver": txtTrophyIcon.setText("🥈"); break;
                        case "Bronze Saver": txtTrophyIcon.setText("🥉"); break;
                        default: txtTrophyIcon.setText("🏆"); break;
                    }
                }
            });
    }

    private String getSavingsLevel(double totalSavings) {
        if (totalSavings >= 50000) return "Gold Saver";
        if (totalSavings >= 25000) return "Silver Saver";
        if (totalSavings >= 5000)  return "Bronze Saver";
        return "Starter";
    }

    private void loadCurrentBalance() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("incomes")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                
                mDirectIncome = 0;
                for (QueryDocumentSnapshot doc : snapshot) {
                    IncomeModel income = doc.toObject(IncomeModel.class);
                    mDirectIncome += income.getAmount();
                }
                recalculateTotalIncome();
            });

        // Listen to budget plans for fallback income
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("budgetPlans")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                
                mBudgetIncome = 0;
                BudgetModel latestBudget = null;
                
                for (QueryDocumentSnapshot doc : snapshot) {
                    BudgetModel budget = doc.toObject(BudgetModel.class);
                    if (latestBudget == null) {
                        latestBudget = budget;
                    } else if (budget.getCreatedAt() != null && latestBudget.getCreatedAt() != null) {
                        if (budget.getCreatedAt().after(latestBudget.getCreatedAt())) {
                            latestBudget = budget;
                        }
                    }
                }
                
                if (latestBudget != null) {
                    mBudgetIncome = latestBudget.getSemesterIncome();
                }
                recalculateTotalIncome();
            });

        // Listen to loans
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("loans")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                mTotalLoans = 0;
                int activeLoansCount = 0;
                for (QueryDocumentSnapshot doc : snapshot) {
                    Double amount = doc.getDouble("principalAmount");
                    if (amount != null) mTotalLoans += amount;
                    
                    String status = doc.getString("status");
                    if (!"PAID".equalsIgnoreCase(status) && !"COMPLETED".equalsIgnoreCase(status)) {
                        activeLoansCount++;
                    }
                }
                TextView txtLoanBadge = findViewById(R.id.txtLoanBadge);
                if (txtLoanBadge != null) {
                    txtLoanBadge.setText(activeLoansCount + " Active");
                }
                recalculateTotalIncome();
            });

        // Listen to utility bills
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("utility_bills")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                mTotalUtilityBills = 0;
                int dueCount = 0;
                for (QueryDocumentSnapshot doc : snapshot) {
                    Double amount = doc.getDouble("amount");
                    if (amount != null) mTotalUtilityBills += amount;
                    
                    String status = doc.getString("status");
                    if (!"PAID".equalsIgnoreCase(status)) {
                        dueCount++;
                    }
                }
                TextView txtUtilityBadge = findViewById(R.id.txtUtilityBadge);
                if (txtUtilityBadge != null) {
                    txtUtilityBadge.setText(dueCount + " Due");
                }
                recalculateTotalIncome();
            });

        // Listen to subscriptions (acting as expenses)
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("subscriptions")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;
                mTotalSubscriptions = 0;
                int subCount = snapshot.size();
                for (QueryDocumentSnapshot doc : snapshot) {
                    Double amount = doc.getDouble("amount");
                    if (amount != null) mTotalSubscriptions += amount;
                }
                TextView txtSubscriptionBadge = findViewById(R.id.txtSubscriptionBadge);
                if (txtSubscriptionBadge != null) {
                    txtSubscriptionBadge.setText(subCount + " Plans");
                }
                recalculateTotalIncome();
            });
    }

    private void recalculateTotalIncome() {
        mTotalIncome = mDirectIncome > 0 ? mDirectIncome : mBudgetIncome;
        updateUIWithTotalIncome();
    }

    private void updateUIWithTotalIncome() {
        double currentBalance = mTotalIncome - mTotalSubscriptions - mTotalSavings - mTotalLoans - mTotalUtilityBills;
        // If currentBalance goes negative, that's fine to show mathematically.
        
        updateBudgetLeft(currentBalance);
        
        TextView txtCurrentBalanceValue = findViewById(R.id.txtCurrentBalanceValue);
        if (txtCurrentBalanceValue != null) {
            txtCurrentBalanceValue.setText(CurrencyHelper.formatMoney(this, currentBalance));
        }
    }

    private void updateBudgetLeft(double currentBalance) {
        // According to instructions, Budget Left can just mirror Current Balance
        // or we use the newly computed balance
        double budgetLeft = currentBalance;
        if (budgetLeft < 0) budgetLeft = 0;
        
        TextView txtBudgetLeftValue = findViewById(R.id.txtBudgetLeftValue);
        if (txtBudgetLeftValue != null) {
            txtBudgetLeftValue.setText(CurrencyHelper.formatMoney(this, budgetLeft));
        }
    }
}
