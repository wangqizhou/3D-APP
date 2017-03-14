package com.evistek.gallery.net.json;

import com.evistek.gallery.model.ContentInfo;

import java.util.ArrayList;

public class JsonRespContent extends JsonRespBase {
    private ArrayList<ContentInfo> contentList;

    public ArrayList<ContentInfo> getContentList() {
        return contentList;
    }

    public void setContentList(ArrayList<ContentInfo> contentList) {
        this.contentList = contentList;
    }

}
