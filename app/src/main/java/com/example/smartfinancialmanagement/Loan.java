package com.example.smartfinancialmanagement;

import java.io.Serializable;

/**
 * Model representing a user's Loan.
 * Supports both Reducing Balance (EMI) and Flat Rate interest types.
 */
public class Loan implements Serializable {
    private String id;
    private String loanName;
    private double principalAmount;
    private double interestRate;
    private int durationMonths;
    private double monthlyEmi;
    private long createdAt;
    // Extended fields (v2)
    private String loanType;     // Personal, Home / Mortgage, Car / Vehicle, Student / Education, Business
    private String interestType; // REDUCING_BALANCE | FLAT_RATE
    private double totalInterest;
    private double totalPayable;

    public Loan() {
        // Required for Firestore deserialization
    }

    public Loan(String id, String loanName, double principalAmount, double interestRate,
                int durationMonths, double monthlyEmi, long createdAt) {
        this.id = id;
        this.loanName = loanName;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.monthlyEmi = monthlyEmi;
        this.createdAt = createdAt;
    }

    // --- Core getters/setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLoanName() { return loanName; }
    public void setLoanName(String loanName) { this.loanName = loanName; }

    public double getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(double principalAmount) { this.principalAmount = principalAmount; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }

    public double getMonthlyEmi() { return monthlyEmi; }
    public void setMonthlyEmi(double monthlyEmi) { this.monthlyEmi = monthlyEmi; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // --- Extended getters/setters (v2) ---

    public String getLoanType() { return loanType != null ? loanType : "Personal"; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public String getInterestType() { return interestType != null ? interestType : "REDUCING_BALANCE"; }
    public void setInterestType(String interestType) { this.interestType = interestType; }

    public double getTotalInterest() { return totalInterest; }
    public void setTotalInterest(double totalInterest) { this.totalInterest = totalInterest; }

    public double getTotalPayable() { return totalPayable; }
    public void setTotalPayable(double totalPayable) { this.totalPayable = totalPayable; }

    // --- Convenience helpers ---

    public boolean isFlatRate() {
        return "FLAT_RATE".equals(interestType);
    }
}
