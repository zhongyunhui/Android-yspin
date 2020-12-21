//
// Created by Administrator on 2020/12/15.
//

#ifndef H264PLAYER_COMMON_H
#define H264PLAYER_COMMON_H

#include <jni.h>
#include <android/log.h>
#define LOG_TAG "zyh"
#ifdef DEBUG
#define LOG(args...)
#define LOGV(...)
#define LOGI(...)
#define LOGW(...)
#else
#define LOGE(...)      __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGV(...)      __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define LOGI(...)      __android_log_print(ANDROID_LOG_INFO  ,LOG_TAG, __VA_ARGS__)
#define LOGW(...)      __android_log_print(ANDROID_LOG_WARN  ,LOG_TAG, __VA_ARGS__)
#endif
#endif //H264PLAYER_COMMON_H
