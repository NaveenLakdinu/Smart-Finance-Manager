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

    /**
     * Schedules a local notification and optional background email reminder for 9:00 AM the day before an invoice is due.
     */
    public static void scheduleInvoiceReminder(Context context, String clientName, String dueDateString, boolean isEmailEnabled, String businessEmail, double grandTotal) {
        try {
            Date dueDate = dateFormat.parse(dueDateString);
            if (dueDate == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dueDate);
            calendar.add(Calendar.DAY_OF_YEAR, -1); // 💡 Set target to exactly 1 day prior to delivery due bounds
            calendar.set(Calendar.HOUR_OF_DAY, 9);  // 💡 Prompt at exactly 9:00 AM local time
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long triggerMillis = calendar.getTimeInMillis();

            // Only attempt registration if execution parameter window resides in the future state
            if (triggerMillis > System.currentTimeMillis()) {

                Intent intent = new Intent(context, InvoiceReminderReceiver.class);
                intent.putExtra("clientName", clientName);
                intent.putExtra("dueDate", dueDateString);
                intent.putExtra("isEmailReminderEnabled", isEmailEnabled);
                intent.putExtra("businessEmail", businessEmail);
                intent.putExtra("grandTotal", grandTotal);

                // Derive isolated deterministic identifier to prevent broad schedule overwriting crashes
                int uniqueRequestId = Math.abs((clientName + "_" + dueDateString).hashCode());

                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestId, intent, flags);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Secure checks for strict real-time wake operations on modern platforms
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

    /**
     * Clears pending broadcast actions from scheduling loops to prevent unintended routine wakeups.
     */
    public static void cancelInvoiceReminder(Context context, String clientName, String dueDateString) {
        try {
            Intent intent = new Intent(context, InvoiceReminderReceiver.class);
            int uniqueRequestId = Math.abs((clientName + "_" + dueDateString).hashCode());

            int flags = PendingIntent.FLAG_NO_CREATE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            // Look up existing intent registration signature
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestId, intent, flags);
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                pendingIntent.cancel(); // 💡 Wipe the reference completely out of OS allocation records
            }
        } catch (Exception ignored) {}
    }
}