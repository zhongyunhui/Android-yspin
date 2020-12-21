//
// Created by Administrator on 2020/12/15.
//

#ifndef H264PLAYER_REALIZE_H
#define H264PLAYER_REALIZE_H

#include <jni.h>
#include "librtmp/rtmp.h"
jboolean NativeConnect(JNIEnv *env,jobject jobject1,jstring jurl);
jboolean NativeSendData(JNIEnv *env,jobject jobject1,jbyteArray jdata,jint len,jlong tms);


#endif //H264PLAYER_REALIZE_H
