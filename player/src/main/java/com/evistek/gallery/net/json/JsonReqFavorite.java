package com.evistek.gallery.net.json;

/**
 * Created by evis on 2016/8/11.
 */

public class JsonReqFavorite extends JsonReqBase{
    private int userId;
    private String version;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
