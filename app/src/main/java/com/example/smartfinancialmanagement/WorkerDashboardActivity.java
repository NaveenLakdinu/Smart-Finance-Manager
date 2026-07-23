package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkerDashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtEarnings, txtPayrollStatus;
    private MaterialCardView cardWorkTasks, cardExpenseClaims, cardPayslips, cardLoanManager;
    private MaterialCardView cardSubscriptionManager, cardSavingManager, cardUtilityManager;
    private android.view.View btnTopLogout;

    private RecyclerView recyclerWorkerTasks;
    private TextView txtViewAllTasks;
    private List<Task> pendingTasks = new ArrayList<>();
    private TaskAdapter pendingTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        initViews();
        setupUserDetails();
        setupClickListeners();
        setupPendingTasks();
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
        recyclerWorkerTasks = findViewById(R.id.recyclerWorkerTasks);
        txtViewAllTasks = findViewById(R.id.txtViewAllTasks);
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
            loadSalaryFromFirestore(user.getUid());
            updatePayrollStatus();
        } else {
            txtEarnings.setText("LKR 0.00");
        }
    }

    private void updatePayrollStatus() {
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysUntilPayday = daysInMonth - dayOfMonth;

        if (daysUntilPayday <= 0) {
            txtPayrollStatus.setText("Payday is today!");
        } else if (daysUntilPayday == 1) {
            txtPayrollStatus.setText("Next payday: tomorrow");
        } else {
            txtPayrollStatus.setText(String.format(Locale.US, "Next payday in %d days", daysUntilPayday));
        }
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
            startActivity(new Intent(this, WorkerTasksActivity.class));
        });
        cardExpenseClaims.setOnClickListener(v -> {
            startActivity(new Intent(this, ExpenseClaimsActivity.class));
        });
        cardPayslips.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkerPayslipActivity.class));
        });
        cardLoanManager.setOnClickListener(v -> {
            startActivity(new Intent(this, LoanFormActivity.class));
        });
        cardSubscriptionManager.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionManagerActivity.class));
        });
        cardSavingManager.setOnClickListener(v -> {
            startActivity(new Intent(this, SavingManagerActivity.class));
        });
        cardUtilityManager.setOnClickListener(v -> {
            startActivity(new Intent(this, UtilityManagerActivity.class));
        });

        btnTopLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        if (txtViewAllTasks != null) {
            txtViewAllTasks.setOnClickListener(v -> {
                startActivity(new Intent(this, WorkerTasksActivity.class));
            });
        }

        setupSecurityButton();
        setupSavingsWidget();
    }

    private void setupSecurityButton() {
        android.view.View btnSecurity = findViewById(R.id.btnSecurity);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> {
                boolean isPinSet = PinHelper.isPinSet(this);
                String[] options;
                if (isPinSet) {
                    options = new String[]{"Change PIN Lock", "Disable PIN Lock"};
                } else {
                    options = new String[]{"Enable PIN Lock"};
                }

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("PIN Lock Security");
                builder.setItems(options, (dialog, which) -> {
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
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }
    }

    private void setupSavingsWidget() {
        TextView txtCurrentSavingsValue = findViewById(R.id.txtCurrentSavingsValue);
        android.view.View btnUpdateSavings = findViewById(R.id.btnUpdateSavings);
        android.view.View cardSavingsWidget = findViewById(R.id.cardSavingsWidget);

        if (txtCurrentSavingsValue != null && btnUpdateSavings != null) {
            loadSavingsFromFirestore(txtCurrentSavingsValue);
            btnUpdateSavings.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            if (cardSavingsWidget != null) {
                cardSavingsWidget.setOnClickListener(v -> showUpdateSavingsDialog(txtCurrentSavingsValue));
            }
        }
    }

    private void setupPendingTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        pendingTaskAdapter = new TaskAdapter(pendingTasks, (task, isChecked) -> {
            String newStatus = isChecked ? "Completed" : "In Progress";
            int newProgress = isChecked ? 100 : task.getProgress();

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .collection("tasks").document(task.getId())
                    .update("status", newStatus, "progress", newProgress)
                    .addOnSuccessListener(aVoid -> {
                        task.setCompleted(isChecked);
                        loadPendingTasks();
                    });
        });

        recyclerWorkerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkerTasks.setAdapter(pendingTaskAdapter);
        recyclerWorkerTasks.setNestedScrollingEnabled(false);

        loadPendingTasks();
    }

    private void loadPendingTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("tasks")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pendingTasks.clear();
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        if (count >= 3) break;
                        String status = doc.getString("status");
                        if (status == null || !"Completed".equals(status)) {
                            String id = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("description");
                            String priority = doc.getString("priority");
                            String dueDate = doc.getString("dueDate");
                            Long progressLong = doc.getLong("progress");
                            Long subtasksCompletedLong = doc.getLong("subtasksCompleted");
                            Long subtasksTotalLong = doc.getLong("subtasksTotal");

                            int progress = progressLong != null ? progressLong.intValue() : 0;
                            int subtasksCompleted = subtasksCompletedLong != null ? subtasksCompletedLong.intValue() : 0;
                            int subtasksTotal = subtasksTotalLong != null ? subtasksTotalLong.intValue() : 0;

                            if (title == null) title = "Untitled Task";
                            if (description == null) description = "No description";
                            if (priority == null) priority = "Medium";
                            if (status == null) status = "Pending";
                            if (dueDate == null) dueDate = "No due date";

                            String subtaskText = subtasksTotal > 0 ?
                                    subtasksCompleted + "/" + subtasksTotal + " subtasks" : "No subtasks";

                            Task task = new Task(id, title, description, priority, status, dueDate, subtaskText, progress);
                            pendingTasks.add(task);
                            count++;
                        }
                    }
                    pendingTaskAdapter.notifyDataSetChanged();
                });
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

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadSalaryFromFirestore(user.getUid());
            updatePayrollStatus();
        }
        loadPendingTasks();
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
