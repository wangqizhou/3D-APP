package com.evistek.gallery.net.json;

import com.evistek.gallery.model.User;

import java.util.ArrayList;

public class JsonRespUserName extends JsonRespBase {
    private ArrayList<User> userList;

    public ArrayList<User> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }
}
