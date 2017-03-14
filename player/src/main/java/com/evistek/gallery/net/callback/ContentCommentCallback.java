package com.evistek.gallery.net.callback;

import com.evistek.gallery.net.json.JsonRespContentComment;

public abstract class ContentCommentCallback {
	public abstract void onResult( int code,JsonRespContentComment JsonResp);
}
