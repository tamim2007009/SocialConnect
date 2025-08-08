package com.example.myapplica23.Model;

import java.util.ArrayList;

public class Story {
    private String storyBy;
    private long storyAt;
    private ArrayList<UserStories> stories;

    // Add these two new fields
    private String userName;
    private String userProfilePhoto;

    public Story() {
        // Required empty public constructor for Firebase
    }

    // --- Getters and Setters for all fields ---

    public String getStoryBy() {
        return storyBy;
    }

    public void setStoryBy(String storyBy) {
        this.storyBy = storyBy;
    }

    public long getStoryAt() {
        return storyAt;
    }

    public void setStoryAt(long storyAt) {
        this.storyAt = storyAt;
    }

    public ArrayList<UserStories> getStories() {
        return stories;
    }

    public void setStories(ArrayList<UserStories> stories) {
        this.stories = stories;
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