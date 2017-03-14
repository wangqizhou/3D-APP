package com.evistek.gallery.net;

import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.net.callback.DownloadCallback;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final String TEMP_FILE = "e3dgallerydownload.tmp";
    private Callback.Cancelable mCancelable;
    private String mDownloadDir;
    private static DownloadManager mInstance;

    private DownloadManager() {
        mCancelable = null;
        mDownloadDir = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/images/";
    }

    public synchronized static DownloadManager getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadManager();
        }

        return mInstance;
    }

    public int download(String url) {
        download(url, null);
        return 0;
    }

    public int download(String url, DownloadCallback callBack) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        download(url, mDownloadDir + fileName, true, false, callBack);
        return 0;
    }

    public int download(String url, String target, DownloadCallback callback) {
        download(url, target, true, false, callback);
        return 0;
    }

    public int download(final String url, String target, boolean autoResume, boolean autoRename,
            final DownloadCallback callBack) {
        final String actualFileName = target;

        File file = new File(actualFileName);
        if (file.exists()) {
            callBack.onResult(200, actualFileName);
        } else {

            mCancelable = download(url, mDownloadDir + TEMP_FILE, new Callback.ProgressCallback<File>() {

                @Override
                public void onSuccess(File result) {
                    File targetFile = result;
                    targetFile.renameTo(new File(actualFileName));
                    callBack.onResult(200, actualFileName);

                    NetWorkService.updatePlayCount(url);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    callBack.onResult(400, ex.getMessage());
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }

                @Override
                public void onWaiting() {

                }

                @Override
                public void onStarted() {

                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    callBack.onProgress((int) (current * 100 / total));
                }
            });
        }
        return 0;
    }

    public int pause() {
//        if (mCancelable != null && mCancelable.supportPause() && !mCancelable.isPaused()) {
//            mCancelable.pause();
//            return 0;
//        }

        return -1;
    }

    public int cancel() {
        if (mCancelable != null && !mCancelable.isCancelled()) {
            mCancelable.cancel();
            mCancelable = null;
            return 0;
        }

        return -1;
    }

    public String getDownloadDir() {
        return mDownloadDir;
    }

    private <T>Callback.Cancelable download(String uri, String savePath, Callback.CommonCallback<T> callback) {
        RequestParams requestParams = new RequestParams(uri);
        requestParams.setAutoRename(false);
        requestParams.setAutoResume(true);
        requestParams.setSaveFilePath(savePath);

        return x.http().get(requestParams, callback);
    }
}
