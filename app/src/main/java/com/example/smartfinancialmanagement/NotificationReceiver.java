package com.example.smartfinancialmanagement;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String billName = intent.getStringExtra("BILL_NAME");
        if (billName == null) billName = "Utility Bill";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "bill_alerts";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Bill Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Upcoming Bill Reminder")
                // Updated copy to match "1 day before due" logic
                .setContentText("Your bill '" + billName + "' is due tomorrow! Please make your payment.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Generate a random-esque ID so multiple bills don't override each other's notification tray alerts
        int notificationId = billName.hashCode();
        manager.notify(notificationId, builder.build());
    }
}