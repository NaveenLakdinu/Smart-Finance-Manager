package com.example.smartfinancialmanagement;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class SmartFinanceMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token refreshed: " + token);
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Smart Finance Manager";
        String body = "You have a new notification";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().getOrDefault("title", title);
            body = remoteMessage.getData().getOrDefault("body", body);
        }

        NotificationHelper.createNotificationChannels(this);
        NotificationHelper.showFcmNotification(this, title, body);
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
    }
}
