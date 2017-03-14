package com.evistek.gallery.model;

import java.io.Serializable;
import java.util.Date;

public class Image implements Serializable {

    private static final long serialVersionUID = 2083520276030936646L;
    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private Date createTime;
    private Date updateTime;
    private String format;
    private int height;
    private int width;
    private long size;
    private String url;
    private String thumbnail;
    private int ownerId;
    private boolean audit;
    private int downloadCount;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isAudit() {
        return audit;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Image(){}

    public Image(String name, int categoryId, String categoryName, Date createTime
            , Date updateTime, String format, int height, int width, long size, String url
            , String thumbnail, int ownerId, boolean audit, int downloadCount) {
        super();
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.format = format;
        this.height = height;
        this.width = width;
        this.size = size;
        this.url = url;
        this.thumbnail = thumbnail;
        this.ownerId = ownerId;
        this.audit = audit;
        this.downloadCount = downloadCount;
    }

    public Image(int id, String name, int categoryId, String categoryName, Date createTime
            , Date updateTime, String format, int height, int width, long size, String url
            , String thumbnail, int ownerId, boolean audit, int downloadCount) {
        super();
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.format = format;
        this.height = height;
        this.width = width;
        this.size = size;
        this.url = url;
        this.thumbnail = thumbnail;
        this.ownerId = ownerId;
        this.audit = audit;
        this.downloadCount = downloadCount;
    }
}