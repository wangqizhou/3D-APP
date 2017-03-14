package com.evistek.gallery.net.json;

import java.util.ArrayList;

public class JsonReqUserName extends JsonReqBase {
    private ArrayList<Integer> userIdList;
    private String version;

    public ArrayList<Integer> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(ArrayList<Integer> userIdList) {
        this.userIdList = userIdList;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
