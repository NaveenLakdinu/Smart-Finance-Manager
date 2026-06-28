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
        String clientName = intent.getStringExtra("clientName");
        String dueDate = intent.getStringExtra("dueDate");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "scheduled_invoice_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Invoice Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                // Fixed: Changed to a reliable android system resource icon
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Upcoming Invoice Due Tomorrow")
                .setContentText("Invoice for " + clientName + " is due on " + dueDate)
                .setAutoCancel(true);

        // Uses a unique ID based on timestamp so multiple reminders don't overwrite each other
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}