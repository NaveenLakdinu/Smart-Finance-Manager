package com.example.smartfinancialmanagement;

public class NotificationModel {

    private String id;
    private String studentId;
    private String type; // e.g. "budget_alert", "payment_due", "achievement"
    private String title;
    private String message;
    private String severity; // "info", "warning", "critical", "success"
    private String sourceModule; // e.g. "BudgetPlanner", "UtilityBills"
    private String relatedEntityId;
    private boolean isRead;
    private long createdAt;
    private String actionRoute; // e.g. "BudgetPlannerActivity", "SavingsPassportActivity"

    public NotificationModel() {
        // Required empty constructor for Firestore
    }

    public NotificationModel(String id, String studentId, String type, String title, String message, 
                             String severity, String sourceModule, String relatedEntityId, 
                             boolean isRead, long createdAt, String actionRoute) {
        this.id = id;
        this.studentId = studentId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.sourceModule = sourceModule;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.actionRoute = actionRoute;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getActionRoute() { return actionRoute; }
    public void setActionRoute(String actionRoute) { this.actionRoute = actionRoute; }
}
