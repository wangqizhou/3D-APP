package com.evistek.gallery.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.model.Task;
import com.evistek.gallery.service.DownloadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Utils
{
    private static final String TAG = "Utils";
    public static final String SHARED_NAME = "e3d_gallery";

    public static final String SHARED_UNSELECTED_IMAGE_DIRS = "unselected_image_dirs";
    public static final String SHARED_IMAGE_ORDER_BY = "image_order_by";
    public static final String SHARED_IMAGE_ORDER_SORT = "image_order_sort";

    public static final String SHARED_UNSELECTED_VIDEO_DIRS = "unselected_video_dirs";
    public static final String SHARED_VIDEO_ORDER_BY = "video_order_by";
    public static final String SHARED_VIDEO_ORDER_SORT = "video_order_sort";
    public static final String SHARED_VIDEO_LAST_PLAYED = "last_palyed_id";
    public static final String SHARED_VIDEO_AUTO_NEXT = "auto_next";
    public static final String SHARED_USERID = "userId";
    public static final String SHARED_USERNAME = "userName";
    public static final String SHARED_NICKNAME = "nickName";
    public static final String SHARED_USERTYPE = "userType";
    public static final String SHARED_REGISTERTIME = "registerTime";
    public static final String SHARED_USERLEVEL = "userLevel";
    public static final String SHARED_LOCATION = "location";
    public static final String SHARED_SOURCE = "source";
    public static final String SHARED_HEAD_IMGURL = "headImgUrl";
    public static final String SHARED_VR_DEVICE = "vrDevice";
    public static final int VIDEO = 0;
    public static final int IMAGE = 1;
    public static final int PORTRAIT = 2;

    public static final String SHARED_ISFIRST = "first_launch";
    private static Context sContext = E3DApplication.getInstance().getApplicationContext();
    private static ContentResolver sContentResolver = E3DApplication.getInstance()
            .getContentResolver();

    public static void saveValue(String key, long value) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getValue(String key, long defaultValue) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        long value = shared.getLong(key, defaultValue);
        return value;
    }

    public static void saveValue(String key, int value) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getValue(String key, int defaultValue) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        int value = shared.getInt(key, defaultValue);
        return value;
    }

    public static void saveValue(String key, boolean value) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getValue(String key, boolean defaultValue) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        boolean value = shared.getBoolean(key, defaultValue);
        return value;
    }

    public static void saveValue(String key, String value) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getValue(String key, String defaultValue) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        String value = shared.getString(key, defaultValue);
        return value;
    }

    public static void saveValue(String key, List<String> value) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        StringBuilder sb = new StringBuilder();
        int size = value.size();
        for (int i = 0; i < size; ++i) {
            sb.append(value.get(i));
            sb.append(';');
        }
        editor.putString(key, sb.toString());
        editor.commit();
    }

    public static List<String> getValue(String key) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        String str = shared.getString(key, "");
        String[] dirPathes = str.split(";");
        List<String> value = new ArrayList<String>();
        int length = dirPathes.length;
        for (int i = 0; i < length; ++i) {
            value.add(dirPathes[i]);
        }
        return value;
    }

    public static void deleteValue(List<String> keyList) {
        SharedPreferences shared = sContext.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        Editor editor = shared.edit();
        for (String key: keyList) {
            editor.remove(key);
        }
        editor.commit();
    }

    public static boolean isMediaMounted(final Context context) {
        boolean mounted = false;
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mounted = true;
        }
        return mounted;
    }

    public static String stringForTime(final long millis) {
        Locale defLoc = Locale.getDefault();
        final int totalSeconds = (int) millis / 1000;
        final int seconds = totalSeconds % 60;
        final int minutes = (totalSeconds / 60) % 60;
        final int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(defLoc, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(defLoc, "%02d:%02d", minutes, seconds);
        }
    }

    public static int deleteMedia(long id, boolean isVideo) {
        String selection = BaseColumns._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        return sContentResolver.delete(isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
    }

    public static int renameMedia(String title, String name, String absPath, long id, boolean isVideo) {
        ContentValues values = new ContentValues();
        values.put(MediaColumns.TITLE, title);
        values.put(MediaColumns.DISPLAY_NAME, name);
        values.put(MediaColumns.DATA, absPath);
        String selection = BaseColumns._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(id) };
        return sContentResolver.update(isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, selection, selectionArgs);
    }

    public static boolean renameFile(String newPath, String absPath) {
        if (absPath == null) {
            return false;
        }
        File newFile = new File(newPath);
        File oldFile = new File(absPath);
        if (oldFile.isFile() && oldFile.exists()) {
            return oldFile.renameTo(newFile);
        }
        return false;
    }

    public static long getMediaId(String path, boolean isVideo) {
        final String where = MediaColumns.DATA + "=?";
        long id = 0;
        Cursor cursor = sContentResolver.query(
                isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaColumns._ID }, where, new String[] { path }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaColumns._ID);
                if (dataIndex > -1) {
                    id = cursor.getLong(dataIndex);
                }
            }
            cursor.close();
        }
        return id;
    }

    public static String getMediaPath(long id, boolean isVideo) {
        if (id < 0)
            return null;

        String data = null;
        Cursor cursor = sContentResolver.query(
                isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaColumns.DATA }, BaseColumns._ID + "=?",
                    new String[] { String.valueOf(id) }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaColumns.DATA);
                if (dataIndex > -1) {
                    data = cursor.getString(dataIndex);
                }
            }
            cursor.close();
        }

        return data;
    }

    public static String getMediaName(long id, boolean isVideo) {
        if (id < 0)
            return null;

        String data = null;
        Cursor cursor = sContentResolver.query(
                isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaColumns.DISPLAY_NAME }, BaseColumns._ID + "=?",
                new String[] { String.valueOf(id) }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
                if (dataIndex > -1) {
                    data = cursor.getString(dataIndex);
                }
            }
            cursor.close();
        }

        return data;
    }

    public static long getVideoDuration(String path) {
        final String where = MediaStore.Video.Media.DATA + "=?";
        long duration = 0;
        Cursor cursor = sContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.VideoColumns.DURATION }, where,
                new String[] { path }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION);
                if (dataIndex > -1) {
                    duration = cursor.getLong(dataIndex);
                }
            }
            cursor.close();
        }
        return duration;
    }

    public static void setBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        activity.getWindow().setAttributes(lp);
    }

    public static float getBrightness(Activity activity) {
        float activityValue = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        activityValue = lp.screenBrightness;

        if (activityValue == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            int systemValue = 0;
            try {
                systemValue = Settings.System.getInt(activity.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            activityValue = systemValue * 1.0f / 255;
        }
        return activityValue;
    }

    public static void writeObjectToFile(Object obj, String toFile) {
        File file = new File(toFile);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
        } catch (FileNotFoundException  fileNotFoundException ) {
            Log.e(TAG, "fileNotFoundException!");
            fileNotFoundException.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IO EXception!");
            e.printStackTrace();
        }
    }

    public static Object readObjectFromFile(String fromFile) {
        Object temp = null;
        File file = new File(fromFile);
        FileInputStream in;
        if (file.exists()) {
            try {
                in = new FileInputStream(file);
                ObjectInputStream objIn = new ObjectInputStream(in);
                temp = objIn.readObject();
                objIn.close();
            } catch (IOException e) {
                Log.e(TAG, "IO Exception!");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "ClassNotFoundException!");
                e.printStackTrace();
            }
        }
        return temp;
    }

    public static boolean isNetworkAvailable() {
        Context context = E3DApplication.getInstance();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.default_version);
        }
    }

    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    public static String getDeviceSystem() {
        return "android " + android.os.Build.VERSION.RELEASE;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String id = tm.getDeviceId();
            return id != null ? id : "";
        }

        return "";
    }

    public static boolean checkApkInstalled(Context context, final String url) {
        String savedInfo = Utils.getValue(url, null);
        if (savedInfo == null) {
            return false;
        } else {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo packageInfoList = pm.getPackageInfo(savedInfo, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static void deleteApkFile(final String url, final DownloadService dLService) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                String path = dLService.getDownloadDir() + fileName;
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }}).start();
    }

    public static void launchApp(Context context, String url) {
        String packageName = Utils.getValue(url, null);
        if (packageName != null) {
            PackageManager pm = E3DApplication.getInstance().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        }
    }

    public static void installApk(Context context, String remoteUrl, String localFilePath) {
        File apkfile = new File(localFilePath);
        if (!apkfile.exists()) {
            return;
        }
        // get package Name
        PackageManager pm = E3DApplication.getInstance().getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(localFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            String packageName = packageInfo.applicationInfo.packageName;
            Utils.saveValue(remoteUrl, packageName);
        }
        // using Intent to install APK
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        context.startActivity(i);
    }

    public static boolean judgeIsDownLoad (String url) {
        String rightFileName = url.substring(url.lastIndexOf("/") + 1);
        String dirName = E3DApplication.getInstance().getDownloadService().getDownloadDir();
        String appUrl = dirName + rightFileName;
        if (new File(appUrl).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean judgeIsWaitingDownload (String url, DownloadService dLService) {
        String rightFileName = url.substring(url.lastIndexOf("/") + 1);
        String dirName = E3DApplication.getInstance().getDownloadService().getDownloadDir();
        String appUrl = dirName + rightFileName + ".temp";
        if (!new File(appUrl).exists()) {
            List<Task> taskList = dLService.getTasks();
            for (int i=0; i<taskList.size() ;i++) {
                if (taskList.get(i).getUrl().equals(url) && i != 0 && taskList.get(i-1).getStatus() != Task.STATUS_COMPLETE) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean judgeIsDownloading (String url, DownloadService dLService) {
        String rightFileName = url.substring(url.lastIndexOf("/") + 1);
        String dirName = E3DApplication.getInstance().getDownloadService().getDownloadDir();
        String appUrl = dirName + rightFileName + ".temp";
        boolean flag = false;
        if (new File(appUrl).exists()) {
            flag = true;
        } else {
            List<Task> taskList = dLService.getTasks();

            for (int i=0; i<taskList.size() ;i++) {
                if (taskList.get(i).getUrl().equals(url)){
                    if(i == 0 ||(i != 0 && taskList.get(i-1).getStatus() != Task.STATUS_COMPLETE)) {
                        flag = true;
                    }
                }
            }
        }
        if (flag) {
            return true;
        } else {
            return false;
        }
    }
}
