package com.example.myapplica23.Model;

public class Comment {
    private String commentBody;
    private String commentedBy;
    private long commentedAt;

    // A no-argument constructor is REQUIRED for Firebase Realtime Database
    public Comment() {
    }

    // Getters for all fields
    public String getCommentBody() {
        return commentBody;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public long getCommentedAt() {
        return commentedAt;
    }

    // Setters for all fields
    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    public void setCommentedAt(long commentedAt) {
        this.commentedAt = commentedAt;
    }
}