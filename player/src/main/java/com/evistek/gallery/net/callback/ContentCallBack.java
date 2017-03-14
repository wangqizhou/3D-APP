package com.evistek.gallery.net.callback;

import com.evistek.gallery.net.json.JsonRespContent;

public abstract class ContentCallBack {
    public abstract void onResult(int code, JsonRespContent JsonResp);
}
