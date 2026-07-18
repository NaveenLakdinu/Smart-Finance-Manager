package com.example.smartfinancialmanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SubscriptionBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) &&
            !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("subscriptions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Subscription sub = doc.toObject(Subscription.class);
                        sub.setDocumentId(doc.getId());
                        SubscriptionNotificationScheduler.scheduleAll(
                                context, user.getUid(), doc.getId(), sub);
                    }
                });
    }
}
