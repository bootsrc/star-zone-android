package com.razerdp.github.com.common.entity.other;


import java.io.Serializable;

/**
 * Created by liushaoming on 2017/12/20.
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 8217838211234456430L;

    public interface ServiceInfoFields {
        String TITLE = "title";
        String CONTENT = "content";
        String TIPS="tips";
    }

    private String title;
    private String content;
    private String tips;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
