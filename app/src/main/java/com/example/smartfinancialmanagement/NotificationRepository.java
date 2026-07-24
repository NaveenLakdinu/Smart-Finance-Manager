package com.example.smartfinancialmanagement;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final String TAG = "NotificationRepo";
    private final FirebaseFirestore db;

    public interface NotificationListCallback {
        void onNotificationsLoaded(List<NotificationModel> notifications);
        void onError(Exception e);
    }

    public interface UnreadCountCallback {
        void onCountUpdated(int count);
    }

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    private CollectionReference getNotificationsRef(String studentId) {
        return db.collection("users").document(studentId).collection("notifications");
    }

    public void createNotification(NotificationModel notification) {
        CollectionReference ref = getNotificationsRef(notification.getStudentId());
        DocumentReference newDoc = ref.document();
        notification.setId(newDoc.getId());
        if (notification.getCreatedAt() == 0) {
            notification.setCreatedAt(System.currentTimeMillis());
        }
        
        newDoc.set(notification)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating notification", e));
    }

    /**
     * Prevents duplicate notifications from being created within a 24-hour window
     * for the same type and related entity.
     */
    public void checkAndCreateDuplicateSafe(NotificationModel notification) {
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        getNotificationsRef(notification.getStudentId())
                .whereEqualTo("type", notification.getType())
                .whereEqualTo("relatedEntityId", notification.getRelatedEntityId())
                .whereGreaterThan("createdAt", twentyFourHoursAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        createNotification(notification);
                    } else {
                        Log.d(TAG, "Duplicate notification prevented for type: " + notification.getType());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for duplicates", e);
                    // Create anyway in case of network issues if critical, but safer to just log
                });
    }

    public ListenerRegistration listenForNotifications(String studentId, NotificationListCallback callback) {
        return getNotificationsRef(studentId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        callback.onError(error);
                        return;
                    }

                    List<NotificationModel> notifications = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            NotificationModel notification = doc.toObject(NotificationModel.class);
                            notifications.add(notification);
                        }
                    }
                    
                    // Sort client-side descending by createdAt
                    java.util.Collections.sort(notifications, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    
                    callback.onNotificationsLoaded(notifications);
                });
    }

    public ListenerRegistration listenForUnreadCount(String studentId, UnreadCountCallback callback) {
        return getNotificationsRef(studentId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Unread count listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        int unreadCount = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            Boolean isRead = doc.getBoolean("isRead");
                            if (isRead != null && !isRead) {
                                unreadCount++;
                            }
                        }
                        callback.onCountUpdated(unreadCount);
                    }
                });
    }

    public void markAsRead(String studentId, String notificationId) {
        getNotificationsRef(studentId).document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as read", e));
    }

    public void markAllAsRead(String studentId) {
        getNotificationsRef(studentId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "All notifications marked as read"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error committing batch mark-as-read", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching unread notifications to mark as read", e));
    }
}
