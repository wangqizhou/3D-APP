package com.evistek.gallery.utils;

import android.graphics.Bitmap;

public class EvisUtil {
    public static final int SHAER_TYPE_VIDEO = 0;
    public static final int SHAER_TYPE_IMAGE = 1;

    public static native void enablePanel3D(boolean enable);
    public static native boolean isPanel3DEnabled();
    public static native void panelParamSet(boolean redFirst);
    public static native void rasterParamSet(String dev, float cover, float cot, float offset, boolean slope);
    public static native void templeteInit(byte[] tmpl, int w, int h);
    public static native int isSbs(Bitmap bmp);
    public static native String[] get2DShader(int type);
    public static native String[] get2D3DShader(int type);
    public static native String[] get3DShader(int type);
    public static native String[] get3D2DShader(int type);

    static {
        System.loadLibrary("evisutil");
    }
}