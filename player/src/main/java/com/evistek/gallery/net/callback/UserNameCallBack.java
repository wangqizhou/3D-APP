package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.UserModel;

public abstract class UserNameCallBack {
    public abstract void onResult(int code, UserModel JsonResp);
}
