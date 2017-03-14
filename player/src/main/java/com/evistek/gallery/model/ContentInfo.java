package com.evistek.gallery.model;

import java.io.Serializable;
import java.util.Date;

public class ContentInfo implements Serializable{

    private static final long serialVersionUID = -5074071720492192071L;

    private Integer id;

    private String contentName;

    private String contentType;

    private Integer categoryId;

    private Integer source;

    private Date createTime;

    private Integer auditStatus;

    private Date updateTime;

    private String url;

    private String coverUrl;

    private Integer userId;

    private Integer downloadCount;

    private Integer height;

    private Integer width;

    private Integer size;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName == null ? null : contentName.trim();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType == null ? null : contentType.trim();
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl == null ? null : coverUrl.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * 无参构造
     */
    public ContentInfo() {
    }

    /**
     * 带参构造
     */

    public ContentInfo(String contentName, String contentType, Integer categoryId, Integer source, Date createTime,
            Integer auditStatus, Date updateTime, String url, String coverUrl, Integer userId, Integer downloadCount,
            Integer height, Integer width, Integer size) {
        super();
        this.contentName = contentName;
        this.contentType = contentType;
        this.categoryId = categoryId;
        this.source = source;
        this.createTime = createTime;
        this.auditStatus = auditStatus;
        this.updateTime = updateTime;
        this.url = url;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.downloadCount = downloadCount;
        this.height = height;
        this.width = width;
        this.size = size;
    }

    public ContentInfo(Integer id, String contentName, String contentType, Integer categoryId, Integer source,
            Date createTime, Integer auditStatus, Date updateTime, String url, String coverUrl, Integer userId,
            Integer downloadCount, Integer height, Integer width, Integer size) {
        super();
        this.id = id;
        this.contentName = contentName;
        this.contentType = contentType;
        this.categoryId = categoryId;
        this.source = source;
        this.createTime = createTime;
        this.auditStatus = auditStatus;
        this.updateTime = updateTime;
        this.url = url;
        this.coverUrl = coverUrl;
        this.userId = userId;
        this.downloadCount = downloadCount;
        this.height = height;
        this.width = width;
        this.size = size;
    }
}