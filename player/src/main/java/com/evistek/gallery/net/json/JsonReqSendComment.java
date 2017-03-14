package com.evistek.gallery.net.json;

public class JsonReqSendComment extends JsonReqBase {
    private int userId;
    private int contentId;
    private String comment;
    private String version;

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getContentId() {
        return contentId;
    }
    public void setContentId(int contentId) {
        this.contentId = contentId;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
