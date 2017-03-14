package com.evistek.gallery.net.callback;

import com.evistek.gallery.net.json.JsonRespUserName;

public abstract class UserNameListCallBack {
    public abstract void onResult(int code, JsonRespUserName JsonResp);
}
