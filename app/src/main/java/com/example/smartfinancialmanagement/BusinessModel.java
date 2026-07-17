package com.example.smartfinancialmanagement;

public class BusinessModel {
    private String businessName;
    private String businessCategory;
    private String businessPhone;
    private String businessEmail; // ව්‍යාපාරයේ නිල ඉමේල් එක (changeable)
    private String ownerEmail;    // 💡 අලුතින් එකතු කළා: අයිතිකරුගේ ස්ථාවර ඉමේල් එක

    public BusinessModel() {}

    public BusinessModel(String businessName, String businessCategory, String businessPhone, String businessEmail, String ownerEmail) {
        this.businessName = businessName;
        this.businessCategory = businessCategory;
        this.businessPhone = businessPhone;
        this.businessEmail = businessEmail;
        this.ownerEmail = ownerEmail;
    }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessCategory() { return businessCategory; }
    public void setBusinessCategory(String businessCategory) { this.businessCategory = businessCategory; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    // 💡 Getter and Setter for ownerEmail
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}