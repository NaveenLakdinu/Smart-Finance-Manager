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

        // 💡 පරිශීලකයා දුන් අවසරය කියවා ගැනීම (Default එක false වේ)
        boolean isEmailReminderEnabled = intent.getBooleanExtra("isEmailReminderEnabled", false);

        // 💡 ඊමේල් එක යැවීමට අවශ්‍ය වන businessEmail සහ grandTotal යන දත්ත ද Intent එකෙන් කියවා ගන්න
        String businessEmail = intent.getStringExtra("businessEmail");
        double grandTotal = intent.getDoubleExtra("grandTotal", 0.0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "scheduled_invoice_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Invoice Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 1. දුරකථනයට සාමාන්‍ය Notification එක හැමවිටම යයි
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Upcoming Invoice Due Tomorrow")
                .setContentText("Invoice for " + clientName + " is due on " + dueDate)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());

        // 2. 💡 පරිශීලකයා ඇක්සෙප්ට් කර ඇත්නම් සහ ඊමේල් ලිපිනයක් පවතී නම් පසුබිමෙන් ඊමේල් එක යවයි!
        if (isEmailReminderEnabled && businessEmail != null && !businessEmail.isEmpty()) {
            // ✉️ අප සාදාගත් EmailSender Backend එක ක්‍රියාත්මක කිරීම
            InvoiceEmailSender.sendInvoiceEmail(businessEmail, clientName, dueDate, grandTotal);
        }
    }
}