#ifndef __COM_EVISTEK_UTILS_EVISUTIL_H__
#define __COM_EVISTEK_UTILS_EVISUTIL_H__
#include <jni.h>
#include <android/log.h>

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#define JNIREG_CLASS "com/evistek/gallery/utils/EvisUtil"

#define ALOGD(TAG, ...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define ALOGI(TAG, ...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define ALOGW(TAG, ...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__)
#define ALOGE(TAG, ...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#define ALOGF(TAG, ...)  __android_log_print(ANDROID_LOG_FATAL,TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

int register_panel_control_native_methods(JNIEnv* env);
int register_frame_process_native_methods(JNIEnv* env);
int register_shader_script_native_methods(JNIEnv* env);

#ifdef __cplusplus
}
#endif

#endif
