package com.appjishu.starzone.model;

import java.io.Serializable;

public class MatchData implements Serializable {
    private static final long serialVersionUID = -7260707653428972102L;
    private String title;
    private String grade;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
