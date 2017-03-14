APP_PROJECT_PATH := $(call my-dir)/..
APP_PLATFORM := android-15
#choose which library to compile against in your Makefile
APP_STL := stlport_static
#APP_ABI���ָ���˱����Ŀ��ƽ̨���ͣ�������Բ�ͬƽ̨�����Ż�,x86 or armeabi-v7a
# Build both ARMv5TE and ARMv7-A machine code.
APP_ABI := armeabi armeabi-v7a armeabi-v7a x86 x86_64
APP_CPPFLAGS += -fexceptions
#for using c++ features,you need to enable these in your Makefile
APP_CPP_FEATURES += exceptions rtti
