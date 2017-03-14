package com.evistek.gallery.model;

public class ContentCount {
    private Integer count;

    private String type;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    /**
     * 无参构造
     */
    public ContentCount() {
    }

    /**
     * 带参构造
     */
    public ContentCount(Integer count, String type) {
        super();
        this.count = count;
        this.type = type;
    }
}