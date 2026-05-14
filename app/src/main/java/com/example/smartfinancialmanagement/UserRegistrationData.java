package com.example.smartfinancialmanagement;

public class UserRegistrationData {
    private static UserRegistrationData instance;
    //public String loanAmount;
    public String monthlyInstallment;
    public String monthsPaid;
    public String paymentMethod;

    // --- Personal Details ---
    public String fullName, email, mobile, password, age;

    // --- Loan Details ---
    public boolean hasLoan = false;
    public String loanAmount, loanType;

    // --- Saving Details ---
    public boolean hasSavingPlan = false;
    public String goalName, targetAmount, targetDate, currentSavings, monthlySavingAmount;

    // --- Terms & Updates ---
    public boolean isTermsAccepted = false;
    public boolean receiveUpdates = false;

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

    // clear all data
    public void clearData() { // reset වෙනුවට clearData නම දැම්මා
        instance = new UserRegistrationData();
    }
}