package com.evistek.gallery.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageRender extends RenderBase {
    private static final String TAG = "ImageRender";

    private ShaderBase mShader2D;
    private ShaderBase mShader2D3D;
    private ShaderBase mShader3D;
    private ShaderBase mShader3D2D;

    private Bitmap mBitmapTexture;
    private boolean mUpdateTexture;

    private float mScaleFactor;
    private float mTotalScaleFactor;
    private float mCenterX;
    private float mCenterY;
    private float mXOffset;
    private float mYOffset;
    private float mTotalXOffset;
    private float mTotalYOffset;
    private boolean mUpdateScaling;
    private boolean mUpdateTranslation;

    private boolean mUpdateRenderMode;

    public ImageRender(Context context) {
        super(context);

        mShader2D = null;
        mShader2D3D = null;
        mShader3D = null;
        mShader3D2D = null;

        mBitmapTexture = null;
        mUpdateTexture = false;

        mScaleFactor = 1.0f;
        mTotalScaleFactor = 1.0f;
        mCenterX = 0.0f;
        mCenterY = 0.0f;
        mXOffset = 0.0f;
        mYOffset = 0.0f;
        mTotalXOffset = 0.0f;
        mTotalYOffset = 0.0f;

        mUpdateScaling = false;
        mUpdateTranslation = false;
        mUpdateRenderMode = false;
    }

    public float getTotalScaleFactor() {
        return mTotalScaleFactor;
    }

    private void reset() {
        switch (mRenderMode) {
        case RENDER_MODE_2D:
        default:
            if (mShader2D != null)
                mShader2D.reset();
            break;
        case RENDER_MODE_2D3D:
            if (mShader2D3D != null)
                mShader2D3D.reset();
            break;
        case RENDER_MODE_3D:
            if (mShader3D != null)
                mShader3D.reset();
            break;
        case RENDER_MODE_3D2D:
            if (mShader3D2D != null)
                mShader3D2D.reset();
            break;
        }

        mScaleFactor = 1.0f;
        mTotalScaleFactor = 1.0f;
        mCenterX = 0.0f;
        mCenterY = 0.0f;
        mXOffset = 0.0f;
        mYOffset = 0.0f;
        mTotalXOffset = 0.0f;
        mTotalYOffset = 0.0f;
    }

    synchronized public void updateTextureImage(Bitmap bitmap) {
        if (mUpdateTexture == false) {
            reset();

            mBitmapTexture = bitmap;
            mUpdateTexture = true;
        }
    }

    synchronized public void requestRender() {
        mUpdateTexture = true;
    }

    synchronized public void scale(float centerX, float centerY, float scaleFactor) {
        //debounce
        if (scaleFactor >= 1.0f && scaleFactor < 1.005f) {
            scaleFactor = 1.0f;
        }

        float tempTotalScale = mTotalScaleFactor * scaleFactor;
        if (tempTotalScale > 3.0f) {
            scaleFactor = 3.0f / mTotalScaleFactor;
        } else if (tempTotalScale < 1.0f) {
            scaleFactor = 1.0f / mTotalScaleFactor;
        }

        mCenterX = 0.0f;
        mCenterY = 0.0f;
        if (scaleFactor != 1.0f) {
            float diffX = centerX * (scaleFactor - 1);
            float diffY = centerY * (scaleFactor - 1);
            mCenterX = diffX / mScreenWidth / mTotalScaleFactor;
            mCenterY = diffY / mScreenHeight / mTotalScaleFactor;
        }
        mScaleFactor = 1 / scaleFactor;
        mTotalScaleFactor *= scaleFactor;

        mTotalXOffset += mCenterX * mScreenWidth * mTotalScaleFactor;
        mTotalYOffset += mCenterY * mScreenHeight * mTotalScaleFactor;

        mUpdateScaling = true;
    }

    synchronized public void translate(float xoffset, float yoffset) {
        if (mTotalScaleFactor == 1.0f && mTotalXOffset == 0.0f && mTotalYOffset == 0.0f) {
            mXOffset = 0.0f;
            mYOffset = 0.0f;
        } else {

            mXOffset = -xoffset / mScreenWidth / mTotalScaleFactor;
            mYOffset = -yoffset / mScreenHeight / mTotalScaleFactor;

            mTotalXOffset += xoffset;
            mTotalYOffset += yoffset;
        }

        mUpdateTranslation = true;
    }

    private void updateVertices(float centerX, float centerY, float scaleFactor) {
        switch (mRenderMode) {
        case RENDER_MODE_2D:
            mShader2D.updateVertices(centerX, centerY, scaleFactor);
            break;
        case RENDER_MODE_2D3D:
            mShader2D3D.updateVertices(centerX, centerY, scaleFactor);
            break;
        case RENDER_MODE_3D:
            mShader3D.updateVertices(centerX, centerY, scaleFactor);
            break;
        case RENDER_MODE_3D2D:
            mShader3D2D.updateVertices(centerX, centerY, scaleFactor);
            break;
        default:
            break;
        }
    }

    @Override
    protected void initGLTexture() {
        super.initGLTexture();

        //Image texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getImageTextureID());
        checkGLError(TAG, "glBindTexture mTextureID");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (mBitmapTexture != null && !mBitmapTexture.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmapTexture, 0);
           // mBitmapTexture.recycle();
        }
        checkGLError(TAG, "glTexParameter");

        //Template texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTemplateTextureID());
        checkGLError(TAG, "glBindTexture mTemptexID");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //same size as screen
        ByteBuffer templateBuffer = getTemplateBuffer();
        if (templateBuffer != null) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
                    getScreenWidth(), getScreenHeight(), 0, GLES20.GL_RGB,
                    GLES20.GL_UNSIGNED_BYTE, templateBuffer);
            checkGLError(TAG, "glTexParameter texBuffer");
        } else {
            Log.e(TAG, "Template buffer is null");
        }
    }

    @Override
    protected void initShader() {
        if (mShader2D == null)
            mShader2D = new ImageShader2D();

        if (mShader2D3D == null)
            mShader2D3D = new ImageShader2D3D();

        if (mShader3D == null)
            mShader3D = new ImageShader3D();

        if (mShader3D2D == null)
            mShader3D2D = new ImageShader3D2D();
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
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);

        initShader();
        initGLTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);

        if (mDev != null && mDev.getSizeType() == RenderBase.SIZE_TYPE_FULL_SCREEN) {
            GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
        } else {
            GLES20.glViewport(0, 0, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized(this) {
            if (mUpdateTexture) {
                mUpdateTexture = false;
                if (mBitmapTexture != null && !mBitmapTexture.isRecycled()) {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getImageTextureID());
                    GLES20.glDeleteTextures(0, new int[] { getImageTextureID() }, 0);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmapTexture, 0);
                    checkGLError(TAG, "updateTextureImage in OnDrawFrame");
                }

                if (!mNeedDrawContinuously)
                    drawFrame();
            }

            if (mUpdateScaling) {
                mUpdateScaling = false;
                updateVertices(mCenterX, mCenterY, mScaleFactor);

                if (!mNeedDrawContinuously)
                    drawFrame();
            }

            if (mUpdateTranslation) {
                mUpdateTranslation = false;
                updateVertices(mXOffset, mYOffset, 1.0f);

                if (!mNeedDrawContinuously)
                    drawFrame();
            }

            if (mUpdateRenderMode) {
                mUpdateRenderMode = false;

                if (!mNeedDrawContinuously)
                    drawFrame();
            }

            if (mNeedDrawContinuously)
                drawFrame();
        }
    }

    @Override
    public void setRenderMode(int mode) {
        super.setRenderMode(mode);
        mUpdateRenderMode = true;
    }
}
