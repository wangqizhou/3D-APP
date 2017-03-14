package com.evistek.gallery.net.json;

public class JsonReqPlayRecord extends JsonReqBase {

    private int userId;

    private String client;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

}
