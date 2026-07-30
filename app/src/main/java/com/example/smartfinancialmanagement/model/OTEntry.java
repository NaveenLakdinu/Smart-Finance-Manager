package com.example.smartfinancialmanagement.model;

/**
 * Represents a single OT (overtime) entry stored in Firestore under
 * users/{uid}/ot_entries/{auto-id}.
 *
 * otPay = otHours x otRate x 1.5 (standard OT multiplier).
 * monthKey ("YYYY-MM") is used as the ot_summary document ID for fast aggregation.
 */
public class OTEntry {

    /** Firestore document ID – populated after fetch. */
    public String docId;
    public String uid;
    /** ISO date "YYYY-MM-DD" */
    public String date;
    /** Month bucket "YYYY-MM" e.g. "2026-07" */
    public String monthKey;
    /** Human-readable month e.g. "July 2026" */
    public String displayMonth;
    /** 24h time "HH:mm" */
    public String startTime;
    /** 24h time "HH:mm" */
    public String endTime;
    /** Decimal hours e.g. 3.5 */
    public double otHours;
    /** Hourly rate in LKR – snapshot at time of entry */
    public double otRate;
    /** = otHours x otRate x 1.5 */
    public double otPay;
    /** Optional free-text note */
    public String note;
    /** Epoch ms creation timestamp */
    public long createdAt;

    // Required no-arg constructor for Firestore
    public OTEntry() {}

    public OTEntry(String uid, String date, String monthKey, String displayMonth,
                   String startTime, String endTime,
                   double otHours, double otRate, String note) {
        this.uid = uid;
        this.date = date;
        this.monthKey = monthKey;
        this.displayMonth = displayMonth;
        this.startTime = startTime;
        this.endTime = endTime;
        this.otHours = otHours;
        this.otRate = otRate;
        this.otPay = Math.round(otHours * otRate * 1.5 * 100.0) / 100.0;
        this.note = (note != null) ? note : "";
        this.createdAt = System.currentTimeMillis();
    }
}
