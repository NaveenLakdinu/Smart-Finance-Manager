package com.example.smartfinancialmanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class SubscriptionNotificationScheduler {

    private static final int BASE_REQUEST_CODE = 9000;

    public static void scheduleAll(Context context, String userId, String docId, Subscription sub) {
        cancelAll(context, userId, docId);
        scheduleForMonth(context, userId, docId, sub, 0);
    }

    private static void scheduleForMonth(Context context, String userId, String docId, Subscription sub, int monthOffset) {
        Calendar cal = Calendar.getInstance();
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int paymentDay = sub.getPaymentDay();

        cal.add(Calendar.MONTH, monthOffset);
        cal.set(Calendar.DAY_OF_MONTH, paymentDay);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.before(Calendar.getInstance())) {
            return;
        }

        int daysUntilPayment = (int) ((cal.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));

        if (daysUntilPayment <= 2 && daysUntilPayment >= 0) {
            String message;
            if (daysUntilPayment == 2) {
                message = "Your " + sub.getName() + " payment of LKR " +
                        String.format("%.0f", sub.getAmount()) + " is due in 2 days";
            } else if (daysUntilPayment == 1) {
                message = "Your " + sub.getName() + " payment of LKR " +
                        String.format("%.0f", sub.getAmount()) + " is due tomorrow";
            } else {
                message = "Your " + sub.getName() + " payment of LKR " +
                        String.format("%.0f", sub.getAmount()) + " is due today!";
            }

            scheduleAlarm(context, userId, docId, sub, cal, message, monthOffset * 3 + (2 - daysUntilPayment));
        }
    }

    private static void scheduleAlarm(Context context, String userId, String docId,
                                       Subscription sub, Calendar cal, String message, int requestCodeOffset) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SubscriptionAlarmReceiver.class);
        intent.putExtra("TITLE", sub.getName() + " Payment Reminder");
        intent.putExtra("MESSAGE", message);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("DOC_ID", docId);
        intent.putExtra("SUB_NAME", sub.getName());
        intent.putExtra("AMOUNT", sub.getAmount());
        intent.putExtra("PAYMENT_DAY", sub.getPaymentDay());
        intent.putExtra("REQUEST_CODE_OFFSET", requestCodeOffset);

        int requestCode = BASE_REQUEST_CODE + docId.hashCode() + requestCodeOffset;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelAll(Context context, String userId, String docId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < 3; i++) {
            Intent intent = new Intent(context, SubscriptionAlarmReceiver.class);
            int requestCode = BASE_REQUEST_CODE + docId.hashCode() + i;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
        }
    }
}
