package com.example.smartfinancialmanagement;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SubscriptionWorker extends Worker {

    public SubscriptionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Calendar today = Calendar.getInstance();
                    int todayDay = today.get(Calendar.DAY_OF_MONTH);

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subscription sub = doc.toObject(Subscription.class);
                        sub.setDocumentId(doc.getId());

                        int paymentDay = sub.getPaymentDay();
                        int daysUntil = daysUntilPayment(todayDay, paymentDay);

                        if (daysUntil >= 0 && daysUntil <= 2) {
                            String message;
                            if (daysUntil == 2) {
                                message = "Your " + sub.getName() + " payment of Rs " +
                                        String.format("%.0f", sub.getAmount()) + " is due in 2 days";
                            } else if (daysUntil == 1) {
                                message = "Your " + sub.getName() + " payment of Rs " +
                                        String.format("%.0f", sub.getAmount()) + " is due tomorrow";
                            } else {
                                message = "Your " + sub.getName() + " payment of Rs " +
                                        String.format("%.0f", sub.getAmount()) + " is due today!";
                            }

                            NotificationHelper.createNotificationChannels(context);
                            NotificationHelper.showNotification(context,
                                    sub.getName() + " Payment Reminder",
                                    message, "bill_alerts");

                            if (daysUntil == 0) {
                                advancePaymentDate(user.getUid(), doc.getId(), paymentDay);
                            }
                        }

                        SubscriptionNotificationScheduler.scheduleAll(
                                context, user.getUid(), doc.getId(), sub);
                    }
                    success[0] = true;
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return success[0] ? Result.success() : Result.retry();
    }

    private int daysUntilPayment(int todayDay, int paymentDay) {
        if (paymentDay == todayDay) return 0;
        if (paymentDay > todayDay) return paymentDay - todayDay;

        Calendar cal = Calendar.getInstance();
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return (maxDay - todayDay) + paymentDay;
    }

    private void advancePaymentDate(String userId, String docId, int paymentDay) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, paymentDay);

        String newRenewDate = String.format("%02d/%02d/%d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("subscriptions").document(docId)
                .update("renewDate", newRenewDate);
    }
}
