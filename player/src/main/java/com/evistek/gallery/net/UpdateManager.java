package com.evistek.gallery.net;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.evistek.gallery.R;
import com.evistek.gallery.utils.Utils;
import com.evistek.gallery.view.RoundProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager
{
    private static final int DOWNLOAD = 1;
    private static final int DOWNLOAD_FINISH = 2;
    HashMap<String, String> mHashMap;
    private String mSavePath;

    private boolean cancelUpdate = false;
    private Context mContext;

    private int progress;
    private RoundProgressBar mProgress;
    private boolean mVersionLimited = false;

    private Dialog mDownloadDialog;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DOWNLOAD:
                mProgress.setProgress(progress);
                break;
            case DOWNLOAD_FINISH:
                installApk();
                break;
            default:
                break;
            }
        }
    };

    public UpdateManager(Context context) {
        mContext = context;
    }

    public void checkUpdate(final boolean manual) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean update = isUpdate();
                    boolean limited = isVersionLimited();
                    if (update || limited) {
                        Looper.prepare();
                        showNoticeDialog(update, mVersionLimited);
                        Looper.loop();
                    } else if (manual) {
                        Looper.prepare();
                        showAlertDialog();
                        Looper.loop();
                    }
                } catch (NotFoundException e) {
                    if (manual) {
                        Looper.prepare();
                        showAlertDialog();
                        Looper.loop();
                    }
                } catch (IOException e) {
                    if (manual) {
                        Looper.prepare();
                        showAlertDialog();
                        Looper.loop();
                    }
                }
            }
        }).start();
    }

    private void showAlertDialog(){
        AlertDialog mdialog = new  AlertDialog.Builder(mContext)
                .setTitle(R.string.downloadDeletetip)
                .setMessage(mContext.getString(R.string.soft_update_no) + " " + Utils.getVersion(mContext))
                .setPositiveButton(R.string.ok,null)
                .show();
    }

    /**
     * check last version info to determine update or not
     *
     * @return
     */
    private boolean isUpdate() throws IOException {
        int versionCode = getVersionCode();

        URL url = new URL(Config.NET_SERVER_ADDRESS);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setConnectTimeout(Config.NET_CONNECT_TIMEOUT);
        urlConn.setReadTimeout(Config.NET_READ_TIMEOUT);
        urlConn.connect();

        InputStream inStream = urlConn.getInputStream();

        // since XML file is not big, we can use DOM to parse it
        ParseXmlService service = new ParseXmlService();
        try {
            mHashMap = service.parseXml(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mHashMap) {
            int serviceCode = Integer.valueOf(mHashMap.get("version"));
            if (serviceCode > versionCode) {
                return true;
            }
        }
        return false;
    }

    private boolean isVersionLimited() {
        int versionCode = getVersionCode();

        if (mHashMap != null) {
            String minVersionString = mHashMap.get("minVersion");
            String maxVersionString = mHashMap.get("maxVersion");
            if (minVersionString != null && maxVersionString != null) {
                int minVersion = Integer.valueOf(minVersionString);
                int maxVersion = Integer.valueOf(maxVersionString);
                if (versionCode < minVersion || versionCode > maxVersion) {
                    mVersionLimited = true;
                } else {
                    mVersionLimited = false;
                }
            } else {
                mVersionLimited = false;
            }

            //E3DApplication.getInstance().setVersionLimited(mVersionLimited);
            return mVersionLimited;
        }

        return false;
    }

    /**
     * get current version it is specified in android:versionCode of
     * AndroidManifest.xml
     *
     * @return
     */
    private int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private String getVersionName() {
        String versionName="";
        try {
            versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void showNoticeDialog(boolean update, boolean versionLimited) {
        if (versionLimited) {
            //showVersionLimitedDialog();
            showUpdateDialog(true);
        } else if (update && !versionLimited) {
            showUpdateDialog(false);
        }
    }

    /**
     * APK update dialog
     */
    private void showUpdateDialog(boolean forceUpdate) {
        String title;
        String info;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        title = mContext.getResources().getString(R.string.soft_update_title)
                + mHashMap.get("name");
        builder.setTitle(title);

        String temp = mHashMap.get("info");
        info = temp.replace("\\n", "\n");
        builder.setMessage(info);
        // builder.setMessage(R.string.soft_update_info);
        // 'update now'
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // display download dialog
                showDownloadDialog();
            }
        });

        if (forceUpdate) {
            builder.setTitle("版本升级");
            builder.setMessage("当前版本(" + getVersionName() + ")太低，请升级到最新版本("+ mHashMap.get("name") + ")");
            builder.setCancelable(false);
        } else {
            // 'update later'
            builder.setNegativeButton(R.string.soft_update_later, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showVersionLimitedDialog() {
        String title;
        String info;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        title = mContext.getResources().getString(R.string.soft_version_limited_title);
        builder.setTitle(title);

        info = mContext.getResources().getString(R.string.soft_version_limited);
        builder.setMessage(info);
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * Apk download dialog
     */
    private void showDownloadDialog() {
        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        // Add progress bar
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (RoundProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);
        // 'cancel update'
        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();

        downloadApk();
    }

    private void downloadApk() {
        // using new thread to do it.
        new downloadApkThread().start();
    }

    private class downloadApkThread extends Thread
    {
        @Override
        public void run() {
            try {
                // check SD exits & we can write to it
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mSavePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

                    // create conn to network for src file
                    URL url = new URL(mHashMap.get("url"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(Config.NET_CONNECT_TIMEOUT);
                    conn.setReadTimeout(Config.NET_READ_TIMEOUT);
                    conn.connect();

                    // file lenth
                    int length = conn.getContentLength();
                    // create input stream
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    // read & store to file
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // calc position for progress bar
                        progress = (int) (((float) count / length) * 100);
                        // udpate progress
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // done
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // store to file
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate); // 'cancelUpdate' is set if click
                                             // 'cancel' of showDownloadDialog
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDownloadDialog.dismiss();
        }
    };

    private void installApk() {
        File apkfile = new File(mSavePath, mHashMap.get("name"));
        if (!apkfile.exists()) {
            return;
        }
        // using Intent to install APK
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
