package com.example.smartfinancialmanagement;

public class InvoiceModel {
    private String selectedBusiness;
    private String clientName;
    private String clientBRN;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private double grandTotal;
    private String paymentDueDate;
    private boolean emailReminderEnabled;
    private String status;
    private String businessEmail;
    private String userId; // 💡 Added for secure dashboard server-side filtering

    // Empty constructor required for Firebase Firestore data parsing
    public InvoiceModel() {}

    // Main constructor used during invoice generation
    public InvoiceModel(String selectedBusiness, String clientName, String clientBRN, String itemName,
                        int quantity, double unitPrice, double subtotal, double grandTotal,
                        String paymentDueDate, boolean emailReminderEnabled, String status, String userId) { // 💡 Added userId here
        this.selectedBusiness = selectedBusiness;
        this.clientName = clientName;
        this.clientBRN = clientBRN;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.grandTotal = grandTotal;
        this.paymentDueDate = paymentDueDate;
        this.emailReminderEnabled = emailReminderEnabled;
        this.status = status;
        this.userId = userId;
    }

    // Getters and Setters
    public String getSelectedBusiness() { return selectedBusiness; }
    public void setSelectedBusiness(String selectedBusiness) { this.selectedBusiness = selectedBusiness; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientBRN() { return clientBRN; }
    public void setClientBRN(String clientBRN) { this.clientBRN = clientBRN; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public String getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(String paymentDueDate) { this.paymentDueDate = paymentDueDate; }

    public boolean isEmailReminderEnabled() { return emailReminderEnabled; }
    public void setEmailReminderEnabled(boolean emailReminderEnabled) { this.emailReminderEnabled = emailReminderEnabled; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    // 💡 Getter and Setter for userId
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}