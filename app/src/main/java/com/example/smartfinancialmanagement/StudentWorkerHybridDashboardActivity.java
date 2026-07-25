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

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtEarnings, txtPayrollStatus, txtCurrentSavingsValue;
    private View btnUpdateSavings, cardSavingsWidget, btnSecurity, btnTopLogout;
    private View cardStudentBudget, cardLoanManager, cardSavingManager, cardSubscriptionManager, cardUtilityManager;
    private View cardWorkTasks, cardExpenseClaims, cardPayslips;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_worker_hybrid_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupBottomNavigation();
        setupUserDetails();
        setupClickListeners();
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtEarnings = findViewById(R.id.txtEarnings);
        txtPayrollStatus = findViewById(R.id.txtPayrollStatus);
        txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);

        btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        cardSavingsWidget = findViewById(R.id.cardSavingsWidget);
        btnSecurity = findViewById(R.id.btnSecurity);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        cardStudentBudget = findViewById(R.id.cardStudentBudget);
        cardLoanManager = findViewById(R.id.cardLoanManager);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        cardUtilityManager = findViewById(R.id.cardUtilityManager);
        cardWorkTasks = findViewById(R.id.cardWorkTasks);
        cardExpenseClaims = findViewById(R.id.cardExpenseClaims);
        cardPayslips = findViewById(R.id.cardPayslips);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_budget) {
                startActivity(new Intent(this, StudentBudgetActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_savings) {
                startActivity(new Intent(this, StudentSavingActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, StudentProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        txtGreeting.setText(getGreetingText());

        if (user != null) {
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                txtUserEmail.setText(email);
                txtProfileLetter.setText(String.valueOf(email.charAt(0)).toUpperCase());
            }
            loadSalaryFromFirestore(user.getUid());
        } else {
            txtEarnings.setText("Rs 0.00");
            txtPayrollStatus.setText(getNextPaydayText());
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
                            txtEarnings.setText(String.format(Locale.US, "Rs %.2f", salary));
                        } else {
                            txtEarnings.setText("Rs 0.00");
                        }
                    } else {
                        txtEarnings.setText("Rs 0.00");
                    }
                    txtPayrollStatus.setText(getNextPaydayText());
                })
                .addOnFailureListener(e -> {
                    txtEarnings.setText("Rs 0.00");
                    txtPayrollStatus.setText(getNextPaydayText());
                });
    }

    private String getNextPaydayText() {
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysUntilPayday = daysInMonth - dayOfMonth;
        if (daysUntilPayday <= 0) return "Payday is today!";
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
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());
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

        if (cardStudentBudget != null) {
            cardStudentBudget.setOnClickListener(v -> startActivity(new Intent(this, StudentBudgetActivity.class)));
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
                                txtValue.setText(String.format(Locale.US, "Rs %.2f", amt));
                            } catch (NumberFormatException e) {
                                txtValue.setText("Rs " + currentSavings);
                            }
                        } else {
                            txtValue.setText("Rs 0.00");
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
                    db.collection("users").document(user.getUid())
                            .update("currentSavings", String.valueOf(amt))
                            .addOnSuccessListener(aVoid -> {
                                txtValue.setText(String.format(Locale.US, "Rs %.2f", amt));
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

    private void showNotificationPanelDialog() {
        NotificationPanelHelper.show(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationPanelHelper.checkAndShowOnResume(this);
        if (txtCurrentSavingsValue != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
        }
        setupUserDetails();
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
