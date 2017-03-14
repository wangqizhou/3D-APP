package com.evistek.gallery.model;

import java.util.Date;

public class UserComment {
    private Integer id;

    private Integer contentId;

    private Integer userId;

    private String content;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 无参构造
     */
    public UserComment() {
    }

    /**
     * 带参构造
     */
    public UserComment(Integer contentId, Integer userId, String content, Date createTime) {
        super();
        this.contentId = contentId;
        this.userId = userId;
        this.content = content;
        this.createTime = createTime;
    }

    public UserComment(Integer contentId, Integer userId, String content) {
        super();
        this.contentId = contentId;
        this.userId = userId;
        this.content = content;
    }
}