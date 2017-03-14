package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.PlayRecordModel;

/**
 * Created by evis on 2016/8/11.
 */

public abstract class PlayRecordCallback {
    public abstract void onResult(int code, PlayRecordModel jsonResp);
}
