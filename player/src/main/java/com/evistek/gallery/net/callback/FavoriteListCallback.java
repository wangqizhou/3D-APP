package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.FavoriteModel;

import java.util.List;

/**
 * Created by evis on 2017/2/17.
 */

public abstract class FavoriteListCallback {
    public abstract void onResult(int code, List<FavoriteModel> jsonResp);
}
