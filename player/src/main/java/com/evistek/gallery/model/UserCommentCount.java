package com.evistek.gallery.model;

public class UserCommentCount {
    private Integer contentId;

    private Integer count;

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 无参构造
     */
    public UserCommentCount() {
    }

    /**
     * 带参构造
     */

    public UserCommentCount(Integer contentId, Integer count) {
        super();
        this.contentId = contentId;
        this.count = count;
    }
}