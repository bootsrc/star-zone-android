package com.appjishu.starzone.model;

public class GifData {
    private String url;
    private boolean preloaded = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPreloaded() {
        return preloaded;
    }

    public void setPreloaded(boolean preloaded) {
        this.preloaded = preloaded;
    }
}
