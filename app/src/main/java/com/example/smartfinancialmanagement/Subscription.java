package com.example.smartfinancialmanagement;

public class Subscription {

    private String name;
    private String renewDate;
    private String status;
    private String logoType;
    private long createdAt;

    public Subscription() {
        // Firebase empty constructor
    }

    public Subscription(String name, String renewDate, String status, String logoType, long createdAt) {
        this.name = name;
        this.renewDate = renewDate;
        this.status = status;
        this.logoType = logoType;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public String getRenewDate() {
        return renewDate;
    }

    public String getStatus() {
        return status;
    }

    public String getLogoType() {
        return logoType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRenewDate(String renewDate) {
        this.renewDate = renewDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLogoType(String logoType) {
        this.logoType = logoType;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}