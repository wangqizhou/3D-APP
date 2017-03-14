package com.evistek.gallery.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * this class is the reflect method because of the hide API, to ensure whether
 * it is a video file
 *
 * @author NingYuanbin nyb12di@126.com
 * @version 1.0 2014/12/22
 */
public class MediaReflect
{

    Class<?> mMediaFile, mMediaFileType;
    Method getFileTypeMethod, isAudioFileTypeMethod, isVideoFileTypeMethod, isImageFileTypeMethod;
    String methodName = "getBoolean";
    String getFileType = "getFileType";

    String isVideoFileType = "isVideoFileType";
    String isImageFileType = "isImageFileType";

    Field fileType;

    public MediaReflect() {
        initReflect();
    }

    public void initReflect() {
        try {
            mMediaFile = Class.forName("android.media.MediaFile");
            mMediaFileType = Class.forName("android.media.MediaFile$MediaFileType");

            fileType = mMediaFileType.getField("fileType");

            getFileTypeMethod = mMediaFile.getMethod(getFileType, String.class);

            isVideoFileTypeMethod = mMediaFile.getMethod(isVideoFileType, int.class);
            isImageFileTypeMethod = mMediaFile.getMethod(isImageFileType, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public int getMediaFileType(String path) {
        int type = 0;
        try {
            Object obj = getFileTypeMethod.invoke(mMediaFile, path);
            if (obj == null) {
                type = -1;
            } else {
                type = fileType.getInt(obj);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return type;
    }

    public boolean isVideoFile(int fileType) {
        boolean isVideoFile = false;
        try {
            isVideoFile = (Boolean) isVideoFileTypeMethod.invoke(mMediaFile, fileType);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return isVideoFile;
    }

    public boolean isImageFile(int fileType) {
        boolean isImageFile = false;
        try {
            isImageFile = (Boolean) isImageFileTypeMethod.invoke(mMediaFile, fileType);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return isImageFile;
    }
}
