LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := MyClientJNI
LOCAL_SRC_FILES := libMyClient.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := hello-uart
LOCAL_SRC_FILES := hello-uart.cpp
LOCAL_SHARED_LIBRARIES:=libutils libbinder liblog MyClientJNI
LOCAL_LDLIBS += -LLOG
LOCAL_LDLIBS += -lm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -g
include $(BUILD_SHARED_LIBRARY)
