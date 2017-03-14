package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.User;

public abstract class LoginCallback {
	public abstract void onResult(int code, String msg,User respLogin);
}
