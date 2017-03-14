package com.evistek.gallery.net.json;

import com.evistek.gallery.model.Application;

import java.util.ArrayList;

/**
 * Created by evis on 2016/7/29.
 */
public class JsonRespApplication extends JsonRespBase {
    private int pageNo;
    private int pageSize;
    private ArrayList<Application> results;
    private int totalCount;
    private int totalPage;

    public int getPageNo() {
        return pageNo;
    }
    public int getPageSize() {
        return pageSize;
    }
    public ArrayList<Application> getResults() {
        return results;
    }
    public int getTotalCount() {
        return totalCount;
    }
    public int getTotalPage() {
        return totalPage;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public void setResults(ArrayList<Application> results) {
        this.results = results;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
