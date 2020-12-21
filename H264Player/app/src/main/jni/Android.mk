LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := rtmp
LOCAL_SRC_FILES :=  librtmp/amf.c \
					librtmp/hashswf.c \
					librtmp/log.c \
					librtmp/rtmp.c \
					librtmp/parseurl.c \

LOCAL_C_INCLUDE :=$(LOCAL_PATH)/librtmp
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS:= -O2 -DSYS=posix -DNO_CRYPTO -Wno-unused-const-variable -Wno-unused-variable -Wno-unused-parameter
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE     := zhibo
LOCAL_SRC_FILES  := source/wrapper.cpp \
					source/realize.cpp \
					source/Live.cpp   \
					source/KMP.cpp  \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/source
LOCAL_SHARED_LIBRARIES :=rtmp
LOCAL_CFLAGS:= -Wno-unused-const-variable -Wno-unused-variable -Wno-unused-parameter
LOCAL_LDLIBS     := -llog
include $(BUILD_SHARED_LIBRARY)