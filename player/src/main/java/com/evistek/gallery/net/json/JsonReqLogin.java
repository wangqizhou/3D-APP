package com.evistek.gallery.net.json;

public class JsonReqLogin extends JsonReqBase {
    private String userName;
    private String version;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
