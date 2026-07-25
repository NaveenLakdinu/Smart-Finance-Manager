package com.example.smartfinancialmanagement;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Shared notification panel dialog used by all dashboard activities.
 * Eliminates code duplication across 6 dashboards.
 */
public class NotificationPanelHelper {

    private static final String PREFS_NAME = "smart_finance_notifications";
    private static final String KEY_SHOW_ON_RESUME = "show_notifications_on_resume";

    /**
     * Call from SmartFinanceMessagingService.onMessageReceived() to flag
     * that the notification panel should auto-open when the dashboard resumes.
     */
    public static void flagShowOnResume(android.content.Context context) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SHOW_ON_RESUME, true)
                .apply();
    }

    /**
     * Call from each dashboard's onResume(). If an FCM notification was tapped,
     * this clears the flag and opens the notification panel automatically.
     * Returns true if the panel was shown.
     */
    public static boolean checkAndShowOnResume(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SHOW_ON_RESUME, false)) {
            prefs.edit().putBoolean(KEY_SHOW_ON_RESUME, false).apply();
            show(activity);
            return true;
        }
        return false;
    }

    public static void show(AppCompatActivity activity) {
        View panelView = LayoutInflater.from(activity).inflate(R.layout.dialog_notifications, null);
        LinearLayout container = panelView.findViewById(R.id.layoutNotificationsContainer);
        TextView btnClose = panelView.findViewById(R.id.btnDismissNotifications);

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.Theme_SmartFinance_Dialog)
                .setView(panelView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        container.removeAllViews();
        TextView loadingView = createTextView(activity, "Loading messages...", "#94A3B8", 13f);
        loadingView.setPadding(0, dp(activity, 24), 0, dp(activity, 24));
        container.addView(loadingView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingView.setText("Not signed in.");
            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
            return;
        }

        FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("uid", currentUser.getUid())
            .whereEqualTo("isUserTargeted", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                container.removeAllViews();
                if (querySnapshot.isEmpty()) {
                    TextView emptyView = createTextView(activity, "No messages from support yet.", "#94A3B8", 13f);
                    emptyView.setPadding(0, dp(activity, 24), 0, dp(activity, 24));
                    container.addView(emptyView);
                } else {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        container.addView(buildNotificationCard(activity, doc));
                    }
                }
            })
            .addOnFailureListener(e -> {
                container.removeAllViews();
                TextView errView = createTextView(activity,
                        "Could not load messages: " + e.getMessage(), "#F87171", 12f);
                errView.setPadding(0, dp(activity, 16), 0, dp(activity, 16));
                container.addView(errView);
            });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static View buildNotificationCard(Activity activity, DocumentSnapshot doc) {
        String title = doc.getString("title");
        String body = doc.getString("body");
        Boolean isRead = doc.getBoolean("read");
        com.google.firebase.Timestamp createdAt = doc.getTimestamp("createdAt");

        int dp8 = dp(activity, 8);
        int dp12 = dp(activity, 12);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp12, dp12, dp12, dp12);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp8);
        card.setLayoutParams(cardParams);
        card.setBackgroundColor(Color.parseColor((isRead != null && isRead) ? "#1A2535" : "#1E2D42"));

        boolean unread = isRead == null || !isRead;

        // Unread tag — only shown, not auto-marked
        if (unread) {
            TextView unreadTag = createTextView(activity, "NEW", "#2DD4BF", 10f);
            unreadTag.setPadding(0, 0, 0, dp8);
            card.addView(unreadTag);
        }

        // Title
        TextView titleView = createTextView(activity,
                title != null ? title : "Support Message", "#2DD4BF", 13f);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 0, 0, dp8);
        card.addView(titleView);

        // Body
        TextView bodyView = createTextView(activity, body != null ? body : "", "#CBD5E1", 12.5f);
        bodyView.setLineSpacing(0, 1.4f);
        card.addView(bodyView);

        // Timestamp
        if (createdAt != null) {
            TextView timeView = createTextView(activity, formatRelativeTime(createdAt.toDate()), "#64748B", 11f);
            timeView.setPadding(0, dp8, 0, 0);
            card.addView(timeView);
        }

        // Mark as read on tap (not on dialog open)
        if (unread) {
            card.setOnClickListener(v -> {
                doc.getReference().update("read", true);
                card.setBackgroundColor(Color.parseColor("#1A2535"));
                // Remove the NEW tag
                if (card.getChildAt(0) instanceof TextView) {
                    TextView firstChild = (TextView) card.getChildAt(0);
                    if ("NEW".equals(firstChild.getText().toString())) {
                        card.removeViewAt(0);
                    }
                }
            });
        }

        return card;
    }

    private static TextView createTextView(Activity activity, String text, String colorHex, float spSize) {
        TextView tv = new TextView(activity);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        return tv;
    }

    private static int dp(Activity activity, int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }

    private static String formatRelativeTime(Date date) {
        long diffMs = System.currentTimeMillis() - date.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
        if (hours < 24) return hours + "h ago";
        long days = TimeUnit.MILLISECONDS.toDays(diffMs);
        if (days < 7) return days + "d ago";
        return new SimpleDateFormat("MMM d", Locale.US).format(date);
    }
}
