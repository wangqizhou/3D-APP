#include <android/bitmap.h>
#include "com_evistek_utils_evisutil.h"
#include "util/frame_process.h"

#define TAG "JNI Frame Process"

int isSbs(JNIEnv* env, jobject obj, jobject bitmap){
    unsigned char* pixels = NULL;
    AndroidBitmapInfo info;
    uint32_t width;
    uint32_t height;
    int32_t format;
    int bpp;
    int ret = -1;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        ALOGE(TAG, "AndroidBitmap_getInfo() failed ! error=%d", ret);
        return ret;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **)&pixels)) < 0) {
        ALOGE(TAG, "AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return ret;
    }

    width = info.width;
    height = info.height;
    switch(info.format)
    {
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            bpp = 4;
            break;
        case ANDROID_BITMAP_FORMAT_RGB_565:
            bpp = 2;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            bpp = 2;
            break;
        default:
            bpp = 4;
            break;
    }
    ret = isSBSFrame(pixels, width, height, bpp);
    AndroidBitmap_unlockPixels(env, bitmap);
    return ret;
}

static JNINativeMethod sMethods[] = {
    {"isSbs", "(Landroid/graphics/Bitmap;)I", (void *) isSbs},
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

int register_frame_process_native_methods(JNIEnv* env)
{
    if (!registerNativeMethods(env, JNIREG_CLASS, sMethods, NELEM(sMethods))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
