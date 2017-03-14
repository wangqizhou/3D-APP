package com.evistek.gallery.render;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.evistek.gallery.utils.EvisUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class ShaderBase {
    private static final String TAG = "ShaderBase";

    protected static final int FLOAT_SIZE_BYTES = 4;
    protected static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 7 * FLOAT_SIZE_BYTES;
    protected static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    protected static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    protected static final int TRIANGLE_VERTICES_DATA_TEMPLATE_UV_OFFSET = 5;
    protected static final float[] mVerticesData = {
        /* x, y, z, texture_u, texture_v, template_u, template_v*/
        -1.0F, -1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F,
         1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F,
        -1.0F,  1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
         1.0F,  1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F
    };
    protected static FloatBuffer mVerticesBuffer  = null;

    protected static byte[] mTemplateData = null;
    protected static byte[] mTempalteKeepARData = null;
    protected static ByteBuffer mTemplateBuffer = null;
    protected static ByteBuffer mTemplateKeepARBuffer = null;

    protected static int mVideoTextureID = -1;
    protected static int mImageTextureID = -1;
    protected static int mTemplateTextureID = -1;

    protected static final float DEPTH_MAX = 0.01f;
    protected static final float DEPTH_DEFAULT = 0.005f;
    protected static float mDepth;

    protected static int mFramePackingFormat;

    protected static int mScreenWidth;
    protected static int mScreenHeight;
    protected static int mVideoWidth;
    protected static int mVideoHeight;
    protected static int mViewWidth; // The actual display width on screen keeping aspect ratio.
    protected static int mViewHeight; // The actual display height on screen keeping aspect ratio.
    protected static int mXOffset;
    protected static int mYOffset;
    protected static boolean mIsKeepAR;
    protected static boolean mOldKeepAR;

    protected static float[] mScaleMatrix = new float[16];
    protected static float[] mTranslateMatrix = new float[16];

    public ShaderBase() {
        mIsKeepAR = true;
        mOldKeepAR = false;
        mDepth = DEPTH_DEFAULT;

        if (mVerticesBuffer == null) {
            mVerticesBuffer = ByteBuffer.allocateDirect(mVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mVerticesBuffer.put(mVerticesData).position(0);
        }

        if (mTemplateBuffer == null) {
            mTemplateData = new byte[mScreenWidth * mScreenHeight * 3];
            EvisUtil.templeteInit(mTemplateData, mScreenWidth, mScreenHeight);

            mTemplateBuffer = ByteBuffer.allocateDirect(mScreenWidth * mScreenHeight * 3);
            mTemplateBuffer.put(mTemplateData).position(0);
        }

        adjustAspectRatio();
      //  if (mTemplateKeepARBuffer ==  null) {
            mTempalteKeepARData = new byte[mViewWidth * mViewHeight * 3];
            for (int y = mYOffset, j = 0; y < mYOffset + mViewHeight; y++, j++) {
                for (int x = mXOffset, i = 0; x < mXOffset + mViewWidth; x++, i++) {
                    mTempalteKeepARData[(j * mViewWidth + i) * 3] = mTemplateData[(y * mScreenWidth + x) * 3];
                    mTempalteKeepARData[(j * mViewWidth + i) * 3 + 1] = mTemplateData[(y * mScreenWidth + x) * 3 + 1];
                    mTempalteKeepARData[(j * mViewWidth + i) * 3 + 2] = mTemplateData[(y * mScreenWidth + x) * 3 + 2];
                }
            }

            mTemplateKeepARBuffer = ByteBuffer.allocateDirect(mViewWidth * mViewHeight * 3);
            mTemplateKeepARBuffer.put(mTempalteKeepARData).position(0);
        //}

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mTranslateMatrix, 0);
    }

    public static float getDepth() {
        return mDepth;
    }

    public static float getMaxDepth() {
        return DEPTH_MAX;
    }

    public static void setDepth(float depth) {
        mDepth = Math.min(DEPTH_MAX, depth);
    }

    public static int getFramePackingFormat() {
        return mFramePackingFormat;
    }

    public static void setFramePackingFormat(int format) {
        Log.i(TAG, "setFramePackingFormat: "
                + (format == RenderBase.FRAME_PACKING_LEFT_RIGHT ? "Left-Right" : "Right-Left")
                + "(" + format + ")");

        mFramePackingFormat = format;
    }

    public static int getVideoTextureID() {
        return mVideoTextureID;
    }

    public static int getImageTextureID() {
        return mImageTextureID;
    }

    public static int getTemplateTextureID() {
        return mTemplateTextureID;
    }

    public static ByteBuffer getTemplateBuffer() {
        return mTemplateBuffer;
    }

    public static void setScreenSize(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    public static void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    public static void generateTextureID() {
        if (mVideoTextureID == -1 || mImageTextureID == -1 || mTemplateTextureID == -1) {
            int[] textures = new int[3];
            GLES20.glGenTextures(3, textures, 0);

            mVideoTextureID = textures[0];
            mImageTextureID = textures[1];
            mTemplateTextureID = textures[2];
        }
    }

    public static void setKeepAR(boolean isKeepAR) {
        mIsKeepAR = isKeepAR;
    }

    public static boolean isKeepAR() {
        return mIsKeepAR;
    }

    public void draw() {
        if (mIsKeepAR != mOldKeepAR) {
            mOldKeepAR = mIsKeepAR;
            if (mIsKeepAR) {
                Log.i(TAG, "keep AR View[" + mViewWidth + ", " + mViewHeight
                        + "], offset[" + mXOffset + ", " + mYOffset + "]");

                GLES20.glViewport(mXOffset, mYOffset, mViewWidth, mViewHeight);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
                        mViewWidth, mViewHeight, 0, GLES20.GL_RGB,
                        GLES20.GL_UNSIGNED_BYTE, mTemplateKeepARBuffer);
            } else {
                Log.i(TAG, "full screen View[" + mViewWidth + ", " + mViewHeight
                        + "], Screen[" + mScreenWidth + ", " + mScreenHeight + "]");

                GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
                        mScreenWidth, mScreenHeight, 0, GLES20.GL_RGB,
                        GLES20.GL_UNSIGNED_BYTE, mTemplateBuffer);
            }
        }

        //Need clear to avoid flicker on some device.
        clear();
    }

    public void reset() {
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mTranslateMatrix, 0);
    }

    private void clear() {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    protected float calculateK() {
        // F (x) = k * (x -0.5) +0.5;   ( 0 < k <= 1,  k=1, then diff = 0.   k_min = 0.8)
        // diff (k) = 0.5 - 0.5k.
        // F_left(x)   = F(x) - diff (k)
        // F_right(x) = F(x) + diff (k)
        // depth = diff (k) = 0.5 - 0.5k. So depth_max = 0.5 - 0.5k_min = 0.5 -0.4 = 0.1
        // k = 1 - 2*depth.
        return 1 - 2 * mDepth;
    }

    public void updateVertices(float centerX, float centerY, float scaleFactor) {

        int X_TRANS_INDEX = 12;
        int Y_TRANS_INDEX = 13;
        float[] originalPoint = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
        float[] rightBottomPoint = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };
        float[] translatedOriginalPoint = new float[4];
        float[] translatedRightBottomPoint = new float[4];
        float[] tempTransMatrix = new float[16];
        float[] stMatrix = new float[16];

        tempTransMatrix = Arrays.copyOf(mTranslateMatrix, mTranslateMatrix.length);
        Matrix.translateM(tempTransMatrix, 0, centerX, centerY, 0);
        Matrix.multiplyMV(translatedOriginalPoint, 0, tempTransMatrix, 0, originalPoint, 0);
        Matrix.multiplyMM(stMatrix, 0, tempTransMatrix, 0, mScaleMatrix, 0);

        // Limit original point (left-top point)
        if (translatedOriginalPoint[0] < 0.0f) {
            Matrix.translateM(mTranslateMatrix, 0, 0, centerY, 0);
            mTranslateMatrix[X_TRANS_INDEX] = 0.0f;
        }

        if (translatedOriginalPoint[1] < 0.0f) {
            Matrix.translateM(mTranslateMatrix, 0, centerX, 0, 0);
            mTranslateMatrix[Y_TRANS_INDEX] = 0.0f;
        } else if (translatedOriginalPoint[1] >= 1 - stMatrix[0]) {
            Matrix.translateM(mTranslateMatrix, 0, centerX, 0, 0);
            mTranslateMatrix[Y_TRANS_INDEX] = 1 - stMatrix[0];
        }

        if (translatedOriginalPoint[0] >= 0.0f && translatedOriginalPoint[1] >= 0.0f) {
            Matrix.translateM(mTranslateMatrix, 0, centerX, centerY, 0);
        }

        // Limit right bottom point
        if (translatedOriginalPoint[0] >= 0.0f || translatedOriginalPoint[1] <= 0.0f) {
            Matrix.multiplyMV(translatedRightBottomPoint, 0, tempTransMatrix, 0, rightBottomPoint, 0);
            if (translatedRightBottomPoint[0] > (2 - stMatrix[0])) {
                Matrix.translateM(mTranslateMatrix, 0, 0, centerY, 0);
                mTranslateMatrix[X_TRANS_INDEX] = 1 - stMatrix[0];
            }

            if (translatedRightBottomPoint[1] > (2 - stMatrix[0])) {
                Matrix.translateM(mTranslateMatrix, 0, centerX, 0, 0);
                mTranslateMatrix[Y_TRANS_INDEX] = 1 - stMatrix[0];
            }
        }

        Matrix.scaleM(mScaleMatrix, 0, scaleFactor, scaleFactor, 0);
    }

    protected void checkGLError(String tag, String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private void adjustAspectRatio() {
        int viewWidth = mScreenWidth;
        int viewHeight = mScreenHeight;
        double aspectRatio = (double) mVideoHeight / (double)mVideoWidth;

        if (viewHeight > (int) ((double)viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            mViewWidth = viewWidth;
            mViewHeight = (int) ((double)viewWidth * aspectRatio);
        } else if(viewHeight == (int) ((double)viewWidth * aspectRatio)) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;
        } else {
            // limited by short height; restrict width
            mViewWidth = (int) ((double)viewHeight / aspectRatio);
            mViewHeight = viewHeight;
        }

        mXOffset = (viewWidth - mViewWidth) / 2;
        mYOffset = (viewHeight - mViewHeight) / 2;
    }
}
