package com.example.smartfinancialmanagement;

public class SavingModel {
    private String savingId;
    private String savingTitle;
    private double targetAmount;
    private double currentAmount;
    private double monthlySavingAmount;
    private String startDate;
    private String targetDate;
    private String status;
    private long createdAt;

    public SavingModel() {
        // Default constructor required for calls to DataSnapshot.getValue(SavingModel.class)
    }

    public SavingModel(String savingId, String savingTitle, double targetAmount, double currentAmount,
                       double monthlySavingAmount, String startDate, String targetDate, String status, long createdAt) {
        this.savingId = savingId;
        this.savingTitle = savingTitle;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.monthlySavingAmount = monthlySavingAmount;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getSavingId() { return savingId; }
    public void setSavingId(String savingId) { this.savingId = savingId; }

    public String getSavingTitle() { return savingTitle; }
    public void setSavingTitle(String savingTitle) { this.savingTitle = savingTitle; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public double getMonthlySavingAmount() { return monthlySavingAmount; }
    public void setMonthlySavingAmount(double monthlySavingAmount) { this.monthlySavingAmount = monthlySavingAmount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
