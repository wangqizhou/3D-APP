package com.evistek.gallery.cache;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.WindowManager;

public class ImageLoader {
    private final static String TAG = "ImageLoader";
    private static final int BYTES_PER_PIXEL = 4; //ARGB8888
    private static ImageLoader mInstance;
    private ImageCache mImageCache;
    private Context mContext;
    private int mScreenWidth;
    private int mScreenHeight;

    private ImageLoader(Context context) {
        mContext = context;

        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        int memSizeKB = (int)Math.ceil(mScreenWidth * mScreenHeight * BYTES_PER_PIXEL * 2 / 1024);
        Log.i(TAG, "Allocate memory cache: "  + memSizeKB + " KB, screen: " + mScreenWidth + "x" + mScreenHeight);
        mImageCache = new ImageCache(memSizeKB);
    }

    public synchronized static ImageLoader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ImageLoader(context);
        }
        return mInstance;
    }

    public Bitmap loadImage(String path) {
        if (path == null)
            return null;

        Bitmap bitmap = mImageCache.getBitmapFromCache(path);
        if (bitmap == null) {
            bitmap = decodeSampledBitmapByPath(path, mScreenWidth, mScreenHeight);
            if (bitmap != null)
                mImageCache.addBitmapToCache(path, bitmap);
            else
                Log.e(TAG, "Could not decode bitmap, path: " + path);
        }

        return bitmap;
    }

    public Bitmap loadImage(Uri uri) {
        if (uri == null)
            return null;

        String path = getMediaPath(mContext, uri);
        return loadImage(path);
    }

    public Bitmap loadImage(long id) {
        if (id < 0)
            return null;

        String path = getMediaPath(mContext, id);
        return loadImage(path);
    }

    public void clearCache() {
        mImageCache.clearCache();
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapByPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Bitmap Factory decode file out of memory!");
            System.gc();
            bmp = null;
        }
        return bmp;
    }

    private long getMediaId(Context context, String path) {
        final String where = Images.Media.DATA + "=?";
        long id = 0;
        Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { Images.Media._ID }, where, new String[] { path }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(Images.Media._ID);
                if (dataIndex > -1) {
                    id = cursor.getLong(dataIndex);
                }
            }
            cursor.close();
        }
        return id;
    }

    private String getMediaPath(Context context, Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[] { Images.Media.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int dataIndex = cursor.getColumnIndex(Images.Media.DATA);
                    if (dataIndex > -1) {
                        data = cursor.getString(dataIndex);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private String getMediaPath(Context context, long id) {
        if (id < 0)
            return null;

        String data = null;
        Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { Images.Media.DATA }, BaseColumns._ID + "=?",
                    new String[] { String.valueOf(id) }, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(Images.Media.DATA);
                if (dataIndex > -1) {
                    data = cursor.getString(dataIndex);
                }
            }
            cursor.close();
        }

        return data;
    }
}
