package com.example.smartfinancialmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

public class NotificationListActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private LinearLayout layoutEmptyState;
    private NotificationAdapter adapter;
    private NotificationRepository notificationRepo;
    private ListenerRegistration notificationsListener;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        notificationRepo = new NotificationRepository();
        
        initViews();
        setupRecyclerView();
    }

    private void initViews() {
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        TextView btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        btnMarkAllRead.setOnClickListener(v -> {
            notificationRepo.markAllAsRead(currentUserId);
            Toast.makeText(this, "Marked all as read", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, notification -> {
            if (!notification.isRead()) {
                notificationRepo.markAsRead(currentUserId, notification.getId());
            }
            
            // Navigate based on actionRoute
            if (notification.getActionRoute() != null) {
                Intent intent = null;
                switch (notification.getActionRoute()) {
                    case "UtilityManagerActivity":
                        intent = new Intent(this, UtilityManagerActivity.class);
                        break;
                    case "SubscriptionManagerActivity":
                        intent = new Intent(this, SubscriptionManagerActivity.class);
                        break;
                    case "SavingsPassportActivity":
                        intent = new Intent(this, SavingsPassportActivity.class);
                        break;
                    case "BudgetPlannerActivity":
                        intent = new Intent(this, BudgetPlannerActivity.class);
                        break;
                }
                
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
        
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        notificationsListener = notificationRepo.listenForNotifications(currentUserId, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onNotificationsLoaded(java.util.List<NotificationModel> notifications) {
                if (notifications.isEmpty()) {
                    recyclerNotifications.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerNotifications.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                    adapter.setNotifications(notifications);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NotificationListActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }
}
