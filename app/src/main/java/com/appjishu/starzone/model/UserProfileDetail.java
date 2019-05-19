package com.appjishu.starzone.model;

public class UserProfileDetail extends UserProfile {
    private int checkInCount;
    private int score;

    public int getCheckInCount() {
        return checkInCount;
    }

    public void setCheckInCount(int checkInCount) {
        this.checkInCount = checkInCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
