package com.evistek.gallery.net.json;

import com.evistek.gallery.model.Image;

import java.util.ArrayList;

//import com.evistek.mediaserver.model.Image;

public class JsonRespImage extends JsonRespBase {
    private int pageNo;
    private int pageSize;
    private ArrayList<Image> results;
    private int totalCount;
    private int totalPage;

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
    public ArrayList<Image> getResults() {
        return results;
    }
    public void setResults(ArrayList<Image> results) {
        this.results = results;
    }
    public int getTotalCount() {
        return totalCount;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    public int getTotalPage() {
        return totalPage;
    }
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
