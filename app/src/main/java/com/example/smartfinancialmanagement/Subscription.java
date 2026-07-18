package com.example.smartfinancialmanagement;

import java.util.HashMap;
import java.util.Map;

public class Subscription {

    private String documentId;
    private String name;
    private String renewDate;
    private String status;
    private String logoType;
    private double amount;
    private int paymentDay;
    private String billingCycle;
    private long createdAt;

    public Subscription() {
    }

    public Subscription(String name, String renewDate, String status, String logoType,
                        double amount, int paymentDay, String billingCycle, long createdAt) {
        this.name = name;
        this.renewDate = renewDate;
        this.status = status;
        this.logoType = logoType;
        this.amount = amount;
        this.paymentDay = paymentDay;
        this.billingCycle = billingCycle;
        this.createdAt = createdAt;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRenewDate() { return renewDate; }
    public void setRenewDate(String renewDate) { this.renewDate = renewDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLogoType() { return logoType; }
    public void setLogoType(String logoType) { this.logoType = logoType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getPaymentDay() { return paymentDay; }
    public void setPaymentDay(int paymentDay) { this.paymentDay = paymentDay; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("renewDate", renewDate);
        map.put("status", status);
        map.put("logoType", logoType);
        map.put("amount", amount);
        map.put("paymentDay", paymentDay);
        map.put("billingCycle", billingCycle);
        map.put("createdAt", createdAt);
        return map;
    }
}
