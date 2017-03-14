package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.Video;

import java.util.List;

public abstract class VideoCallback {
	public abstract void onResult( int code,List<Video> JsonResp);
}
