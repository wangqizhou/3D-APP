package com.evistek.gallery.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRender extends RenderBase implements OnFrameAvailableListener{

    private static final String TAG = "VideoRender";

    private ShaderBase mShader2D;
    private ShaderBase mShader2D3D;
    private ShaderBase mShader3D;
    private ShaderBase mShader3D2D;

    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaPlayer;

    private boolean mUpdateSurface;

    public VideoRender(Context context) {
        super(context);

        mShader2D = null;
        mShader2D3D = null;
        mShader3D = null;
        mShader3D2D = null;

        mSurfaceTexture = null;
        mMediaPlayer = null;

        mUpdateSurface = false;
    }

    public void setMediaPlayer(MediaPlayer player) {
        mMediaPlayer = player;
    }

    synchronized public void requestRender() {
        mUpdateSurface = true;
    }

    @Override
    protected void drawFrame() {
        switch(mRenderMode) {
        case RENDER_MODE_2D:
            if (mShader2D != null)
                mShader2D.draw();
            break;
        case RENDER_MODE_2D3D:
            if (mShader2D3D != null)
                mShader2D3D.draw();
            break;
        case RENDER_MODE_3D:
            if (mShader3D != null)
                mShader3D.draw();
            break;
        case RENDER_MODE_3D2D:
            if (mShader3D2D != null)
                mShader3D2D.draw();
            break;
        default:
            Log.e(TAG, "unknown render mode, use 2D mode as default");
            if (mShader2D != null)
                mShader2D.draw();
            break;
        }
    }

    @Override
    protected void initGLTexture() {
        super.initGLTexture();

        //Video Texture
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getVideoTextureID());
        checkGLError(TAG, "glBindTexture video texture");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE);
        checkGLError(TAG, "glTexParameter");

        //Template Texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTemplateTextureID());
        checkGLError(TAG, "glBindTexture template texture");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        ByteBuffer templateBuffer = getTemplateBuffer();
        if (templateBuffer != null) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
                    getScreenWidth(), getScreenHeight(), 0, GLES20.GL_RGB,
                    GLES20.GL_UNSIGNED_BYTE, templateBuffer);
            checkGLError(TAG, "glTexParameter template buffer");
        } else {
            Log.e(TAG, "Template buffer is null");
        }
    }

    @Override
    protected void initShader() {
        if (mShader2D == null) {
            mShader2D = new VideoShader2D();
        }

        if (mShader2D3D == null) {
            mShader2D3D = new VideoShader2D3D();
        }

        if (mShader3D == null) {
            mShader3D = new VideoShader3D();
        }

        if (mShader3D2D == null) {
            mShader3D2D = new VideoShader3D2D();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);

        initShader();
        initGLTexture();

        mSurfaceTexture = new SurfaceTexture(getVideoTextureID());
        mSurfaceTexture.setOnFrameAvailableListener(this);

        Surface surface = new Surface(mSurfaceTexture);
        mMediaPlayer.setSurface(surface);
        surface.release();

        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException: media player prepare failed");
            e.printStackTrace();
        }

        synchronized (this) {
            mUpdateSurface = false;
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);

        if (mDev != null && mDev.getSizeType() == RenderBase.SIZE_TYPE_FULL_SCREEN) {
           if (!isKeepAR()) {
                GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
           }
        } else {
            GLES20.glViewport(0, 0, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        synchronized (this) {
            if (mUpdateSurface) {
                mSurfaceTexture.updateTexImage();
                mUpdateSurface = false;

                if (!mNeedDrawContinuously)
                    drawFrame();
            }

            if (mNeedDrawContinuously)
                drawFrame();
        }
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mUpdateSurface = true;
    }
}
