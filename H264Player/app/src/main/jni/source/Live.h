//
// Created by Administrator on 2020/12/15.
//

#ifndef H264PLAYER_LIVE_H
#define H264PLAYER_LIVE_H

#include <memory>
#include "librtmp/rtmp.h"
class Live {
public:
    Live() {
        rtmp= nullptr;
        sps= nullptr;
        sps_len= 0;
        pps= nullptr;
        pps_len= 0;

    }

    RTMP *getRtmp() const {
        return rtmp;

    }

    void setRtmp(RTMP *rtmp) {
        Live::rtmp = rtmp;
    }

    int8_t getSpsSpecified(int pos){
        return sps[pos];
    }
    int8_t getPpsSpecified(int pos){
        return pps[pos];
    }

    int8_t *getSps() const {
        return sps;
    }

    void setSps(int8_t *sps) {
        Live::sps = sps;
    }

    int16_t getSpsLen() const {
        return sps_len;
    }

    void setSpsLen(int16_t spsLen) {
        sps_len = spsLen;
    }

    int8_t *getPps() const {
        return pps;
    }

    void setPps(int8_t *pps) {
        Live::pps = pps;
    }

    int16_t getPpsLen() const {
        return pps_len;
    }

    void setPpsLen(int16_t ppsLen) {
        pps_len = ppsLen;
    }

private:
    //同一个视频中SPS,PPS相同
    RTMP *rtmp;
    int8_t *sps;      //宽高  帧率   编码质量   H264   配置信息
    int16_t sps_len;
    int8_t *pps; //宽高
    int16_t pps_len;

};


#endif //H264PLAYER_LIVE_H
