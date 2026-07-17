package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtTotalCount, txtSubMessage;

    private MaterialCardView cardManageLoan, cardManageSubscription, cardManageSaving, cardManageUtility;
    private android.view.View btnTopLogout;

    private LinearLayout recentSection;
    private RecyclerView recyclerRecent;

    private ArrayList<Subscription> recentList;
    private RecentSubscriptionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupRecyclerView();
        setupUserDetails();
        setupFunctionCards();
        loadUserSubscriptions();
    }

    private void initViews() {
        txtProfileLetter = findViewById(R.id.txtProfileLetter);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserEmail = findViewById(R.id.txtUserEmail);

        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtSubMessage = findViewById(R.id.txtSubMessage);

        cardManageLoan = findViewById(R.id.cardManageLoan);
        cardManageSubscription = findViewById(R.id.cardManageSubscription);
        cardManageSaving = findViewById(R.id.cardManageSaving);
        cardManageUtility = findViewById(R.id.cardManageUtility);
        btnTopLogout = findViewById(R.id.btnTopLogout);

        recentSection = findViewById(R.id.recentSection);
        recyclerRecent = findViewById(R.id.recyclerRecent);
        setupSavingsWidget();
    }

    private void setupRecyclerView() {
        recentList = new ArrayList<>();
        adapter = new RecentSubscriptionAdapter(this, recentList);

        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(adapter);
        recyclerRecent.setNestedScrollingEnabled(false);
    }

    private void setupUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        txtGreeting.setText(getGreetingText());

        if (user != null) {
            String email = user.getEmail();

            if (email != null && !email.isEmpty()) {
                txtUserEmail.setText(email);
                txtProfileLetter.setText(String.valueOf(email.charAt(0)).toUpperCase());
            } else {
                txtUserEmail.setText("No email found");
                txtProfileLetter.setText("U");
            }
        } else {
            txtUserEmail.setText("User not logged in");
            txtProfileLetter.setText("U");
        }
    }

    private String getGreetingText() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning 👋";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon ☀️";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening 🌙";
        } else {
            return "Good Night ✨";
        }
    }

    private void setupFunctionCards() {

        cardManageLoan.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, LoanFormActivity.class);
            startActivity(intent);
        });

        cardManageSubscription.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, SubscriptionManagerActivity.class);
            startActivity(intent);
        });

        cardManageSaving.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, SavingManagerActivity.class);
            startActivity(intent);
        });

        cardManageUtility.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, UtilityManagerActivity.class);
            startActivity(intent);
        });

        btnTopLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DashboardActivity.this, LoginFormActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserSubscriptions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            resetDashboard();
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore instead of Realtime Database
        db.collection("users").document(uid).collection("subscriptions")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        resetDashboard();
                        Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<Subscription> allSubscriptions = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Subscription subscription = doc.toObject(Subscription.class);
                            if (subscription != null) {
                                allSubscriptions.add(subscription);
                            }
                        }
                    }
                    updateDashboard(allSubscriptions);
                });
    }

    private void updateDashboard(ArrayList<Subscription> allSubscriptions) {

        int total = allSubscriptions.size();
        txtTotalCount.setText(String.format("%02d", total));

        if (total == 0) {
            txtSubMessage.setText("No subscriptions added yet");
            recentSection.setVisibility(View.GONE);

            recentList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        txtSubMessage.setText("Manage your plans easily");

        Collections.sort(allSubscriptions, (s1, s2) -> Long.compare(s2.getCreatedAt(), s1.getCreatedAt()));

        recentList.clear();

        int limit = Math.min(4, allSubscriptions.size());

        for (int i = 0; i < limit; i++) {
            recentList.add(allSubscriptions.get(i));
        }

        adapter.notifyDataSetChanged();
        recentSection.setVisibility(View.VISIBLE);
    }

    private void resetDashboard() {
        txtTotalCount.setText("00");
        txtSubMessage.setText("No subscriptions added yet");

        recentList.clear();
        adapter.notifyDataSetChanged();

        recentSection.setVisibility(View.GONE);
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
        loadUserSubscriptions();
    }
}