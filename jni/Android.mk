LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := hello-uart
LOCAL_SRC_FILES := hello-uart.cpp example.cpp
LOCAL_SHARED_LIBRARIES:=libutils libbinder liblog
LOCAL_LDLIBS += -LLOG
LOCAL_LDLIBS += -lm
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -g
include $(BUILD_SHARED_LIBRARY)
