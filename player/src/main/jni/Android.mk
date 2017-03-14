LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
#TARGET_ARCH_ABI := armeabi-v7a

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS := -mfloat-abi=softfp -mfpu=neon
endif

LOCAL_SRC_FILES := \
    util/panel_param.cpp \
    util/frame_process.cpp \
    jni_panel_control.cpp \
    jni_frame_process.cpp \
    jni_shader_script.cpp \
    com_evistek_utils_evisutil.cpp

LOCAL_MODULE := libevisutil
LOCAL_LDLIBS := -lm -llog -ljnigraphics

LOCAL_SHARED_LIBRARIES := \
            libcutils \
            libutils \

include $(BUILD_SHARED_LIBRARY)
