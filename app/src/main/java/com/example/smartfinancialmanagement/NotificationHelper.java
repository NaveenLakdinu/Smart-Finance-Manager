package com.example.smartfinancialmanagement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID_DEFAULT = "smart_finance_general";
    private static final String CHANNEL_ID_BILLS = "bill_alerts";
    private static final String CHANNEL_ID_INVOICES = "scheduled_invoice_reminders";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel general = new NotificationChannel(
                    CHANNEL_ID_DEFAULT,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            general.setDescription("General app notifications");

            NotificationChannel bills = new NotificationChannel(
                    CHANNEL_ID_BILLS,
                    "Bill Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            bills.setDescription("Reminders for upcoming bill due dates");

            NotificationChannel invoices = new NotificationChannel(
                    CHANNEL_ID_INVOICES,
                    "Invoice Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            invoices.setDescription("Reminders for upcoming invoice due dates");

            manager.createNotificationChannel(general);
            manager.createNotificationChannel(bills);
            manager.createNotificationChannel(invoices);
        }
    }

    public static void showNotification(Context context, String title, String message, String channelId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }

    public static void showFcmNotification(Context context, String title, String body) {
        showNotification(context, title, body, CHANNEL_ID_DEFAULT);
    }
}
