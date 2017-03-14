package com.evistek.gallery.render;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.PanelConfig;
import com.evistek.gallery.utils.PanelConfig.PanelDevice;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderBase implements Renderer {

    private static final String TAG = "RenderBase";

    public static final int RENDER_MODE_2D = 1;
    public static final int RENDER_MODE_2D3D = 2;
    public static final int RENDER_MODE_3D = 3;
    public static final int RENDER_MODE_3D2D = 4;

    public static final int FRAME_PACKING_LEFT_RIGHT = 0;
    public static final int FRAME_PACKING_RIGHT_LEFT = 1;

    public static final int DRAW_MODE_WHEN_NEED = 0;
    public static final int DRAW_MODE_ALWAYS = 1;

    public static final int SIZE_TYPE_FULL_SCREEN = 0;
    public static final int SIZE_TYPE_DISPLAY_REGION = 1;

    protected Context mContext;
    protected int mRenderMode;

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected int mVideoWidth;
    protected int mVideoHeight;

    protected boolean mNeedDrawContinuously;
    protected PanelDevice mDev;

    private static OnSurfaceChangedListener mOnSurfaceChangedListener;
    public static String GL_VENDOR;
    public static String GL_RENDERER;

    @SuppressLint("NewApi")
    public RenderBase(Context context) {
        mContext = context;
        GL_VENDOR = "";
        GL_RENDERER = "";

        mDev = PanelConfig.getInstance(mContext).findDevice();
        if (mDev != null) {
            mNeedDrawContinuously = (mDev.getDrawMode() == DRAW_MODE_ALWAYS);
            ShaderBase.setFramePackingFormat(mDev.getFramePacking());

            if (mDev.getSizeType() == RenderBase.SIZE_TYPE_FULL_SCREEN) {
                Point p = new Point();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getRealSize(p);
                mScreenWidth = p.x;
                mScreenHeight = p.y;
            } else {
                mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
            }
        } else {
            mNeedDrawContinuously = true;
            ShaderBase.setFramePackingFormat(RenderBase.FRAME_PACKING_LEFT_RIGHT);
            mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        }

        ShaderBase.setScreenSize(mScreenWidth, mScreenHeight);

        mVideoWidth = mScreenWidth;
        mVideoHeight = mScreenHeight;
    }

    public int getRenderMode() {
        return mRenderMode;
    }

    public void setRenderMode(int mode) {
        if (mode == RENDER_MODE_3D || mode == RENDER_MODE_2D3D) {
            EvisUtil.enablePanel3D(true);
        } else {
            EvisUtil.enablePanel3D(false);
        }
        mRenderMode = mode;
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;

        ShaderBase.setVideoSize(width, height);
    }

    public void setDepth(float depth) {
        ShaderBase.setDepth(depth);
    }

    public float getDepth() {
        return ShaderBase.getDepth();
    }

    public float getMaxDepth() {
        return ShaderBase.getMaxDepth();
    }

    public void setKeepAR(boolean isKeepAR) {
        ShaderBase.setKeepAR(isKeepAR);
    }

    public boolean isKeepAR() {
        return ShaderBase.isKeepAR();
    }

    public void setFramePackingFormat(int format) {
        ShaderBase.setFramePackingFormat(format);
    }

    public int getFramePackingFormat() {
        return ShaderBase.getFramePackingFormat();
    }

    protected void initGLTexture() {
        ShaderBase.generateTextureID();
    }

    protected int getVideoTextureID() {
        return ShaderBase.getVideoTextureID();
    }

    protected int getImageTextureID() {
        return ShaderBase.getImageTextureID();
    }

    protected int getTemplateTextureID() {
        return ShaderBase.getTemplateTextureID();
    }

    protected ByteBuffer getTemplateBuffer() {
        return ShaderBase.getTemplateBuffer();
    }

    protected int getScreenWidth() {
        return mScreenWidth;
    }

    protected int getScreenHeight() {
        return mScreenHeight;
    }

    protected int getVideoWidth() {
        return mVideoWidth;
    }

    protected int getVideoHeight() {
        return mVideoHeight;
    }

    protected void initShader() {

    }

    protected void drawFrame() {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GL_VENDOR = gl.glGetString(GLES20.GL_VENDOR);
        GL_RENDERER = gl.glGetString(GLES20.GL_RENDERER);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mOnSurfaceChangedListener != null) {
            mOnSurfaceChangedListener.OnChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // TODO Auto-generated method stub

    }

    protected void checkGLError(String tag, String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public interface OnSurfaceChangedListener {
        public void OnChanged(int width, int height);
    }

    public static void registerOnSurfaceChangedListener(OnSurfaceChangedListener listener) {
        mOnSurfaceChangedListener = listener;
    }
}
