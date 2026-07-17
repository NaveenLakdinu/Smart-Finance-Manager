package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InvoiceReminderScheduler {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    public static void scheduleInvoiceReminder(Context context, String clientName, String dueDateString, boolean isEmailEnabled, String businessEmail, double grandTotal) {
        try {
            Date dueDate = dateFormat.parse(dueDateString);
            if (dueDate == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dueDate);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            long triggerMillis = calendar.getTimeInMillis();

            if (triggerMillis > System.currentTimeMillis()) {

                Intent intent = new Intent(context, InvoiceReminderReceiver.class);
                intent.putExtra("clientName", clientName);
                intent.putExtra("dueDate", dueDateString);
                intent.putExtra("isEmailReminderEnabled", isEmailEnabled);
                intent.putExtra("businessEmail", businessEmail);
                intent.putExtra("grandTotal", grandTotal);

                int uniqueRequestId = Math.abs((clientName + dueDateString).hashCode());

                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestId, intent, flags);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            try {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                            } catch (SecurityException e) {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                            }
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                    }
                }
            }
        } catch (Exception ignored) {}
    }


    public static void cancelInvoiceReminder(Context context, String clientName, String dueDateString) {
        try {
            Intent intent = new Intent(context, InvoiceReminderReceiver.class);
            int uniqueRequestId = Math.abs((clientName + dueDateString).hashCode());

            int flags = PendingIntent.FLAG_NO_CREATE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestId, intent, flags);
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
    }
}