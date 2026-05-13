package com.example.subscriptionapp;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtProfileLetter, txtGreeting, txtUserEmail;
    private TextView txtTotalCount, txtSubMessage;

    private CardView cardManageLoan, cardManageSubscription, cardManageUtility, cardPaused;

    private LinearLayout recentSection;
    private RecyclerView recyclerRecent;

    private ArrayList<Subscription> recentList;
    private RecentSubscriptionAdapter adapter;

    private DatabaseReference subscriptionsRef;
    private ValueEventListener subscriptionsListener;

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

        cardManageLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ManageLoanActivity.class);
                startActivity(intent);
            }
        });

        cardManageSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ManageSubscriptionActivity.class);
                startActivity(intent);
            }
        });

        cardManageUtility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ManageUtilityActivity.class);
                startActivity(intent);
            }
        });

        cardPaused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, PausedActivity.class);
                startActivity(intent);
            }
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

        subscriptionsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("subscriptions");

        subscriptionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Subscription> allSubscriptions = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Subscription subscription = child.getValue(Subscription.class);

                        if (subscription != null) {
                            allSubscriptions.add(subscription);
                        }
                    }
                }

                updateDashboard(allSubscriptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                resetDashboard();
                Toast.makeText(DashboardActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        subscriptionsRef.addValueEventListener(subscriptionsListener);
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

        Collections.sort(allSubscriptions, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription s1, Subscription s2) {
                return Long.compare(s2.getCreatedAt(), s1.getCreatedAt());
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (subscriptionsRef != null && subscriptionsListener != null) {
            subscriptionsRef.removeEventListener(subscriptionsListener);
        }
    }
}