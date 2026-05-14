package com.example.smartfinancialmanagement;

public class UserRegistrationData {
    private static UserRegistrationData instance;
    //public String loanAmount;
    public String monthlyInstallment;
    public String monthsPaid;
    public String paymentMethod;

    // --- Personal Details ---
    public String fullName, email, mobile, password, age; // phoneNumber වෙනුවට mobile කියලා හැදුවා

    // --- Loan Details ---
    public boolean hasLoan = false;
    public String loanAmount, loanType;

    // --- Saving Details ---
    public boolean hasSavingPlan = false;
    public String goalName, targetAmount, targetDate, currentSavings, monthlySavingAmount; // monthlySavingAmount එකතු කළා

    // --- Terms & Updates ---
    public boolean isTermsAccepted = false; // agreedToTerms වෙනුවට RegisterActivity එකේ නම දැම්මා
    public boolean receiveUpdates = false;   // wantsSubUpdates වෙනුවට RegisterActivity එකේ නම දැම්මා

    // --- Subscription Notification Preferences ---
    public boolean checkEmail = false;
    public boolean checkSms = false;
    public boolean checkPush = false;
    public boolean checkReport = false;
    public boolean checkPromo = false;

    private UserRegistrationData() {} // Private constructor

    public static synchronized UserRegistrationData getInstance() {
        if (instance == null) {
            instance = new UserRegistrationData();
        }
        return instance;
    }

    // දත්ත ඔක්කොම clear කරන්න
    public void clearData() { // reset වෙනුවට clearData නම දැම්මා
        instance = new UserRegistrationData();
    }
}