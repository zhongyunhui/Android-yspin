//
// Created by Administrator on 2020/12/15.
//
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "librtmp/rtmp.h"
#include "realize.h"
#include "Common.h"



static const JNINativeMethod gMethods[] = {
        {"connect","(/java/lang/String;)Z",(void*)NativeConnect},
        {"sendData","([BII)Z",(void*)NativeSendData}
};

static const char* const kClassName="com/example/h264player/JniHelper";

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env = NULL; //注册时在JNIEnv中实现的，所以必须首先获取它
    if(vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK){//从JavaVM获取JNIEnv，一般使用1.4的版本
        return -1;
    }

    jclass myClass = env->FindClass(kClassName);
    if(myClass == NULL)
    {
        //printf("cannot get class:%s\n", kClassName);
        return -1;
    }
    if(env->RegisterNatives(myClass,gMethods,sizeof(gMethods)/sizeof(gMethods[0]))<0)
    {
        //printf("register native method failed!\n");
        return -1;
    }
    //printf("--------JNI_OnLoad-----");
    return JNI_VERSION_1_6; //这里很重要，必须返回版本，否则加载会失败。
}

