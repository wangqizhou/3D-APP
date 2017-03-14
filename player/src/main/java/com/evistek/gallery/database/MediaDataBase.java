package com.evistek.gallery.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MediaDataBase {
    private static final String TAG = "MediaDataBase";

    public static final String DB_NAME = "MediaDataBase.db";
    public static final int DB_VERSION = 1;
    public static final String DB_TABLE_NAME = "MediaTable";
    public static final String MEDIA_KEY_ID = "_id";
    public static final String MEDIA_KEY_DISPLAY_NAME = "display_name";//include file extension
    public static final String MEDIA_KEY_MIME_TYPE = "mime_type";
    public static final String MEDIA_KEY_POSITION = "position";
    public static final String MEDIA_KEY_SRC_TYPE = "src_type";
    public static final String MEDIA_KEY_PLAY_TYPE = "play_type";

    public static final String[] MEDIA_KEY_COLUMNS = {
            MEDIA_KEY_ID,
            MEDIA_KEY_DISPLAY_NAME,
            MEDIA_KEY_MIME_TYPE,
            MEDIA_KEY_SRC_TYPE,
            MEDIA_KEY_PLAY_TYPE,
            MEDIA_KEY_POSITION
    };

    public static final int SRC_TYPE_2D = 0;
    public static final int SRC_TYPE_3D = 1;
    public static final int PLAY_TYPE_2D = 0;
    public static final int PLAY_TYPE_3D = 1;
    public static final int MIME_TYPE_VIDEO = 0;
    public static final int MIME_TYPE_IMAGE = 1;
    public static final int MIME_TYPE_ALL = -1;

    private SQLiteDatabase mDB = null;
    private static MediaDataBase mInstance = null;

    private MediaDataBase(Context context) {
        //mDB = new DatabaseHelper(new DatabaseContext(context)).getWritableDatabase();
        mDB = new DatabaseHelper(context).getWritableDatabase();
    }

    public synchronized static MediaDataBase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MediaDataBase(context);
        }

        return mInstance;
    }

    public void add(MediaInfo media) {

        if (media == null)
            return;
        ContentValues values = new ContentValues();
        values.put(MEDIA_KEY_ID, media.getId());
        values.put(MEDIA_KEY_DISPLAY_NAME, media.getDisplayName());
        values.put(MEDIA_KEY_POSITION, media.getPosition());
        values.put(MEDIA_KEY_SRC_TYPE, media.getSrcType());
        values.put(MEDIA_KEY_PLAY_TYPE, media.getPlayType());
        values.put(MEDIA_KEY_MIME_TYPE, media.getMIMEType());

        if (isExisting(media.getId())) {
            mDB.update(DB_TABLE_NAME, values, MEDIA_KEY_ID + "=?", new String[] { String.valueOf(media.getId()) });
        } else {
            mDB.insert(DB_TABLE_NAME, null, values);
        }
    }

    public boolean isExisting(long id) {
        boolean ret = false;
        Cursor cursor = mDB.query(DB_TABLE_NAME, new String[] { MEDIA_KEY_ID },
                MEDIA_KEY_ID + "=?", new String[] { String.valueOf(id) },
                null, null, null);

        if (cursor.moveToFirst())
            ret = true;

        cursor.close();
        return ret;
    }

    public void delete(long id) {
        mDB.delete(DB_TABLE_NAME,
                MEDIA_KEY_ID + "=?", new String[] { String.valueOf(id) });
    }

    public long getPosition(long id) {
        Long position = (Long)query(id, MEDIA_KEY_POSITION);
        if (position != null) {
            return position.longValue();
        }
        return 0;
    }

    public int getSrcType(long id) {
        Long srcType = (Long)query(id, MEDIA_KEY_SRC_TYPE);
        if (srcType != null) {
            return srcType.intValue();
        }
        return SRC_TYPE_2D;
    }

    public int getPlayType(long id) {
        Long playType = (Long)query(id, MEDIA_KEY_PLAY_TYPE);
        if (playType != null) {
            return playType.intValue();
        }
        return PLAY_TYPE_2D;
    }

    public int getMIMEType(long id) {
        Long mimeType = (Long)query(id, MEDIA_KEY_MIME_TYPE);
        if (mimeType != null) {
            return mimeType.intValue();
        }
        return MIME_TYPE_VIDEO;
    }

    public String getDisplayName(long id) {
        return (String)query(id, MEDIA_KEY_DISPLAY_NAME);
    }

    public void updatePosition(long id, long position) {
        ContentValues value = new ContentValues();
        value.put(MEDIA_KEY_POSITION, position);

        mDB.update(DB_TABLE_NAME, value,
                    MEDIA_KEY_ID + "=?", new String[] { String.valueOf(id) });
    }

    public void updatePlayType(long id, int playType) {
        ContentValues value = new ContentValues();

        switch(playType) {
        case PLAY_TYPE_2D:
            value.put(MEDIA_KEY_PLAY_TYPE, PLAY_TYPE_2D);
            break;
        case PLAY_TYPE_3D:
            value.put(MEDIA_KEY_PLAY_TYPE, PLAY_TYPE_3D);
            break;
        default:
            Log.e(TAG, "Unknown play type: " + playType + ", use PLAY_TYPE_2D as default.");
            value.put(MEDIA_KEY_PLAY_TYPE, PLAY_TYPE_2D);
            break;
        }

        mDB.update(DB_TABLE_NAME, value, MEDIA_KEY_ID + "=?", new String[] { String.valueOf(id) });
    }

    public Cursor findAll(int mimeType) {
        return mDB.query(DB_TABLE_NAME, MEDIA_KEY_COLUMNS,
                MEDIA_KEY_MIME_TYPE + "=?", new String[]{String.valueOf(mimeType)},
                null, null, null);
    }

    private Object query(long id, String key) {
        Object object = null;
        String[] column = new String[] {key};
        String selection = MEDIA_KEY_ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(id) };

        Cursor cursor = mDB.query(DB_TABLE_NAME, column,
                selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(key);
            int fieldType = cursor.getType(columnIndex);
            switch (fieldType) {
            case Cursor.FIELD_TYPE_INTEGER:
                object = Long.valueOf(cursor.getLong(columnIndex));
                break;
            case Cursor.FIELD_TYPE_STRING:
                object = cursor.getString(columnIndex);
                break;
            }
        }
        cursor.close();

        return object;
    }

    public class MediaInfo {
        private long mId;
        private String mDisplayName;
        private long mPosition;
        private int mSrcType;
        private int mPlayType;
        private int mMIMEType;

        public MediaInfo (long id, String displayName, long position, int srcType, int playType, int mimeType)
        {
            mId = id;
            mDisplayName = displayName;
            mPosition = position;
            mSrcType = srcType;
            mPlayType = playType;
            mMIMEType = mimeType;
        }

        public long getId() {
            return mId;
        }

        public String getDisplayName() {
            return mDisplayName;
        }

        public long getPosition() {
            return mPosition;
        }

        public int getSrcType() {
            return mSrcType;
        }

        public int getPlayType() {
            return mPlayType;
        }

        public int getMIMEType() {
            return mMIMEType;
        }

    }

    // This context is used to change database folder to debug.
    private class DatabaseContext extends ContextWrapper {
        public DatabaseContext(Context base) {
            super(base);
        }

        /**
         * 获得数据库路径，如果不存在，则创建对象对象
         *
         * @param name
         */
        @Override
        public File getDatabasePath(String name) {
            //判断是否存在sd卡
            boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
            if (!sdExist) {//如果不存在,
                Log.e("SD卡管理：", "SD卡不存在，请加载SD卡");
                return null;
            } else {//如果存在
                //获取sd卡路径
                String dbDir = android.os.Environment.getExternalStorageDirectory().toString();
                dbDir += "/evistekdb";//数据库所在目录
                String dbPath = dbDir + "/" + name;//数据库路径
                //判断目录是否存在，不存在则创建该目录
                File dirFile = new File(dbDir);
                if (!dirFile.exists())
                    dirFile.mkdirs();

                //数据库文件是否创建成功
                boolean isFileCreateSuccess = false;
                //判断文件是否存在，不存在则创建该文件
                File dbFile = new File(dbPath);
                if (!dbFile.exists()) {
                    try {
                        isFileCreateSuccess = dbFile.createNewFile();//创建文件
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    isFileCreateSuccess = true;

                //返回数据库文件对象
                if (isFileCreateSuccess)
                    return dbFile;
                else
                    return null;
            }
        }

        /**
         * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
         *
         * @param name
         * @param mode
         * @param factory
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                                   SQLiteDatabase.CursorFactory factory) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
            return result;
        }

        /**
         * Android 4.0会调用此方法获取数据库。
         *
         * @param name
         * @param mode
         * @param factory
         * @param errorHandler
         * @see android.content.ContextWrapper#openOrCreateDatabase(java.lang.String, int,
         * android.database.sqlite.SQLiteDatabase.CursorFactory,
         * android.database.DatabaseErrorHandler)
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                                   DatabaseErrorHandler errorHandler) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
            return result;
        }
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < DB_VERSION && newVersion == DB_VERSION) {
                dropTable(db);
                createTable(db);
            }
        }

        private void dropTable(SQLiteDatabase db) {
            String sqlCmd = "DROP TABLE " + DB_TABLE_NAME;
            db.execSQL(sqlCmd);
        }

        private void createTable(SQLiteDatabase db) {
            String sqlCmd = "CREATE TABLE IF NOT EXISTS "
                            + DB_TABLE_NAME + "("
                            + MEDIA_KEY_ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE, "
                            + MEDIA_KEY_DISPLAY_NAME + " TEXT, "
                            + MEDIA_KEY_SRC_TYPE + " INTEGER, "
                            + MEDIA_KEY_PLAY_TYPE + " INTEGER, "
                            + MEDIA_KEY_MIME_TYPE + " INTEGER, "
                            + MEDIA_KEY_POSITION + " INTEGER "
                            + ")" ;
            db.execSQL(sqlCmd);
        }
    }
}
