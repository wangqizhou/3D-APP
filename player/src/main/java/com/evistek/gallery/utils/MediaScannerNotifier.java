package com.evistek.gallery.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

/**
 * Created by evis on 2016/8/19.
 */
public class MediaScannerNotifier implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mScanPath;
    private String mScanMimeType;

    public MediaScannerNotifier(Context context) {
        mConnection = new MediaScannerConnection(context, this);
        mScanPath = null;
        mScanMimeType = null;
    }

    public void scanFile(String path, String mimeType) {
        mScanPath = path;
        mScanMimeType = mimeType;

        mConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        if (mScanPath != null) {
            mConnection.scanFile(mScanPath, mScanMimeType);
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mConnection.disconnect();
    }
}
