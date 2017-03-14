#ifndef _PANAL_PARAM_H
#define _PANAL_PARAM_H

#if 1 //android log
#include <android/log.h>
#define LOG_TAG "Panel Param"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

#else // standard c/c++ log
#include <stdio.h>
#define LOGD(...)  printf(__VA_ARGS__)
#define LOGI(...)  printf(__VA_ARGS__)
#define LOGW(...)  printf(__VA_ARGS__)
#define LOGE(...)  printf(__VA_ARGS__)
#define LOGF(...)  printf(__VA_ARGS__)
#endif

#ifdef __cplusplus

extern void panelParamSet(bool redFirst);
extern void rasterParamSet(float cover, float cot, float offset, bool slope);
extern bool templateCreate(signed char* buf, int w, int h);
#else
extern "C"
{
    void panelParamSet(bool redFirst);
    void rasterParamSet(float cover, float cot, float offset, bool slope);
    bool templateCreate(signed char* buf, int w, int h);
}
#endif

#endif
