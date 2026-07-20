package com.example.smartfinancialmanagement;

import java.io.Serializable;
import java.util.Locale;

public class ExpenseClaim implements Serializable {

    private Long id;
    private String claimId;
    private String title;
    private String category;
    private double amount;
    private String expenseDate;
    private String description;
    private int receiptCount;
    private String status;
    private String rejectedReason;
    private String approvedBy;
    private String approvedDate;
    private String workerEmail;

    public ExpenseClaim() {
    }

    public ExpenseClaim(String title, String category, double amount, String expenseDate, String description, int receiptCount, String status, String workerEmail) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.receiptCount = receiptCount;
        this.status = status;
        this.workerEmail = workerEmail;
        this.claimId = "EXP-" + new java.text.SimpleDateFormat("yyyy", Locale.getDefault()).format(new java.util.Date()) + "-" + String.format(Locale.getDefault(), "%03d", (int) (Math.random() * 900) + 100);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getReceiptCount() { return receiptCount; }
    public void setReceiptCount(int receiptCount) { this.receiptCount = receiptCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovedDate() { return approvedDate; }
    public void setApprovedDate(String approvedDate) { this.approvedDate = approvedDate; }

    public String getWorkerEmail() { return workerEmail; }
    public void setWorkerEmail(String workerEmail) { this.workerEmail = workerEmail; }

    public String getFormattedAmount() {
        return String.format(Locale.getDefault(), "LKR %,.2f", amount);
    }

    public int getStatusColor() {
        if (status == null) return 0xFFB0B0B0;
        switch (status.toUpperCase()) {
            case "PENDING": return 0xFFFBBF24;
            case "APPROVED": return 0xFF34D399;
            case "REJECTED": return 0xFFF87171;
            case "DRAFT": return 0xFF60A5FA;
            default: return 0xFFB0B0B0;
        }
    }

    public int getStatusBgColor() {
        if (status == null) return 0x1AB0B0B0;
        switch (status.toUpperCase()) {
            case "PENDING": return 0x26FBBF24;
            case "APPROVED": return 0x2634D399;
            case "REJECTED": return 0x26F87171;
            case "DRAFT": return 0x2660A5FA;
            default: return 0x1AB0B0B0;
        }
    }

    public String getStatusLabel() {
        if (status == null) return "UNKNOWN";
        return status.toUpperCase();
    }

    public int getCategoryIconBg() {
        if (category == null) return 0x14FFFFFF;
        switch (category.toLowerCase()) {
            case "travel": return 0x2660A5FA;
            case "meals": return 0x26FBBF24;
            case "transport": return 0x2634D399;
            case "accommodation": return 0x26A78BFA;
            case "supplies": return 0x26FCD34D;
            default: return 0x14FFFFFF;
        }
    }

    public String getCategoryIcon() {
        if (category == null) return "📌";
        switch (category.toLowerCase()) {
            case "travel": return "✈️";
            case "meals": return "🍽️";
            case "transport": return "🚗";
            case "accommodation": return "🏨";
            case "supplies": return "📦";
            default: return "📌";
        }
    }

    public String getCategoryLabel() {
        if (category == null) return "Other";
        switch (category.toLowerCase()) {
            case "travel": return "Travel";
            case "meals": return "Meals";
            case "transport": return "Transport";
            case "accommodation": return "Accommodation";
            case "supplies": return "Supplies";
            default: return "Other";
        }
    }
}
