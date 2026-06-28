package com.example.smartfinancialmanagement;

public class BusinessModel {
    private String businessName;
    private String businessCategory;
    private String businessPhone;
    private String businessEmail;

    // Required empty constructor for Firebase
    public BusinessModel() {}

    public BusinessModel(String businessName, String businessCategory, String businessPhone, String businessEmail) {
        this.businessName = businessName;
        this.businessCategory = businessCategory;
        this.businessPhone = businessPhone;
        this.businessEmail = businessEmail;
    }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessCategory() { return businessCategory; }
    public void setBusinessCategory(String businessCategory) { this.businessCategory = businessCategory; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
}