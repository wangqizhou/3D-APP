package com.evistek.gallery.net.callback;

public abstract class UploadCallback {
     public abstract void onResult(int code,String msg);
     public abstract void onProgress(int progress,boolean isUploading);
}
