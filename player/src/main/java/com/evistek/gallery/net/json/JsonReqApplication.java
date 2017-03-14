package com.evistek.gallery.net.json;

/**
 * Created by evis on 2016/7/29.
 */
public class JsonReqApplication extends JsonReqBase {
    private int pageNo;
    private int pageSize;
    private String version;

    public int getPageNo() {
        return pageNo;
    }
    public int getPageSize() {
        return pageSize;
    }
    public String getVersion() {
        return version;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
