package com.example.smartfinancialmanagement;

import java.io.Serializable;

public class Loan implements Serializable {
    private String id;
    private String loanName;
    private double principalAmount;
    private double interestRate;
    private int durationMonths;
    private double monthlyEmi;
    private long createdAt;

    public Loan() {
        // Required for Firestore
    }

    public Loan(String id, String loanName, double principalAmount, double interestRate, int durationMonths, double monthlyEmi, long createdAt) {
        this.id = id;
        this.loanName = loanName;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.monthlyEmi = monthlyEmi;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public double getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(double monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
