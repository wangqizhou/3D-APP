package com.evistek.gallery.net.json;

public class JsonReqChallenge extends JsonReqBase {
    private String userName;
    private String ciphertext;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getCiphertext() {
        return ciphertext;
    }
    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
