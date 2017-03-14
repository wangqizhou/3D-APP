#include <stdlib.h>
#include "com_evistek_utils_evisutil.h"
#define TAG "JNI Shader Script"

const char mVideo2DVertexShader[] =
		"attribute vec4 aPosition;\n\
		 attribute vec4 aTextureCoord;\n\
		 varying vec2 vTextureCoord;\n\
		 void main() {\n\
		     gl_Position = aPosition;\n\
		     vTextureCoord = aTextureCoord.xy;\n\
		 }\n";

const char mVideo2DFragmentShader[] =
		"#extension GL_OES_EGL_image_external : require\n\
		 precision mediump float;\n\
		 varying vec2 vTextureCoord;\n\
		 uniform samplerExternalOES uTexture;\n\
		                                  \n\
		 void main() {\n\
		     gl_FragColor = texture2D(uTexture, vTextureCoord);\
		 }\n";

const char mVideo2D3DVertexShader[] =
		"attribute vec4 aPosition;\n \
		 attribute vec4 aTextureCoord;\n \
		 varying vec2 vTextureCoord;\n \
		 void main() {\n \
		     gl_Position = aPosition;\n \
		     vTextureCoord = aTextureCoord.xy;\n \
		 }\n";

const char mVideo2D3DFragmentShader[] =
		"#extension GL_OES_EGL_image_external : require\n \
		 precision highp float;\n \
		 varying vec2 vTextureCoord;\n \
		 uniform samplerExternalOES uTexture;\n \
		 uniform sampler2D uTemplateTexture;\n \
		 uniform float uK;\n \
		                             \n \
		 void main() {\n \
		     vec2 coord  =  vTextureCoord;\n \
		     coord.s = coord.s - uK;\n \
		     coord.s = coord.s + uK;\n \
		                                      \n \
		     vec4 color1 = texture2D(uTexture, coord)*texture2D(uTemplateTexture, coord).bgra; \n \
		     gl_FragColor = color1;\n \
		 }\n";


const char mVideo3DVertexShader[] =
		"attribute vec4 aPosition;\n \
		 attribute vec4 aTextureCoord;\n \
		 varying vec2 vTextureCoord;\n \
		 void main() {\n \
		     gl_Position = aPosition;\n \
		     vTextureCoord = aTextureCoord.xy;\n \
		 }\n";

const char mVideo3DFragmentShader[] =
		"#extension GL_OES_EGL_image_external : require\n \
		 precision highp float;\n \
		 varying vec2 vTextureCoord;\n \
		 uniform samplerExternalOES uTexture;\n \
		 uniform sampler2D uTemplateTexture;\n \
		 uniform int uFramePacking;\n \
		                             \n \
		 #define FRAME_PACKING_LEFT_RIGHT 0    \n \
		 #define FRAME_PACKING_RIGHT_LEFT 1  \n \
		 void main() {\n \
		     vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n \
		     vec4 red =  vec4(1.0, 0.0, 0.0, 1.0);\n \
		     vec4 green =  vec4(0.0, 1.0, 0.0, 1.0);\n \
		     vec4 blue =  vec4(0.0, 0.0, 1.0, 1.0);\n \
		                                       \n \
		     vec2 coord  =  vTextureCoord;\n \
		     vec2 coordl =  vTextureCoord;\n \
		     vec2 coordr =  vTextureCoord;\n \
		                                   \n \
		     if(uFramePacking == FRAME_PACKING_LEFT_RIGHT)\n \
		     {\n \
		         coordl.s = coord.s;\n \
		         coordr.s = coord.s;\n \
		     }\n \
		     else if (uFramePacking == FRAME_PACKING_RIGHT_LEFT)\n \
		     {\n \
		         coordr.s = coord.s;\n \
		         coordl.s = coord.s;\n \
		     }\n \
		                                            \n \
		     gl_FragColor = texture2D(uTexture, coordr)*texture2D(uTemplateTexture, coord).bgra;\n \
		 }\n";

const char mVideo3D2DVertexShader[] =
		"attribute vec4 aPosition;\n \
		 attribute vec4 aTextureCoord;\n \
		 varying vec2 vTextureCoord;\n \
		 void main() {\n \
		     gl_Position = aPosition;\n \
		     vTextureCoord = aTextureCoord.xy;\n \
		 }\n";

const char mVideo3D2DFragmentShader[] =
		"#extension GL_OES_EGL_image_external : require\n \
		 precision mediump float;\n \
		 varying vec2 vTextureCoord;\n \
		 uniform samplerExternalOES uTexture;\n \
		                                 \n \
		 void main() {\n \
		     vec2 coord =  vTextureCoord;\n \
		     gl_FragColor = texture2D(uTexture, coord);\n \
		 }\n";


