package com.evistek.gallery.net.json;

import java.util.ArrayList;

public class JsonReqContent extends JsonReqBase {
    private ArrayList<Integer> contentIdList;

    private String version;

    public ArrayList<Integer> getContentIdList() {
        return contentIdList;
    }

    public void setContentIdList(ArrayList<Integer> contentIdList) {
        this.contentIdList = contentIdList;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
