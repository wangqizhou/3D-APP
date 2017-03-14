package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.FavoriteModel;

/**
 * Created by evis on 2016/8/11.
 */

public abstract class FavoriteCallback {
    public abstract void onResult(int code, FavoriteModel jsonResp);
}