const char mImage2DVertexShader[] =
        "attribute vec4 aPosition;\n \
         attribute vec4 aTextureCoord;\n \
         varying vec2 vTextureCoord;\n \
         uniform mat4 uScaleMatrix;\n \
         uniform mat4 uTranslateMatrix;\n \
                                       \n \
         void main() {\n \
             gl_Position = aPosition;\n \
             mat4 temp = uScaleMatrix * uTranslateMatrix + uTranslateMatrix; \n \
             vTextureCoord = (aTextureCoord * temp).xy;\n \
         }\n";

const char mImage2DFragmentShader[] =
        "precision highp float;\n \
         varying vec2 vTextureCoord;\n \
         uniform sampler2D uTexture;\n \
                                             \n \
         void main() {\n \
             gl_FragColor = texture2D(uTexture, vTextureCoord);\n  \
         }\n";

const char mImage2D3DVertexShader[] =
        "attribute vec4 aPosition;\n \
         attribute vec4 aTextureCoord;\n \
         attribute vec4 aTextureCoord_template;\n \
         uniform mat4 uScaleMatrix;\n \
         uniform mat4 uTranslateMatrix;\n \
         varying vec2 vTextureCoord;\n \
         varying vec2 vTextureCoord_template;\n \
		                                    \n \
         void main() {\n \
             gl_Position = aPosition;\n \
			 mat4 temp = uScaleMatrix * uTranslateMatrix + uTranslateMatrix; \n \
             vTextureCoord = (aTextureCoord * temp).xy;\n \
             vTextureCoord_template = aTextureCoord_template.xy;\n \
         }\n";

const char mImage2D3DFragmentShader[] =
        "precision highp float;\n \
         varying vec2 vTextureCoord;\n \
         varying vec2 vTextureCoord_template;\n \
         uniform sampler2D uTexture;\n \
         uniform sampler2D uTemplateTexture;\n \
         uniform float uK;\n \
                                     \n \
         void main() {\n \
             vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n \
             vec2 coord = vTextureCoord;\n \
             vec2 coordl = vTextureCoord;\n \
             vec2 coordr = vTextureCoord;\n \
                                        \n \
             coordl.s = coord.s - uK;\n \
             coordr.s = coord.s + uK;\n \
                                         \n \
             gl_FragColor = texture2D(uTexture, coordr) * texture2D(uTemplateTexture, coordl).bgra;\n \
         }\n";

const char mImage3DVertexShader[] =
        "attribute vec4 aPosition;\n \
         attribute vec4 aTextureCoord;\n \
         attribute vec4 aTextureCoord_template;\n \
         uniform mat4 uScaleMatrix;\n \
         uniform mat4 uTranslateMatrix;\n \
         varying vec2 vTextureCoord;\n \
         varying vec2 vTextureCoord_template;\n \
         void main() {\n \
             gl_Position =  aPosition;\n \
             mat4 temp = uScaleMatrix * uTranslateMatrix + uTranslateMatrix; \n \
             vTextureCoord = (aTextureCoord * temp).xy;\n \
             vTextureCoord_template = aTextureCoord_template.xy;\n \
         }\n";

const char mImage3DFragmentShader[] =
		"precision highp float;\n \
		 varying vec2 vTextureCoord;\n \
		 varying vec2 vTextureCoord_template;\n \
		 uniform sampler2D uTexture;\n \
		 uniform sampler2D uTempTexture;\n \
		 uniform int uFramePacking;\n \
		                               \n \
		 #define FRAME_PACKING_LEFT_RIGHT 0    \n \
		 #define FRAME_PACKING_RIGHT_LEFT 1  \n \
		                                          \n \
		 void main() {\n \
		     vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n \
		     vec4 red =  vec4(1.0, 0.0, 0.0, 1.0);\n \
		     vec4 green =  vec4(0.0, 1.0, 0.0, 1.0);\n \
		     vec4 blue =  vec4(0.0, 0.0, 1.0, 1.0);\n \
		                                           \n \
		     vec2 coord  =  vTextureCoord;\n \
		     vec2 coordl =  vTextureCoord;\n \
		     vec2 coordr =  vTextureCoord;\n \
		                                      \n \
		     if(uFramePacking == FRAME_PACKING_LEFT_RIGHT)\n \
		     {\n \
		         coordl.s = coord.s;\n \
		         coordr.s = coord.s;\n \
		     }\n \
		     else if (uFramePacking == FRAME_PACKING_RIGHT_LEFT)\n \
		     {\n \
		         coordr.s = coord.s;\n \
		         coordl.s = coord.s;\n \
		     }\n \
		                                      \n \
		     vec2 coord_template  =  vTextureCoord_template;\n \
		     gl_FragColor = texture2D(uTexture, coordr)*texture2D(uTempTexture, coord_template).bgra; \n \
		 }\n";


