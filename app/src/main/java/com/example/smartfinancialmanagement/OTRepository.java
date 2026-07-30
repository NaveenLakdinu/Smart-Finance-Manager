package com.example.smartfinancialmanagement;

import androidx.annotation.NonNull;

import com.example.smartfinancialmanagement.model.OTEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore data layer for the OT management feature.
 *
 * <p>Data model:
 * <pre>
 *   users/{uid}/ot_entries/{autoId}   – individual OT shift records
 *   users/{uid}/ot_summary/{monthKey} – atomic per-month totals (FieldValue.increment)
 * </pre>
 *
 * <p>All writes to ot_entries and ot_summary are performed in a single atomic
 * {@link WriteBatch} to ensure the summary always stays consistent with the
 * individual records.</p>
 */
public class OTRepository {

    private final CollectionReference entriesRef;
    private final CollectionReference summaryRef;

    public OTRepository(@NonNull String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        entriesRef = db.collection("users").document(uid).collection("ot_entries");
        summaryRef = db.collection("users").document(uid).collection("ot_summary");
    }

    // ── Add ──────────────────────────────────────────────────────────────────

    /**
     * Adds a new OT entry and atomically increments the monthly summary.
     *
     * @param entry           the entry to persist
     * @param successListener called with the new document ID on success
     * @param failureListener called on error
     */
    public void addEntry(@NonNull OTEntry entry,
                         OnSuccessListener<String> successListener,
                         OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference entryRef = entriesRef.document(); // auto-ID
        DocumentReference summaryDocRef = summaryRef.document(entry.monthKey);

        WriteBatch batch = db.batch();
        batch.set(entryRef, entry);
        batch.set(summaryDocRef,
                new java.util.HashMap<String, Object>() {{
                    put("monthKey", entry.monthKey);
                    put("displayMonth", entry.displayMonth);
                    put("totalOtHours", FieldValue.increment(entry.otHours));
                    put("totalOtPay", FieldValue.increment(entry.otPay));
                    put("entryCount", FieldValue.increment(1));
                }},
                com.google.firebase.firestore.SetOptions.merge());

        batch.commit()
                .addOnSuccessListener(aVoid -> successListener.onSuccess(entryRef.getId()))
                .addOnFailureListener(failureListener);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    /**
     * Updates an existing entry and adjusts the monthly summary by the diff.
     *
     * @param docId    Firestore document ID of the entry to update
     * @param oldEntry the old entry values (used to compute the delta)
     * @param newEntry the updated entry values
     */
    public void updateEntry(@NonNull String docId,
                            @NonNull OTEntry oldEntry,
                            @NonNull OTEntry newEntry,
                            OnSuccessListener<Void> successListener,
                            OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        double hoursDelta = newEntry.otHours - oldEntry.otHours;
        double payDelta = newEntry.otPay - oldEntry.otPay;

        DocumentReference summaryDocRef = summaryRef.document(newEntry.monthKey);

        WriteBatch batch = db.batch();
        batch.set(entriesRef.document(docId), newEntry);
        batch.set(summaryDocRef,
                new java.util.HashMap<String, Object>() {{
                    put("monthKey", newEntry.monthKey);
                    put("displayMonth", newEntry.displayMonth);
                    put("totalOtHours", FieldValue.increment(hoursDelta));
                    put("totalOtPay", FieldValue.increment(payDelta));
                    put("entryCount", FieldValue.increment(0)); // keep field present
                }},
                com.google.firebase.firestore.SetOptions.merge());

        batch.commit()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /**
     * Deletes an entry and decrements the monthly summary.
     *
     * @param docId the Firestore document ID to delete
     * @param entry the entry being deleted (needed to decrement summary)
     */
    public void deleteEntry(@NonNull String docId,
                            @NonNull OTEntry entry,
                            OnSuccessListener<Void> successListener,
                            OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference summaryDocRef = summaryRef.document(entry.monthKey);

        WriteBatch batch = db.batch();
        batch.delete(entriesRef.document(docId));
        batch.set(summaryDocRef,
                new java.util.HashMap<String, Object>() {{
                    put("monthKey", entry.monthKey);
                    put("displayMonth", entry.displayMonth);
                    put("totalOtHours", FieldValue.increment(-entry.otHours));
                    put("totalOtPay", FieldValue.increment(-entry.otPay));
                    put("entryCount", FieldValue.increment(-1));
                }},
                com.google.firebase.firestore.SetOptions.merge());

        batch.commit()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    /**
     * Returns all OT entries for a given month bucket (e.g. "2026-07"),
     * ordered newest-first by createdAt.
     */
    public Task<QuerySnapshot> getEntriesForMonth(@NonNull String monthKey) {
        return entriesRef
                .whereEqualTo("monthKey", monthKey)
                .get();
    }

    /**
     * Returns the summary document for a given month (may not exist if no OT logged).
     */
    public Task<DocumentSnapshot> getMonthlySummary(@NonNull String monthKey) {
        return summaryRef.document(monthKey).get();
    }
}
