package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtTotalCount, txtSubMessage;

    private CardView cardManageLoan, cardManageSubscription, cardManageUtility, cardPaused;
    private MaterialButton btnLogout;

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
        cardManageUtility = findViewById(R.id.cardManageUtility);
        cardPaused = findViewById(R.id.cardPaused);
        btnLogout = findViewById(R.id.btnLogout);

        recentSection = findViewById(R.id.recentSection);
        recyclerRecent = findViewById(R.id.recyclerRecent);
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

        cardManageSubscription.setOnClickListener(view -> Toast.makeText(DashboardActivity.this, "Subscription Management - Coming Soon", Toast.LENGTH_SHORT).show());

        cardManageUtility.setOnClickListener(view -> Toast.makeText(DashboardActivity.this, "Utility Management - Coming Soon", Toast.LENGTH_SHORT).show());

        cardPaused.setOnClickListener(view -> Toast.makeText(DashboardActivity.this, "Paused Subscriptions - Coming Soon", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(view -> {
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
    }