const char mImage3D2DVertexShader[] =
		"attribute vec4 aPosition;\n \
		 attribute vec4 aTextureCoord;\n \
		 uniform mat4 uScaleMatrix;\n \
		 uniform mat4 uTranslateMatrix;\n \
		 varying vec2 vTextureCoord;\n \
		 void main() {\n \
		     gl_Position = aPosition;\n \
		     mat4 temp = uScaleMatrix * uTranslateMatrix + uTranslateMatrix; \n \
		     vTextureCoord = (aTextureCoord * temp).xy;\n \
		 }\n";

const char mImage3D2DFragmentShader[] =
		"#extension GL_OES_EGL_image_external : require\n \
		 precision mediump float;\n \
		 varying vec2 vTextureCoord;\n \
		 uniform sampler2D uTexture;\n \
		                                     \n \
		 void main() {\n \
		     vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n \
		     vec2 coord =  vTextureCoord;\n \
		     gl_FragColor = texture2D(uTexture, coord);\n \
		 }\n";

#define SHADER_TYPE_VIDEO 0
#define SHADER_TYPE_IMAGE 1
#define SHADER_INDEX_2D 0
#define SHADER_INDEX_2D3D 1
#define SHADER_INDEX_3D 2
#define SHADER_INDEX_3D2D 3

jobjectArray getObjectArray(JNIEnv *env, jint type, jint index) {
	jobjectArray objArray = NULL;
    jsize len = 2;

    objArray = env->NewObjectArray(len, env->FindClass("java/lang/String"), NULL);
    if (objArray != NULL) {
        jstring vertexShader;
        jstring fragmentShader;

        if (type == SHADER_TYPE_VIDEO) {
            switch(index) {
            case SHADER_INDEX_2D:
                vertexShader = env->NewStringUTF(mVideo2DVertexShader);
                fragmentShader = env->NewStringUTF(mVideo2DFragmentShader);
                break;
            case SHADER_INDEX_2D3D:
                vertexShader = env->NewStringUTF(mVideo2D3DVertexShader);
                fragmentShader = env->NewStringUTF(mVideo2D3DFragmentShader);
                break;
            case SHADER_INDEX_3D:
                vertexShader = env->NewStringUTF(mVideo3DVertexShader);
                fragmentShader = env->NewStringUTF(mVideo3DFragmentShader);
                break;
            case SHADER_INDEX_3D2D:
                vertexShader = env->NewStringUTF(mVideo3D2DVertexShader);
                fragmentShader = env->NewStringUTF(mVideo3D2DFragmentShader);
                break;
            default:
                break;
            }
        } else if (type == SHADER_TYPE_IMAGE) {
            switch(index) {
            case SHADER_INDEX_2D:
                vertexShader = env->NewStringUTF(mImage2DVertexShader);
                fragmentShader = env->NewStringUTF(mImage2DFragmentShader);
                break;
            case SHADER_INDEX_2D3D:
                vertexShader = env->NewStringUTF(mImage2D3DVertexShader);
                fragmentShader = env->NewStringUTF(mImage2D3DFragmentShader);
                break;
            case SHADER_INDEX_3D:
                vertexShader = env->NewStringUTF(mImage3DVertexShader);
                fragmentShader = env->NewStringUTF(mImage3DFragmentShader);
                break;
            case SHADER_INDEX_3D2D:
                vertexShader = env->NewStringUTF(mImage3D2DVertexShader);
                fragmentShader = env->NewStringUTF(mImage3D2DFragmentShader);
                break;
            default:
                break;
            }
        }

        env->SetObjectArrayElement(objArray, 0, vertexShader);
        env->SetObjectArrayElement(objArray, 1, fragmentShader);
    }

    return objArray;
}

jobjectArray get2DShader(JNIEnv *env, jobject obj, jint type) {
    return getObjectArray(env, type, SHADER_INDEX_2D);
}

jobjectArray get2D3DShader(JNIEnv *env, jobject obj, jint type) {
    return getObjectArray(env, type, SHADER_INDEX_2D3D);
}

jobjectArray get3DShader(JNIEnv *env, jobject obj, jint type) {
    return getObjectArray(env, type, SHADER_INDEX_3D);
}

jobjectArray get3D2DShader(JNIEnv *env, jobject obj, jint type) {
    return getObjectArray(env, type, SHADER_INDEX_3D2D);
}

static JNINativeMethod sMethods[] = {
    {"get2DShader", "(I)[Ljava/lang/String;", (void *)get2DShader},
    {"get2D3DShader", "(I)[Ljava/lang/String;", (void *)get2D3DShader},
    {"get3DShader", "(I)[Ljava/lang/String;", (void *)get3DShader},
    {"get3D2DShader", "(I)[Ljava/lang/String;", (void *)get3D2DShader},
};

static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods,
    int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL)
        return JNI_FALSE;
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

int register_shader_script_native_methods(JNIEnv* env)
{
    if (!registerNativeMethods(env, JNIREG_CLASS, sMethods, NELEM(sMethods))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
