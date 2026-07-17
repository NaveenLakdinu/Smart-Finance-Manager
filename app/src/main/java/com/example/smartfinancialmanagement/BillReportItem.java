package com.example.smartfinancialmanagement;

import java.io.Serializable;

// Serializable allows us to cleanly pass this object array through intents
public class BillReportItem implements Serializable {
    private String billId;
    private String billName;
    private String accountNo;
    private String category;
    private double amount;
    private String targetMonth;

    public BillReportItem(String billId, String billName, String accountNo, String category, double amount, String targetMonth) {
        this.billId = billId;
        this.billName = billName;
        this.accountNo = accountNo;
        this.category = category;
        this.amount = amount;
        this.targetMonth = targetMonth;
    }

    // Getters
    public String getBillId() { return billId; }
    public String getBillName() { return billName; }
    public String getAccountNo() { return accountNo; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getTargetMonth() { return targetMonth; }
}