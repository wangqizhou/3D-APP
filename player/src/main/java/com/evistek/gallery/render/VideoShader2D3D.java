package com.evistek.gallery.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.ShaderHelper;

public class VideoShader2D3D extends ShaderBase {
    private static final String TAG = "VideoShader2D3D";

    // Attribute constants
    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURECOORD = "aTextureCoord";

    // Attribute locations
    private final int maPositionHandle;
    private final int maTextureHandle;

    // Uniform constants
    private static final String U_TEMPLATE_TEXTURE = "uTemplateTexture";
    private static final String U_K = "uK";

    // Uniform locations
    private final int muTemplateTextureHandle;
    private final int muKHandle;

    private final int mProgram;

    public VideoShader2D3D() {
        final String[] shaders = EvisUtil.get2D3DShader(EvisUtil.SHAER_TYPE_VIDEO);
        mProgram = ShaderHelper.buildProgramBinary(E3DApplication.getInstance().getApplicationContext(),
                    ShaderHelper.GLSL_VIDEO_2D3D, shaders[0], shaders[1]);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        checkGLError(TAG, "glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
          throw new RuntimeException("Could not get attrib location for aPosition");
        }

        mVerticesBuffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGLError(TAG, "glEnableVertexAttribArray maPositionHandle");
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mVerticesBuffer);
        checkGLError(TAG, "glVertexAttribPointer maPosition");

        maTextureHandle = GLES20.glGetAttribLocation(mProgram, A_TEXTURECOORD);
        checkGLError(TAG, "glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
          throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        mVerticesBuffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGLError(TAG, "glEnableVertexAttribArray maTextureHandle");
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mVerticesBuffer);
        checkGLError(TAG, "glVertexAttribPointer maTextureHandle");

        muTemplateTextureHandle = GLES20.glGetUniformLocation(mProgram, U_TEMPLATE_TEXTURE);
        checkGLError(TAG, "glGetUniformLocation muTempTextureHandle");
        if (muTemplateTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for utempTexture");
        }

        muKHandle = GLES20.glGetUniformLocation(mProgram, U_K);
        checkGLError(TAG, "glGetUniformLocation muKHandle");
        if (muKHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for muKHandle");
        }
    }

    @Override
    public void draw() {
        checkGLError(TAG, "onDrawFrame start");
        super.draw();

        GLES20.glUseProgram(mProgram);
        checkGLError(TAG, "glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getVideoTextureID());

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTemplateTextureID());
        GLES20.glUniform1i(muTemplateTextureHandle, 1);

        GLES20.glUniform1f(muKHandle, calculateK());

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGLError(TAG, "glDrawArrays");

        GLES20.glFinish();
    }

    @Override
    public void reset() {
        super.reset();
        if (maTextureHandle != -1) {
            mVerticesBuffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGLError(TAG, "glEnableVertexAttribArray maTextureHandle");
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mVerticesBuffer);
            checkGLError(TAG, "glVertexAttribPointer maTextureHandle");
        }
    }
}
