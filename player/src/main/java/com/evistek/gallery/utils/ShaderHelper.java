package com.evistek.gallery.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.evistek.gallery.render.RenderBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

public class ShaderHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "ShaderHelper";

    public static final String GLSL_FILE_NAME = "EvisGlsl.bin";
    public static final String GLSL_IMAGE_2D = "IMAGE2D";
    public static final String GLSL_IMAGE_2D3D = "IMAGE2D3D";
    public static final String GLSL_IMAGE_3D = "IMAGE3D";
    public static final String GLSL_IMAGE_3D2D = "IMAGE3D2D";
    public static final String GLSL_VIDEO_2D = "VIDEO2D";
    public static final String GLSL_VIDEO_2D3D = "VIDEO2D3D";
    public static final String GLSL_VIDEO_3D = "VIDEO3D";
    public static final String GLSL_VIDEO_3D2D = "VIDEO3D2D";

    private static final int MAX_LOCATION_INDEX = 16;
    private static int mLocationIndex = 0;
    private static HashMap<Integer, int[]> mLocationIndexs = new HashMap<Integer, int[]>();

    private static final int MAX_GLSL_FILE_SIZE = 40 * 1024; //40k
    private static EvisGlslBinary mEvisGlslBinary = new EvisGlslBinary();

    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     */
    private static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     */
    private static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private static int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        final int shaderObjectId = GLES20.glCreateShader(type);

        if (shaderObjectId == 0) {
            if (DEBUG) {
                Log.w(TAG, "Could not create new shader.");
            }

            return 0;
        }

        // Pass in the shader source.
        GLES20.glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        GLES20.glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS,
            compileStatus, 0);

        if (DEBUG) {
            // Print the shader info log to the Android log output.
            Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode
                + "\n:" + GLES20.glGetShaderInfoLog(shaderObjectId));
        }

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            GLES20.glDeleteShader(shaderObjectId);

            if (DEBUG) {
                Log.w(TAG, "Compilation of shader failed.");
            }

            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    private static int[] getAvailableLocationIndexs(int programId, String[] attributes) {
        if (mLocationIndexs.containsKey(Integer.valueOf(programId))) {
            return mLocationIndexs.get(Integer.valueOf(programId));
        } else {
            int count = attributes.length;
            int[] indexs = new int[count];
            for (int i = 0; i < count; i++) {
                indexs[i] = mLocationIndex;
                //TODO: how to determine the max index of attribute in GPU
                mLocationIndex = (++mLocationIndex) % MAX_LOCATION_INDEX;
            }
            mLocationIndexs.put(programId, indexs);
            return indexs;
        }
    }

    private static void bindAttriLocation(int program, String[] attributes) {
        if (attributes != null) {
            int[] indexs = getAvailableLocationIndexs(program, attributes);
            if (indexs != null) {
                for (int i = 0; i < indexs.length; i++)
                    GLES20.glBindAttribLocation(program, indexs[i], attributes[i]);
            }
        }
    }
    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    private static int linkProgram(int vertexShaderId, int fragmentShaderId, String[] attributes) {

        // Create a new program object.
        final int programObjectId = GLES20.glCreateProgram();

        if (programObjectId == 0) {
            if (DEBUG) {
                Log.w(TAG, "Could not create new program");
            }

            return 0;
        }

        // Attach the vertex shader to the program.
        GLES20.glAttachShader(programObjectId, vertexShaderId);

        // Attach the fragment shader to the program.
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        //bindAttriLocation
        bindAttriLocation(programObjectId, attributes);

        // Link the two shaders together into a program.
        GLES20.glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS,
            linkStatus, 0);

        if (DEBUG) {
            // Print the program info log to the Android log output.
            Log.v(
                TAG,
                "Results of linking program:\n"
                    + GLES20.glGetProgramInfoLog(programObjectId));
        }

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            GLES20.glDeleteProgram(programObjectId);

            if (DEBUG) {
                Log.w(TAG, "Linking of program failed.");
            }

            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS,
            validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0]
            + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    /**
     * Helper function that compiles the shaders, links and validates the
     * program, returning the program ID.
     */
    public static int buildProgram(String vertexShaderSource,
        String fragmentShaderSource) {
        int program;

        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader, null);

        if (DEBUG) {
            validateProgram(program);
        }

        return program;
    }

    public static int buildProgram(String vertexShaderSource,
            String fragmentShaderSource, String[] attributes) {
        int program;

        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader, attributes);

        if (DEBUG) {
                validateProgram(program);
        }

        return program;
    }

    public static int buildProgramBinary(Context context, String name,
                        String vertexShaderSource, String fragmentShaderSource) {
        //build dummy shader script
        int dummyProgram = buildProgram(vertexShaderSource, fragmentShaderSource);
        return loadBinaryProgram(context, name, dummyProgram);
    }

    public static int buildProgramBinary(Context context, String name,
            String vertexShaderSource, String fragmentShaderSource, String[] attributes) {
        //build dummy shader script
        int dummyProgram = buildProgram(vertexShaderSource, fragmentShaderSource, attributes);
        return loadBinaryProgram(context, name, dummyProgram);
    }

    @SuppressLint("NewApi")
    private static int loadBinaryProgram(Context context, String name, int dummyProgram) {
        int binProgram = GLES20.glCreateProgram();

        IntBuffer formats = null;
        IntBuffer numFormats = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GLES20.GL_NUM_SHADER_BINARY_FORMATS, numFormats);
        if (numFormats.get(0) > 0)
        {
            formats = IntBuffer.allocate(numFormats.get(0));
        } else {
            Log.w(TAG, "GL_NUM_SHADER_BINARY_FORMATS is 0 !!");
            formats = IntBuffer.allocate(1);
        }
        GLES20.glGetIntegerv(GLES20.GL_SHADER_BINARY_FORMATS, formats);

        IntBuffer len = IntBuffer.allocate(1);
        ByteBuffer shaderbin = ByteBuffer.allocate(MAX_GLSL_FILE_SIZE);
        GLES30.glGetProgramBinary(dummyProgram, MAX_GLSL_FILE_SIZE, len, formats, shaderbin);

        openGlslBinaryFiles(context, RenderBase.GL_VENDOR, RenderBase.GL_RENDERER);
        int offset = mEvisGlslBinary.getItemOffset(name);
        int length = mEvisGlslBinary.getItemLength(name);
        ByteBuffer bbf = ByteBuffer.allocate(length);
        bbf.put(mEvisGlslBinary.getBuffer(), offset, length);
        bbf.position(0);
        GLES30.glProgramBinary(binProgram, formats.get(0), bbf, length);

        return binProgram;
    }

    private static boolean openGlslBinaryFiles(Context context, String vendor, String renderer) {
        if (mEvisGlslBinary.getItemNumber() == 0) {
            AssetManager assetManager = context.getAssets();
            try {
                String fileToOpen = GLSL_FILE_NAME;
                boolean foundFile = false;
                String[] files = assetManager.list(vendor);
                if (files.length > 0) {
                    for (String file: files) {
                        if (file.equals(renderer)) {
                            foundFile = true;
                        }
                    }

                    if (foundFile) {
                        fileToOpen = vendor + "/" + renderer + "/" + GLSL_FILE_NAME;
                    } else {
                        fileToOpen = vendor + "/" + GLSL_FILE_NAME;
                    }

                    InputStream inputStream = assetManager.open(fileToOpen);
                    mEvisGlslBinary.allocateBuffer(MAX_GLSL_FILE_SIZE);
                    inputStream.read(mEvisGlslBinary.getBuffer());
                    inputStream.close();

                    mEvisGlslBinary.parse();
                } else {
                    Log.e(TAG, "Error! Shader binary files missing! vendor: "
                            + vendor + " renderer: " + renderer);
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to find shader binary files");
                e.printStackTrace();
                return false;
            } finally {
                //assetManager.close();
            }
        }

        return true;
    }
}
