package com.example.smartfinancialmanagement;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class BudgetModel implements Parcelable {
    private String documentId;
    private String userId;
    private double monthlyAllowance;
    private double scholarship;
    private double partTimeIncome;
    private int duration;
    
    private double monthlyIncome;
    private double semesterIncome;
    private double dailyBudget;
    private double weeklyBudget;
    private double monthlyBudget;
    
    private int financialScore;
    private String healthStatus;
    
    private String insight1;
    private String insight2;
    private String insight3;
    
    private String semesterStart;
    private String semesterEnd;
    
    @ServerTimestamp
    private Date createdAt;

    public BudgetModel() {
        // Required for Firestore
    }

    protected BudgetModel(Parcel in) {
        documentId = in.readString();
        userId = in.readString();
        monthlyAllowance = in.readDouble();
        scholarship = in.readDouble();
        partTimeIncome = in.readDouble();
        duration = in.readInt();
        monthlyIncome = in.readDouble();
        semesterIncome = in.readDouble();
        dailyBudget = in.readDouble();
        weeklyBudget = in.readDouble();
        monthlyBudget = in.readDouble();
        financialScore = in.readInt();
        healthStatus = in.readString();
        insight1 = in.readString();
        insight2 = in.readString();
        insight3 = in.readString();
        semesterStart = in.readString();
        semesterEnd = in.readString();
        long tmpDate = in.readLong();
        createdAt = tmpDate == -1 ? null : new Date(tmpDate);
    }

    public static final Creator<BudgetModel> CREATOR = new Creator<BudgetModel>() {
        @Override
        public BudgetModel createFromParcel(Parcel in) {
            return new BudgetModel(in);
        }

        @Override
        public BudgetModel[] newArray(int size) {
            return new BudgetModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId);
        dest.writeString(userId);
        dest.writeDouble(monthlyAllowance);
        dest.writeDouble(scholarship);
        dest.writeDouble(partTimeIncome);
        dest.writeInt(duration);
        dest.writeDouble(monthlyIncome);
        dest.writeDouble(semesterIncome);
        dest.writeDouble(dailyBudget);
        dest.writeDouble(weeklyBudget);
        dest.writeDouble(monthlyBudget);
        dest.writeInt(financialScore);
        dest.writeString(healthStatus);
        dest.writeString(insight1);
        dest.writeString(insight2);
        dest.writeString(insight3);
        dest.writeString(semesterStart);
        dest.writeString(semesterEnd);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public double getMonthlyAllowance() { return monthlyAllowance; }
    public void setMonthlyAllowance(double monthlyAllowance) { this.monthlyAllowance = monthlyAllowance; }
    
    public double getScholarship() { return scholarship; }
    public void setScholarship(double scholarship) { this.scholarship = scholarship; }
    
    public double getPartTimeIncome() { return partTimeIncome; }
    public void setPartTimeIncome(double partTimeIncome) { this.partTimeIncome = partTimeIncome; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    
    public double getSemesterIncome() { return semesterIncome; }
    public void setSemesterIncome(double semesterIncome) { this.semesterIncome = semesterIncome; }
    
    public double getDailyBudget() { return dailyBudget; }
    public void setDailyBudget(double dailyBudget) { this.dailyBudget = dailyBudget; }
    
    public double getWeeklyBudget() { return weeklyBudget; }
    public void setWeeklyBudget(double weeklyBudget) { this.weeklyBudget = weeklyBudget; }
    
    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    
    public int getFinancialScore() { return financialScore; }
    public void setFinancialScore(int financialScore) { this.financialScore = financialScore; }
    
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    
    public String getInsight1() { return insight1; }
    public void setInsight1(String insight1) { this.insight1 = insight1; }
    
    public String getInsight2() { return insight2; }
    public void setInsight2(String insight2) { this.insight2 = insight2; }
    
    public String getInsight3() { return insight3; }
    public void setInsight3(String insight3) { this.insight3 = insight3; }
    
    public String getSemesterStart() { return semesterStart; }
    public void setSemesterStart(String semesterStart) { this.semesterStart = semesterStart; }
    
    public String getSemesterEnd() { return semesterEnd; }
    public void setSemesterEnd(String semesterEnd) { this.semesterEnd = semesterEnd; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
