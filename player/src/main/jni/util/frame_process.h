#ifndef __FRAME_PROCESS_H__
#define __FRAME_PROCESS_H__

#if 1 //android log
#include <android/log.h>
#define LOG_TAG "Frame Process"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

#else // standard c log
#include <stdio.h>
#define LOGD(...)  printf(__VA_ARGS__)
#define LOGI(...)  printf(__VA_ARGS__)
#define LOGW(...)  printf(__VA_ARGS__)
#define LOGE(...)  printf(__VA_ARGS__)
#define LOGF(...)  printf(__VA_ARGS__)
#endif



typedef unsigned char   uchar;
typedef unsigned short  ushort;
typedef unsigned int    uint;

#ifndef ABS
#	define ABS(a)  ((a)>0 ? (a):(-a))
#endif
#ifndef MIN
#  define MIN(a,b)  ((a) > (b) ? (b) : (a))
#endif

#ifndef MAX
#  define MAX(a,b)  ((a) < (b) ? (b) : (a))
#endif


#ifdef __cplusplus
extern "C" {
#endif

int isSBSFrame(unsigned char* pixels, uint32_t width, uint32_t height, int bpp);

#ifdef __cplusplus
}
#endif

#endif
