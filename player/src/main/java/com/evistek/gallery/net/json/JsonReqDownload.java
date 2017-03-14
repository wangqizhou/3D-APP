package com.evistek.gallery.net.json;

public class JsonReqDownload extends JsonReqBase {
    private int contentId;
    private String url;

    public int getContentId() {
        return contentId;
    }
    public void setContentId(int contentId) {
        this.contentId = contentId;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
