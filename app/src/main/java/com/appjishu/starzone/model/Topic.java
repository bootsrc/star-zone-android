package com.appjishu.starzone.model;

import java.io.Serializable;

public class Topic implements Serializable {
    private static final long serialVersionUID = 2873174449029078334L;
    private long topicId;
    private String title;
    /**
     * 图片的url，不包含前缀(http://resources.appjishu.com)
     */
    private String img;
    private String introduction;
    private long createTime;
    private long updateTime;
    /**
     * 正文过敏
     */
    private String content;

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
