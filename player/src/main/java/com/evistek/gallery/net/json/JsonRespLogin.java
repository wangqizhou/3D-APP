package com.evistek.gallery.net.json;

public class JsonRespLogin extends JsonRespBase {
    private String challenge;

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
}