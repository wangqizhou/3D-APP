package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.Image;

import java.util.List;

public abstract class ImageCallback {
	public abstract void onResult( int code,List<Image> JsonResp);
}
