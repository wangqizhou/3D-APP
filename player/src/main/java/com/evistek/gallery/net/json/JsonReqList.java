package com.evistek.gallery.net.json;

public class JsonReqList extends JsonReqBase {
    private int pageNo;
    private int pageSize;
    private String contentType;
    private boolean detail;
    private int categoryId;
    private String version;

    public int getPageNo() {
        return pageNo;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public boolean isDetail() {
        return detail;
    }
    public void setDetail(boolean detail) {
        this.detail = detail;
    }
    public int getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
