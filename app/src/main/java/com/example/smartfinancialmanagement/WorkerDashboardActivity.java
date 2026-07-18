package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

public class WorkerDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtEarnings, txtPayrollStatus;
    private MaterialCardView cardWorkTasks, cardExpenseClaims, cardPayslips, cardLoanManager;
    private MaterialCardView cardSubscriptionManager, cardSavingManager, cardUtilityManager;
    private android.view.View btnTopLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        initViews();
        setupUserDetails();
        setupClickListeners();
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtEarnings = findViewById(R.id.txtEarnings);
        txtPayrollStatus = findViewById(R.id.txtPayrollStatus);

        cardWorkTasks = findViewById(R.id.cardWorkTasks);
        cardExpenseClaims = findViewById(R.id.cardExpenseClaims);
        cardPayslips = findViewById(R.id.cardPayslips);
        cardLoanManager = findViewById(R.id.cardLoanManager);
        cardSubscriptionManager = findViewById(R.id.cardSubscriptionManager);
        cardSavingManager = findViewById(R.id.cardSavingManager);
        cardUtilityManager = findViewById(R.id.cardUtilityManager);
        
        btnTopLogout = findViewById(R.id.btnTopLogout);
    }

    private void setupUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        txtGreeting.setText(getGreetingText());

if (user != null) {
    String email = user.getEmail();
    if (email != null && !email.isEmpty()) {
        txtUserEmail.setText(email);
        txtProfileLetter.setText(String.valueOf(email.charAt(0)).toUpperCase());
    }
    // Load salary from Firestore
    loadSalaryFromFirestore(user.getUid());
} else {
    txtEarnings.setText("LKR 0.00");
}
        
        txtPayrollStatus.setText("Next payday: June 30th");
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "Good Morning, Worker 👋";
        else if (hour >= 12 && hour < 17) return "Good Afternoon, Worker ☀️";
        else if (hour >= 17 && hour < 21) return "Good Evening, Worker 🌙";
        else return "Good Night, Worker ✨";
    }

    private void setupClickListeners() {
        cardWorkTasks.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkerTasksActivity.class);
            startActivity(intent);
        });
        cardExpenseClaims.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseClaimsActivity.class);
            startActivity(intent);
        });
        cardPayslips.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkerPayslipActivity.class);
            startActivity(intent);
        });
        cardLoanManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanFormActivity.class);
            startActivity(intent);
        });
        cardSubscriptionManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });
        cardSavingManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavingManagerActivity.class);
            startActivity(intent);
        });
        cardUtilityManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, UtilityManagerActivity.class);
            startActivity(intent);
        });

        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        setupSecurityButton();
        setupSavingsWidget();
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
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

    private void loadSalaryFromFirestore(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("worker_profile").document("profile_data")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double salary = documentSnapshot.getDouble("monthlySalary");
                        if (salary != null && salary > 0) {
                            txtEarnings.setText(String.format(Locale.US, "LKR %.2f", salary));
                        } else {
                            txtEarnings.setText("LKR 0.00");
                        }
                    } else {
                        txtEarnings.setText("LKR 0.00");
                    }
                })
                .addOnFailureListener(e -> txtEarnings.setText("LKR 0.00"));
    }

    private void showUpdateSavingsDialog(TextView txtValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog);
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
}