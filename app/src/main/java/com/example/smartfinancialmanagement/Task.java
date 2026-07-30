package com.example.smartfinancialmanagement;

/**
 * Task Model Class for Firebase Firestore
 * Represents a single task in the user's task list
 */
public class Task {
    // Firestore document ID
    private String id;
    
    // Task fields
    private String title;
    private String description;
    private String priority; // "High", "Medium", "Low"
    private String status;   // "Active", "Done", "Overdue", "Pending", "In Progress", "Completed"
    private String dueDate;  // Due date as string (e.g., "15/08/2024")
    private String subtaskText;
    private int progress;    // 0-100
    private boolean isCompleted;
    
    // Additional fields for Firestore
    private long createdAt;
    private String workerEmail;
    private int subtasksCompleted;
    private int subtasksTotal;

    // Empty constructor required for Firestore
    public Task() {
    }

    // Constructor for creating new tasks
    public Task(String id, String title, String description, String priority, String status, 
                String dueLabel, String subtaskText, int progress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueLabel;
        this.subtaskText = subtaskText;
        this.progress = progress;
        this.isCompleted = status.equals("Completed");
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getDueLabel() { return dueDate; }
    public String getDueDate() { return dueDate; }
    public String getSubtaskText() { return subtaskText; }
    public int getProgress() { return progress; }
    public boolean isCompleted() { return isCompleted; }
    public long getCreatedAt() { return createdAt; }
    public String getWorkerEmail() { return workerEmail; }
    public int getSubtasksCompleted() { return subtasksCompleted; }
    public int getSubtasksTotal() { return subtasksTotal; }

    // Setters required for Firestore
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setStatus(String status) { 
        this.status = status;
        this.isCompleted = status.equals("Completed");
    }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setSubtaskText(String subtaskText) { this.subtaskText = subtaskText; }
    public void setProgress(int progress) { this.progress = progress; }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) {
            this.status = "Completed";
            this.progress = 100;
        } else {
            this.status = "In Progress";
            this.progress = Math.max(progress, 10);
        }
    }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setWorkerEmail(String workerEmail) { this.workerEmail = workerEmail; }
    public void setSubtasksCompleted(int subtasksCompleted) { this.subtasksCompleted = subtasksCompleted; }
    public void setSubtasksTotal(int subtasksTotal) { this.subtasksTotal = subtasksTotal; }
}
