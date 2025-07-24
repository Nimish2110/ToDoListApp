package com.example.todolistapp;

import java.io.Serializable;

public class Task implements Serializable {
    private String text;
    private long dueDate;
    private int priority; // 0=None, 1=Low, 2=Medium, 3=High
    private String tag;   // New: "Work", "Personal", "Shopping"

    public Task(String text, long dueDate, int priority) {
        this(text, dueDate, priority, "General");
    }

    public Task(String text, long dueDate, int priority, String tag) {
        this.text = text;
        this.dueDate = dueDate;
        this.priority = priority;
        this.tag = tag;
    }

    // Getters
    public String getText() { return text; }
    public long getDueDate() { return dueDate; }
    public int getPriority() { return priority; }
    public String getTag() { return tag; }

    // Setters
    public void setText(String text) { this.text = text; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setTag(String tag) { this.tag = tag; }
}