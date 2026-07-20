package com.example.smartfinancialmanagement;

public class IncomeModel {
    private String id;
    private double amount;
    private String source;
    private String date;
    private long timestamp;

    public IncomeModel() {
        // Default constructor required for calls to DataSnapshot.getValue(IncomeModel.class)
    }

    public IncomeModel(String id, double amount, String source, String date, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.source = source;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
