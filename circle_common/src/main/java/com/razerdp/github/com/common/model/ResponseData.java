package com.razerdp.github.com.common.model;

import java.io.Serializable;

public class ResponseData implements Serializable {
    private static final long serialVersionUID = 1447339403407144621L;
    private int code;
    private String msg;
    private Object data;

    public ResponseData() {
    }

    public static ResponseData newOK() {
        ResponseData responseData = new ResponseData();
        responseData.setCode(0);
        responseData.setMsg("OK");
        return responseData;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
