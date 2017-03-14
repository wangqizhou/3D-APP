package com.evistek.gallery.net.callback;

public abstract class DownloadCallback {
    /*
     *  if success, code is 200 (SC_OK), and msg is absolute path of download file.
     *  if fail, code is exception code return from server, the msg is message return from server
     */
    public abstract void onResult(int code, String msg);

    /*
     *  return download progress in range 0 to 100
     */
    public abstract void onProgress(int progress);
}
