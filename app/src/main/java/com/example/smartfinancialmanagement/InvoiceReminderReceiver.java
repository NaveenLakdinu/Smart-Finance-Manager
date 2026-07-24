package com.example.smartfinancialmanagement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class InvoiceReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String clientName = intent.getStringExtra("clientName");
        String dueDate = intent.getStringExtra("dueDate");

        boolean isEmailReminderEnabled = intent.getBooleanExtra("isEmailReminderEnabled", false);

        String businessEmail = intent.getStringExtra("businessEmail");
        double grandTotal = intent.getDoubleExtra("grandTotal", 0.0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "scheduled_invoice_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Invoice Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Upcoming Invoice Due Tomorrow")
                .setContentText("Invoice for " + (clientName != null ? clientName : "Client") + " is due on " + (dueDate != null ? dueDate : "scheduled date"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (manager != null) {
            // Using unique request codes by truncation to safe bit scopes to avoid structural collisions
            manager.notify((int) (System.currentTimeMillis() & 0xfffffff), builder.build());
        }


        if (isEmailReminderEnabled && businessEmail != null && !businessEmail.trim().isEmpty()) {

            InvoiceEmailSender.sendInvoiceEmail(businessEmail, clientName, dueDate, grandTotal);
        }
    }
}