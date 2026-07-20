package com.example.smartfinancialmanagement;

public class Task {
    private final String id;
    private final String title;
    private final String description;
    private final String priority;
    private String status;
    private final String dueLabel;
    private final String subtaskText;
    private int progress;
    private boolean isCompleted;

    public Task(String id, String title, String description, String priority, String status, String dueLabel, String subtaskText, int progress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueLabel = dueLabel;
        this.subtaskText = subtaskText;
        this.progress = progress;
        this.isCompleted = status.equals("Completed");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getDueLabel() { return dueLabel; }
    public String getSubtaskText() { return subtaskText; }
    public int getProgress() { return progress; }
    public boolean isCompleted() { return isCompleted; }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            this.status = "Completed";
            this.progress = 100;
        } else {
            this.status = "In Progress";
            this.progress = Math.max(progress, 10);
        }
    }
}
