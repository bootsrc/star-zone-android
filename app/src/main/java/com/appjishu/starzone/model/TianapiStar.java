package com.appjishu.starzone.model;

import java.io.Serializable;
import java.util.List;

public class TianapiStar implements Serializable {
    private static final long serialVersionUID = 1940943699935431103L;
    private int code;
    private String msg;
    private List<MatchData> newslist;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<MatchData> getNewslist() {
        return newslist;
    }

    public void setNewslist(List<MatchData> newslist) {
        this.newslist = newslist;
    }
}
