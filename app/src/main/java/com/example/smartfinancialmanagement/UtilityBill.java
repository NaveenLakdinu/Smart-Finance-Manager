package com.example.smartfinancialmanagement;

public class UtilityBill {
    private String id;
    private String billName;
    private String accountNo;   // Kept from BillModel
    private double amount;      // Kept from UtilityBill
    private String category;
    private String paymentDate; // Serves as the due date string
    private String status;
    private long createdAt;
    private String userId;      // Added for filtering by logged-in user

    // Mandated empty constructor for Firebase Firestore reflection
    public UtilityBill() {
    }

    // Complete constructor used when creating and uploading a new bill
    public UtilityBill(String billName, String accountNo, double amount, String category, String paymentDate, String status, long createdAt, String userId) {
        this.billName = billName;
        this.accountNo = accountNo;
        this.amount = amount;
        this.category = category;
        this.paymentDate = paymentDate;
        this.status = status;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBillName() { return billName; }
    public void setBillName(String billName) { this.billName = billName; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}