package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class StudentWorkerHybridDashboardActivity extends AppCompatActivity {

    private TextView tvInitials, tvStudentName;
    private TextView txtEarnings, txtPayrollStatus, txtCurrentSavingsValue;
    private View btnUpdateSavings, cardSavingsWidget, btnSecurity, btnTopLogout;
    private View cardDashboardHeaderAchievement, cardDashboardHeaderBudget, cardLoanManager, cardSavingManager, cardSubscriptionManager, cardUtilityManager;
    private View cardWorkTasks, cardExpenseClaims, cardPayslips;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int currentPayday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_worker_hybrid_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupUserDetails();
        loadUserData();
        loadAvatarImage();
        loadAchievementBadge();
        setupClickListeners();
    }

    private void initViews() {
        tvInitials = findViewById(R.id.tvInitials);
        tvStudentName = findViewById(R.id.tvStudentName);
        txtEarnings = findViewById(R.id.txtEarnings);
        txtPayrollStatus = findViewById(R.id.txtPayrollStatus);
        txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);

        btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        cardSavingsWidget = findViewById(R.id.cardSavingsWidget);
        btnSecurity = findViewById(R.id.btnSecurity);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        cardDashboardHeaderAchievement = findViewById(R.id.cardDashboardHeaderAchievement);
        cardDashboardHeaderBudget = findViewById(R.id.cardDashboardHeaderBudget);
        cardLoanManager = findViewById(R.id.cardLoanManager);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        cardUtilityManager = findViewById(R.id.cardUtilityManager);
        cardWorkTasks = findViewById(R.id.cardWorkTasks);
        cardExpenseClaims = findViewById(R.id.cardExpenseClaims);
        cardPayslips = findViewById(R.id.cardPayslips);
    }

    private void setupUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
            }
            loadSalaryFromFirestore(user.getUid());
        } else {
            txtEarnings.setText(CurrencyHelper.formatMoney(this, 0));
            txtPayrollStatus.setText(getNextPaydayText());
        }
    }

    
    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    TextView tvStudentName = findViewById(R.id.tvStudentName);
                    if (tvStudentName != null) {
                        tvStudentName.setText(documentSnapshot.getString("name"));
                    }
                    TextView tvInitials = findViewById(R.id.tvInitials);
                    if (tvInitials != null) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            tvInitials.setText(name.substring(0, 1).toUpperCase());
                        }
                    }
                }
            });
    }

    private void loadAvatarImage() {
        android.content.SharedPreferences prefs = getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE);
        String uriStr = prefs.getString("avatar_uri", null);
        android.widget.ImageView imgDashboardAvatar = findViewById(R.id.imgDashboardAvatar);
        TextView tvInitials = findViewById(R.id.tvInitials);
        if (uriStr != null && imgDashboardAvatar != null) {
            try {
                imgDashboardAvatar.setImageURI(android.net.Uri.parse(uriStr));
            } catch (SecurityException e) {
                getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE).edit().remove("avatar_uri").apply();
            }
            imgDashboardAvatar.setVisibility(android.view.View.VISIBLE);
            if (tvInitials != null) tvInitials.setVisibility(android.view.View.GONE);
        } else {
            if (imgDashboardAvatar != null) imgDashboardAvatar.setVisibility(android.view.View.GONE);
            if (tvInitials != null) tvInitials.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void loadSalaryFromFirestore(String uid) {
        db.collection("users").document(uid)
                .collection("worker_profile").document("profile_data")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double salary = documentSnapshot.getDouble("monthlySalary");
                        if (salary != null && salary > 0) {
                            txtEarnings.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, salary));
                        } else {
                            txtEarnings.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, 0));
                        }
                        
                        Long paydayLong = documentSnapshot.getLong("payday");
                        if (paydayLong != null) {
                            currentPayday = paydayLong.intValue();
                        } else {
                            currentPayday = 0;
                        }
                    } else {
                        txtEarnings.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, 0));
                        currentPayday = 0;
                    }
                    txtPayrollStatus.setText(getNextPaydayText());
                })
                .addOnFailureListener(e -> {
                    txtEarnings.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, 0));
                    currentPayday = 0;
                    txtPayrollStatus.setText(getNextPaydayText());
                });
    }

    private String getNextPaydayText() {
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        int targetPayday = currentPayday;
        if (targetPayday <= 0 || targetPayday > 31) {
             targetPayday = daysInMonth;
        } else if (targetPayday > daysInMonth) {
             targetPayday = daysInMonth;
        }

        int daysUntilPayday;
        if (dayOfMonth <= targetPayday) {
             daysUntilPayday = targetPayday - dayOfMonth;
        } else {
             Calendar nextMonth = Calendar.getInstance();
             nextMonth.add(Calendar.MONTH, 1);
             int nextMonthDays = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
             int nextTarget = currentPayday;
             if (nextTarget <= 0 || nextTarget > 31) {
                  nextTarget = nextMonthDays;
             } else if (nextTarget > nextMonthDays) {
                  nextTarget = nextMonthDays;
             }
             daysUntilPayday = (daysInMonth - dayOfMonth) + nextTarget;
        }
        
        if (daysUntilPayday == 0) return "Payday is today!";
        if (daysUntilPayday == 1) return "Next payday: tomorrow";
        return String.format(Locale.US, "Next payday in %d days", daysUntilPayday);
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "Good Morning, Hybrid Scholar \uD83D\uDC4B";
        else if (hour >= 12 && hour < 17) return "Good Afternoon, Hybrid Scholar \u2600\uFE0F";
        else if (hour >= 17 && hour < 21) return "Good Evening, Hybrid Scholar \uD83C\uDF19";
        else return "Good Night, Hybrid Scholar \u2728";
    }

    private void setupClickListeners() {
        // Notification button
        View btnNotifications = findViewById(R.id.btnNotifications);
        View btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        if (btnProfileAvatar != null) {
            btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, StudentProfileActivity.class)));
        }

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationListActivity.class)));
        }

        if (btnTopLogout != null) {
            btnTopLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(this, LoginFormActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        }

        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                boolean isPinSet = PinHelper.isPinSet(this);
                String[] options;
                if (isPinSet) {
                    options = new String[]{"Change PIN Lock", "Disable PIN Lock"};
                } else {
                    options = new String[]{"Enable PIN Lock"};
                }

                new AlertDialog.Builder(this)
                        .setTitle("PIN Lock Security")
                        .setItems(options, (dialog, which) -> {
                            if (!isPinSet) {
                                startActivity(new Intent(this, PinSetupActivity.class));
                            } else {
                                if (which == 0) {
                                    startActivity(new Intent(this, PinSetupActivity.class));
                                } else if (which == 1) {
                                    PinHelper.clearPin(this);
                                    Toast.makeText(this, "PIN Lock disabled successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }


        if (cardDashboardHeaderAchievement != null) {
            cardDashboardHeaderAchievement.setOnClickListener(v -> startActivity(new Intent(this, SavingsPassportActivity.class)));
        }
        if (cardDashboardHeaderBudget != null) {
            cardDashboardHeaderBudget.setOnClickListener(v -> startActivity(new Intent(this, BudgetPlannerActivity.class)));
        }
        if (cardLoanManager != null) {
            cardLoanManager.setOnClickListener(v -> startActivity(new Intent(this, LoanFormActivity.class)));
        }
        if (cardSavingManager != null) {
            cardSavingManager.setOnClickListener(v -> startActivity(new Intent(this, SavingManagerActivity.class)));
        }
        if (cardSubscriptionManager != null) {
            cardSubscriptionManager.setOnClickListener(v -> startActivity(new Intent(this, SubscriptionManagerActivity.class)));
        }
        if (cardUtilityManager != null) {
            cardUtilityManager.setOnClickListener(v -> startActivity(new Intent(this, UtilityManagerActivity.class)));
        }
        if (cardWorkTasks != null) {
            cardWorkTasks.setOnClickListener(v -> startActivity(new Intent(this, WorkerTasksActivity.class)));
        }
        if (cardExpenseClaims != null) {
            cardExpenseClaims.setOnClickListener(v -> startActivity(new Intent(this, ExpenseClaimsActivity.class)));
        }
        if (cardPayslips != null) {
            cardPayslips.setOnClickListener(v -> startActivity(new Intent(this, WorkerPayslipActivity.class)));
        }

        View cardWorkerEarnings = findViewById(R.id.cardWorkerEarnings);
        if (cardWorkerEarnings != null) {
            cardWorkerEarnings.setOnClickListener(v -> showUpdateSalaryDialog());
        }

        View cardUndoUpgrade = findViewById(R.id.cardUndoUpgrade);
        if (cardUndoUpgrade != null) {
            cardUndoUpgrade.setOnClickListener(v -> undoUpgrade());
        }

        setupSavingsWidget();
    }

    private void setupSavingsWidget() {
        if (txtCurrentSavingsValue != null && btnUpdateSavings != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
            btnUpdateSavings.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            if (cardSavingsWidget != null) {
                cardSavingsWidget.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            }
        }
    }

    private void loadSavingsFromFirestore(TextView txtValue) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentSavings = documentSnapshot.getString("currentSavings");
                        if (currentSavings != null && !currentSavings.trim().isEmpty()) {
                            try {
                                double amt = Double.parseDouble(currentSavings.trim());
                                txtValue.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText(CurrencyHelper.getCurrencySymbol(StudentWorkerHybridDashboardActivity.this) + " " + currentSavings);
                            }
                        } else {
                            txtValue.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, 0));
                        }
                    }
                });
    }

    private void showUpdateSavingsDialog(TextView txtValue) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Current Savings");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount (" + CurrencyHelper.getCurrencySymbol(this) + ")");

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
                    db.collection("users").document(user.getUid())
                            .update("currentSavings", String.valueOf(amt))
                            .addOnSuccessListener(aVoid -> {
                                txtValue.setText(CurrencyHelper.formatMoney(StudentWorkerHybridDashboardActivity.this, amt));
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

    private void undoUpgrade() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Undo Upgrade")
                .setMessage("Are you sure you want to revert to the standard Student Dashboard? You will lose access to the worker features on your main screen.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    db.collection("users").document(user.getUid())
                            .update("role", "Student")
                            .addOnSuccessListener(aVoid -> {
                                android.content.SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                prefs.edit().putString("user_role", "Student").apply();
                                Intent intent = new Intent(this, StudentDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to undo upgrade", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateSalaryDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Salary & Payday");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        final EditText inputSalary = new EditText(this);
        inputSalary.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputSalary.setHint("Monthly Salary (" + CurrencyHelper.getCurrencySymbol(this) + ")");
        layout.addView(inputSalary);

        final EditText inputPayday = new EditText(this);
        inputPayday.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputPayday.setHint("Payday (1-31)");
        layout.addView(inputPayday);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String salaryStr = inputSalary.getText().toString().trim();
            String paydayStr = inputPayday.getText().toString().trim();
            
            double salary = 0;
            int payday = 0;
            
            try {
                if (!salaryStr.isEmpty()) salary = Double.parseDouble(salaryStr);
                if (!paydayStr.isEmpty()) payday = Integer.parseInt(paydayStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show();
                return;
            }

            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("monthlySalary", salary);
            updates.put("payday", payday);

            db.collection("users").document(user.getUid())
                    .collection("worker_profile").document("profile_data")
                    .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        loadSalaryFromFirestore(user.getUid());
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }



    @Override
    protected void onResume() {
        super.onResume();

        NotificationPanelHelper.checkAndShowOnResume(this);
        if (txtCurrentSavingsValue != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
        }
        setupUserDetails();
        loadUserData();
        loadAvatarImage();
        loadAchievementBadge();
        loadBadgeData();
    }

    private void loadBadgeData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("loans").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                TextView txtLoanBadge = findViewById(R.id.txtLoanBadge);
                if (txtLoanBadge != null) {
                    txtLoanBadge.setText(queryDocumentSnapshots.size() + " Active");
                }
            });

        db.collection("users").document(uid).collection("subscriptions").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                TextView txtSubscriptionBadge = findViewById(R.id.txtSubscriptionBadge);
                if (txtSubscriptionBadge != null) {
                    txtSubscriptionBadge.setText(queryDocumentSnapshots.size() + " Plans");
                }
            });

        db.collection("utilityBill").whereEqualTo("userId", uid).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                TextView txtUtilityBadge = findViewById(R.id.txtUtilityBadge);
                if (txtUtilityBadge != null) {
                    int dueCount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if (!"Paid".equalsIgnoreCase(status) && !"PAID".equalsIgnoreCase(status)) {
                            dueCount++;
                        }
                    }
                    txtUtilityBadge.setText(dueCount + " Due");
                }
            });
    }

    private void loadAchievementBadge() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).collection("savings")
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null) return;

                double totalSavings = 0;
                int activeGoals = 0;
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                    Double amount = doc.getDouble("currentAmount");
                    if (amount != null) totalSavings += amount;
                    String status = doc.getString("status");
                    if (!"COMPLETED".equalsIgnoreCase(status)) {
                        activeGoals++;
                    }
                }

                TextView txtSavingBadge = findViewById(R.id.txtSavingBadge);
                if (txtSavingBadge != null) {
                    if (activeGoals > 0) {
                        txtSavingBadge.setText(activeGoals + " Active");
                    } else {
                        txtSavingBadge.setText("0 Active");
                    }
                }

                String level = "Starter";
                if (totalSavings >= 50000) level = "Gold Saver";
                else if (totalSavings >= 25000) level = "Silver Saver";
                else if (totalSavings >= 5000) level = "Bronze Saver";

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

    private void animateCards(View... cards) {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                cards[i].setAlpha(0f);
                cards[i].setTranslationY(40f);
                final int delay = i * 100;
                cards[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
            }
        }
    }
}
