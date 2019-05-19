package com.appjishu.starzone.model;

/**
 * 星座基本档案
 */
public class StarArchive {
    /**
     * 掌管宫位,第几宫 = index + 1
     */
    private int index;
    private String date;
    /**
     *
     */
    private String title;
    /**
     * 特点
     */
    private String characteristic;
    /**
     * 阴阳性
     */
    private boolean positive;
    /**
     * 最大特征
     */
    private String maxFeature;
    /**
     * 主管星
     */
    private String supervisorStar;
    /**
     * 颜色
     */
    private String color;
    /**
     * 珠宝
     */
    private String jewelry;
    /**
     * 幸运号
     */
    private int luckyNumber;
    /**
     * 金属
     */
    private String metal;
    /**
     * 描述
     */
    private String description;

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public String getMaxFeature() {
        return maxFeature;
    }

    public void setMaxFeature(String maxFeature) {
        this.maxFeature = maxFeature;
    }

    public String getSupervisorStar() {
        return supervisorStar;
    }

    public void setSupervisorStar(String supervisorStar) {
        this.supervisorStar = supervisorStar;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getJewelry() {
        return jewelry;
    }

    public void setJewelry(String jewelry) {
        this.jewelry = jewelry;
    }

    public int getLuckyNumber() {
        return luckyNumber;
    }

    public void setLuckyNumber(int luckyNumber) {
        this.luckyNumber = luckyNumber;
    }

    public String getMetal() {
        return metal;
    }

    public void setMetal(String metal) {
        this.metal = metal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "StarArchive{" +
                "index=" + index +
                ", date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", characteristic='" + characteristic + '\'' +
                ", positive=" + positive +
                ", maxFeature='" + maxFeature + '\'' +
                ", supervisorStar='" + supervisorStar + '\'' +
                ", color='" + color + '\'' +
                ", jewelry='" + jewelry + '\'' +
                ", luckyNumber=" + luckyNumber +
                ", metal='" + metal + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
