package com.example.myapplica23.Model;

public class Follow {

    private String followedBy;
    private long followedAt;

    // FIX: Added new fields to hold the followed user's info
    private String userId; // The ID of the person you are following
    private String userName;
    private String userProfilePhoto;

    public Follow() {
        // Required empty public constructor
    }

    // --- Getters and Setters for all fields ---

    public String getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(String followedBy) {
        this.followedBy = followedBy;
    }

    public long getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(long followedAt) {
        this.followedAt = followedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfilePhoto() {
        return userProfilePhoto;
    }

    public void setUserProfilePhoto(String userProfilePhoto) {
        this.userProfilePhoto = userProfilePhoto;
    }
}