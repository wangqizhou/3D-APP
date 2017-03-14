package com.evistek.gallery.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class ImageCache {
    private static final String TAG = "ImageCache";
    // Default memory cache size in KB
    private static final int DEFAULT_MEM_CACHE_SIZE = 24 * 1024; //24MB

    private LruCache<String, Bitmap> mMemoryCache;

    public ImageCache(int cacheSizeInKB) {
        int size = DEFAULT_MEM_CACHE_SIZE;
        if (cacheSizeInKB > 0) {
            size = cacheSizeInKB;
        }

        mMemoryCache = new LruCache<String, Bitmap>(size) {
            /**
             * Measure item size in kilobytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                final int bitmapSize = bitmap.getByteCount() / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }
        };
    }

    public void addBitmapToCache(String path, Bitmap bitmap) {
        if (path == null || bitmap == null)
            return;

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(path) == null) {
            mMemoryCache.put(path, bitmap);
            Log.i(TAG, "addBitmapToCache path:" + path
                    + " size: " + bitmap.getByteCount() / 1024 + "KB");
        }
    }

    public Bitmap getBitmapFromCache(String path) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(path);
            if (memBitmap != null) {
                Log.i(TAG, "Memory cache hit path: " +  path
                        + " size: " + memBitmap.getByteCount() / 1024 + "KB");
                return memBitmap;
            }
        }
        return null;
    }

    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }
}
