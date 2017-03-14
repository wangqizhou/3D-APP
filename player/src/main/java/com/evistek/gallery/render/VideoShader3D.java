package com.evistek.gallery.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.utils.EvisUtil;
import com.evistek.gallery.utils.ShaderHelper;

public class VideoShader3D extends ShaderBase {
    private static final String TAG = "VideoShader3D";

    // Attribute constants
    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURECOORD = "aTextureCoord";

    // Attribute locations
    private final int maPositionHandle;
    private final int maTextureHandle;

    // Uniform constants
    private static final String U_FRAME_PACKING_FORMAT = "uFramePacking";
    private static final String U_TEXTURECOORD = "uTexture";
    private static final String U_TEMPLATE_TEXTURECOORD = "uTemplateTexture";

    // Uniform locations
    private final int muTextureHandle;
    private final int muTemplateTextureHandle;
    private final int muFramePackingFormatHandle;

    private final int mProgram;

    public VideoShader3D() {
        final String[] shaders = EvisUtil.get3DShader(EvisUtil.SHAER_TYPE_VIDEO);
        mProgram = ShaderHelper.buildProgramBinary(E3DApplication.getInstance().getApplicationContext(),
                    ShaderHelper.GLSL_VIDEO_3D, shaders[0],  shaders[1]);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }

        //Attribute setting
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

        //Uniform location
        muFramePackingFormatHandle = GLES20.glGetUniformLocation(mProgram, U_FRAME_PACKING_FORMAT);
        checkGLError(TAG, "glGetUniformLocation muLRHandle");
        if (muFramePackingFormatHandle == -1) {
            throw new RuntimeException("Could not get attrib location for leftright");
        }

        muTextureHandle = GLES20.glGetUniformLocation(mProgram, U_TEXTURECOORD);
        checkGLError(TAG, "glGetUniformLocation muTempTextureHandle");
        if (muTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for utempTexture");
        }

        muTemplateTextureHandle = GLES20.glGetUniformLocation(mProgram, U_TEMPLATE_TEXTURECOORD);
        checkGLError(TAG, "glGetUniformLocation muTempTextureHandle");
        if (muTemplateTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for utempTexture");
        }
    }

    @Override
    public void draw() {
        checkGLError(TAG, "onDrawFrame start");
        super.draw();

        GLES20.glUseProgram(mProgram);
        checkGLError(TAG, "glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mVideoTextureID);
        GLES20.glUniform1i(muTextureHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTemplateTextureID);
        GLES20.glUniform1i(muTemplateTextureHandle, 1);

        GLES20.glUniform1i(muFramePackingFormatHandle, mFramePackingFormat);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGLError(TAG, "glDrawArrays");

        GLES20.glFinish();
    }
}
