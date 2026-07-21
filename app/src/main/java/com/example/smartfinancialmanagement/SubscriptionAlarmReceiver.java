package com.example.smartfinancialmanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SubscriptionAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("TITLE");
        String message = intent.getStringExtra("MESSAGE");
        String userId = intent.getStringExtra("USER_ID");
        String docId = intent.getStringExtra("DOC_ID");
        String subName = intent.getStringExtra("SUB_NAME");
        double amount = intent.getDoubleExtra("AMOUNT", 0);
        int paymentDay = intent.getIntExtra("PAYMENT_DAY", 1);
        int requestCodeOffset = intent.getIntExtra("REQUEST_CODE_OFFSET", 0);

        if (title == null) title = "Subscription Reminder";
        if (message == null) message = "You have a subscription payment due";

        NotificationHelper.createNotificationChannels(context);
        NotificationHelper.showNotification(context, title, message, "bill_alerts");

        boolean isPaymentDay = message.contains("due today");

        if (isPaymentDay && userId != null && docId != null) {
            advancePaymentDate(context, userId, docId, paymentDay);
        }

        if (!isPaymentDay && userId != null && docId != null && subName != null) {
            scheduleRemainingNotifications(context, userId, docId, subName, amount, paymentDay, requestCodeOffset);
        }
    }

    private void advancePaymentDate(Context context, String userId, String docId, int paymentDay) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, paymentDay);

        String newRenewDate = String.format("%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        Map<String, Object> updates = new HashMap<>();
        updates.put("renewDate", newRenewDate);

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("subscriptions").document(docId)
                .update(updates);
    }

    private void scheduleRemainingNotifications(Context context, String userId, String docId,
                                                String subName, double amount, int paymentDay, int currentOffset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, paymentDay);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int daysUntil = (int) ((cal.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));

        if (daysUntil == 1) {
            String msg = "Your " + subName + " payment of Rs " + String.format("%.0f", amount) + " is due tomorrow";
            Subscription sub = new Subscription(subName, "", "Active", "", amount, paymentDay, "Monthly", 0);
            SubscriptionNotificationScheduler.scheduleAll(context, userId, docId, sub);
        }
    }
}
