package com.evistek.gallery.net.callback;

import com.evistek.gallery.model.PlayRecordModel;

import java.util.List;

/**
 * Created by evis on 2017/2/17.
 */

public abstract class PlayRecordListCallback {
    public abstract void onResult(int code, List<PlayRecordModel> jsonResp);
}
