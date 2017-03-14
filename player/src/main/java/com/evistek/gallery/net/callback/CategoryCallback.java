package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.Category;

import java.util.List;

public abstract class CategoryCallback {
	public abstract void onResult( int code,List<Category> JsonResp);
}
