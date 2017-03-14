package com.evistek.gallery.activity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.evistek.gallery.database.DBManager;
import com.evistek.gallery.database.MediaDataBase;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.service.DownloadService;
import com.evistek.gallery.service.DownloadService.DownloadServiceBinder;
import com.evistek.gallery.user.User;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.PanelConfig;
import com.evistek.gallery.utils.PanelConfig.PanelDevice;

import org.xutils.x;

import java.io.File;

public class E3DApplication extends Application {
    private static final String TAG = "E3DApplication";
    private static final String INTENT_START_SERVICE = "com.evistek.gallery.ACTION_SERVICE";
    private static E3DApplication instance;
    private DBManager mManager;
    private MediaDataBase mDataBase;
    private PanelConfig mPanelConfig;
    private DownloadService mDownloadService;
    private boolean mBind;
    private Object mDownloadLock;
    private User mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mManager = DBManager.getInstance(this);
        mDataBase = MediaDataBase.getInstance(this);

        mDownloadLock = new Object();
        mPanelConfig = PanelConfig.getInstance(this);
        PanelDevice dev = mPanelConfig.findDevice();
        if (dev != null) {
            EvisUtil.rasterParamSet(dev.getNode(), dev.getCover(), dev.getCot(), dev.getOffset(), dev.isSlope());
        }

        File cacheDir = new File(Config.DISK_CACHE_PATH);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
            if (!cacheDir.exists()) {
                Log.e(TAG, "Failed to make dir: " + Config.DISK_CACHE_PATH);
            }
        }
        mBind = false;
        mUser = new User();

        if (!mBind) {
            bindDownloadService();
        }

        // init xUtils3
        x.Ext.init(this);
    }

    public static E3DApplication getInstance() {
        return instance;
    }

    public DBManager getManager() {
        return mManager;
    }

    public MediaDataBase getDataBase() {
        return mDataBase;
    }

    public PanelConfig getPanelConfig() {
        return mPanelConfig;
    }

    public DownloadService getDownloadService() {
        synchronized (mDownloadLock) {
            try {
                if (!mBind) {
                    mDownloadLock.wait(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mDownloadService;
        }
    }

    public void exit() {
        unbindDownloadService();
        System.exit(0);
    }

    public User getUser() {
        return mUser;
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (mDownloadLock) {
                DownloadServiceBinder binder = (DownloadServiceBinder) service;
                mDownloadService = binder.getService();
                mBind = true;
                mDownloadLock.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Download Service Disconnected!");
            mBind = false;
        }
    };

    private void bindDownloadService() {
        Intent intent = new Intent(INTENT_START_SERVICE);
        intent.setPackage(this.getPackageName());
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindDownloadService() {
        if (mBind) {
            mDownloadService.cancelCurrentTask();
            mDownloadService.removeDownloadNotify();
            mDownloadService.saveTask();
            unbindService(mConn);
            mBind = false;
        }
    }
}
