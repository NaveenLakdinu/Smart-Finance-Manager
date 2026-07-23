package com.example.smartfinancialmanagement;

public class UtilityBill {
    private String id;
    private String billName;
    private double amount;
    private String category;
    private String paymentDate;
    private String status;
    private long createdAt;

    public UtilityBill() {
        // Firebase empty constructor
    }

    public UtilityBill(String billName, double amount, String category, String paymentDate, String status, long createdAt) {
        this.billName = billName;
        this.amount = amount;
        this.category = category;
        this.paymentDate = paymentDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBillName() {
        return billName;
    }

    public void setBillName(String billName) {
        this.billName = billName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
