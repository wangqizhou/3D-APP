package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.Product;

import java.util.List;

/**
 * Created by ymzhao on 2016/10/26.
 */

public abstract class AdsCallback {
    public abstract void onResult(int code, List<Product> jsonResp);
}
