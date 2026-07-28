package com.example.smartfinancialmanagement;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification created successfully: " + newDoc.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating notification", e));
    }

    public void checkAndCreateDuplicateSafe(NotificationModel notification) {
        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        getNotificationsRef(notification.getStudentId())
                .whereEqualTo("type", notification.getType())
                .whereEqualTo("relatedEntityId", notification.getRelatedEntityId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Safe number parsing — createdAt could be Long or Double
                        Object createdAtObj = doc.get("createdAt");
                        long createdAt = 0L;
                        if (createdAtObj instanceof Number) {
                            createdAt = ((Number) createdAtObj).longValue();
                        }
                        if (createdAt > twentyFourHoursAgo) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        createNotification(notification);
                    } else {
                        Log.d(TAG, "Duplicate notification prevented for type: " + notification.getType());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for duplicates, creating anyway", e);
                    createNotification(notification);
                });
    }

    public ListenerRegistration listenForNotifications(String studentId, NotificationListCallback callback) {
        Log.d(TAG, "listenForNotifications: Starting listener for userId=" + studentId
                + " path=users/" + studentId + "/notifications");

        return getNotificationsRef(studentId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen FAILED for userId=" + studentId, error);
                        callback.onError(error);
                        return;
                    }

                    List<NotificationModel> notifications = new ArrayList<>();
                    if (value != null) {
                        Log.d(TAG, "Snapshot received: " + value.size() + " documents for userId=" + studentId);

                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                NotificationModel notification = doc.toObject(NotificationModel.class);
                                notification.setId(doc.getId()); // ensure ID is set
                                notifications.add(notification);
                                Log.d(TAG, "Parsed notification OK: id=" + doc.getId()
                                        + " title=" + notification.getTitle());
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing notification doc: " + doc.getId(), e);
                            }
                        }
                    } else {
                        Log.w(TAG, "Snapshot value is null for userId=" + studentId);
                    }
                    
                    // Sort client-side descending by createdAt
                    java.util.Collections.sort(notifications, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    
                    Log.d(TAG, "Delivering " + notifications.size() + " notifications to callback");
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
                            // Check BOTH "read" and "isRead" field names
                            Boolean readVal = doc.getBoolean("read");
                            if (readVal == null) {
                                readVal = doc.getBoolean("isRead");
                            }
                            // If neither field exists, treat as unread
                            if (readVal == null || !readVal) {
                                unreadCount++;
                            }
                        }
                        callback.onCountUpdated(unreadCount);
                    }
                });
    }

    public void markAsRead(String studentId, String notificationId) {
        // Update BOTH "read" and "isRead" to handle all naming conventions
        Map<String, Object> updates = new HashMap<>();
        updates.put("read", true);
        updates.put("isRead", true);
        
        getNotificationsRef(studentId).document(notificationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read: " + notificationId))
                .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as read", e));
    }

    public void markAllAsRead(String studentId) {
        // Fetch ALL notifications, check read status client-side for both field names
        getNotificationsRef(studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    int count = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean readVal = doc.getBoolean("read");
                        if (readVal == null) {
                            readVal = doc.getBoolean("isRead");
                        }
                        if (readVal == null || !readVal) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("read", true);
                            updates.put("isRead", true);
                            batch.update(doc.getReference(), updates);
                            count++;
                        }
                    }
                    if (count > 0) {
                        batch.commit()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "All notifications marked as read"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error committing batch mark-as-read", e));
                    } else {
                        Log.d(TAG, "No unread notifications to mark as read");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching notifications to mark as read", e));
    }
}
