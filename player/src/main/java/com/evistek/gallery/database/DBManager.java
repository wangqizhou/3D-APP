package com.evistek.gallery.database;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.database.MediaDataBase.MediaInfo;
import com.evistek.gallery.utils.EvisUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private static final String TAG = "DBManager";
    private static final boolean DEBUG = false;

    public static final String SAVE_PATH =
            E3DApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

    private static final Uri URI_VIDEO = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static final Uri URI_IMAGE = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String ORDER = MediaStore.MediaColumns._ID + " ASC ";
    private static String[] PROJECTION = new String[] {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
    };

    private Context mContext;
    private ContentResolver mContentResolver;
    private MediaDataBase mDB;
    private static DBManager mInstance;

    private DBManager(Context context) {
        mContext = context;

        mContentResolver = mContext.getContentResolver();
        mDB = MediaDataBase.getInstance(mContext);
    }

    public static DBManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager(context);
                }
            }

        }

        return mInstance;
    }

    public void initDataBase() {
        queryMedia(false);
    }

    public MediaDataBase getDataBase() {
        return mDB;
    }

    public void sync() {
        queryMedia(true);
    }

    private void queryMedia(boolean sync) {
        Cursor videoCursor = mContentResolver.query(URI_VIDEO, PROJECTION, null, null, ORDER);
        if (videoCursor != null) {
            addMedia(videoCursor, true, sync);
            videoCursor.close();
        }

        Cursor imageCursor = mContentResolver.query(URI_IMAGE, PROJECTION, null, null, ORDER);
        if (imageCursor != null) {
            addMedia(imageCursor, false, sync);
            imageCursor.close();
        }
    }

    private void addMedia(Cursor cursor, boolean isVideo, boolean needSync) {
        long id = 0;
        String displayName = null;
        long position = 0;
        int srcType = 0;
        int playType = 0;
        int mimeType = 0;
        Bitmap thumbnail = null;

        List<Long> mediaSet = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();

            id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

            if (needSync) {
                mediaSet.add(Long.valueOf(id));
            }

            if (needSync && mDB.isExisting(id)) {
                continue;
            } else {
                displayName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                mimeType = isVideo ? MediaDataBase.MIME_TYPE_VIDEO : MediaDataBase.MIME_TYPE_IMAGE;

                thumbnail = getThumbnail(id);
                if (thumbnail == null) {
                    srcType = MediaDataBase.SRC_TYPE_2D;
                    playType = MediaDataBase.PLAY_TYPE_2D;
                } else {
                    srcType = (EvisUtil.isSbs(thumbnail) == 1) ? MediaDataBase.SRC_TYPE_3D : MediaDataBase.SRC_TYPE_2D;
                    if (srcType == MediaDataBase.SRC_TYPE_3D) {
                        playType = MediaDataBase.PLAY_TYPE_3D;
                    } else {
                        playType = MediaDataBase.PLAY_TYPE_2D;
                    }
                }

                MediaInfo media =
                        mDB.new MediaInfo(id, displayName, position, srcType, playType, mimeType);
                mDB.add(media);
            }
        }

        if (thumbnail != null)
            thumbnail.recycle();

        if (needSync) {
            Cursor c = mDB.findAll(isVideo ? MediaDataBase.MIME_TYPE_VIDEO : MediaDataBase.MIME_TYPE_IMAGE);

            ArrayList<Long> mediaToDelete = new ArrayList<Long>();
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToNext();
                long key_id = c.getLong(c.getColumnIndexOrThrow(MediaDataBase.MEDIA_KEY_ID));
                if (!mediaSet.contains(Long.valueOf(key_id))) {
                    mediaToDelete.add(Long.valueOf(key_id));
                    if (DEBUG) {
                        Log.e(TAG, "+++++DELETE item from database+++++");
                    }
                }
            }

            if (!mediaToDelete.isEmpty()) {
                for (Long e: mediaToDelete) {
                    mDB.delete(e.longValue());
                }
            }

            c.close();
            mediaToDelete.clear();
            mediaSet.clear();
        }
    }

    private Bitmap getThumbnail(long id) {
        File file = new File(SAVE_PATH + File.separator + "thumbnail" + String.valueOf(id));
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getPath());
        }

        return null;
    }
}
