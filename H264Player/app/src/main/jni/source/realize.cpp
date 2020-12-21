//
// Created by Administrator on 2020/12/15.
//

#include <malloc.h>
#include "realize.h"
#include <memory>
#include "source/Live.h"
#include "source/KMP.h"
#include "Common.h"

namespace Delimiter{
    int8_t Dsps[4]={0x00,0x00,0x01,0x67};
    int8_t Dpps[4]={0x00,0x00,0x01,0x68};
}
std::unique_ptr<Live> liveptr;

jboolean NativeConnect(JNIEnv *env,jobject jobject1,jstring jurl){
    const char* url=env->GetStringUTFChars(jurl,0);
    int ret;
    do{
        liveptr.reset(new Live());

        liveptr->setRtmp(RTMP_Alloc());
        RTMP_Init(liveptr->getRtmp());
        liveptr->getRtmp()->Link.timeout=10;

        if(!(ret=RTMP_SetupURL(liveptr->getRtmp(),(char*)url)))break;
        RTMP_EnableWrite(liveptr->getRtmp());
        if(!(ret=RTMP_Connect(liveptr->getRtmp(),0)))break;
        if(!(ret=RTMP_ConnectStream(liveptr->getRtmp(),0)))break;
    }while(0);
    if(!ret&&liveptr!= nullptr){
        liveptr.release();
    }
    env->ReleaseStringUTFChars(jurl,url);

    return static_cast<jboolean >(ret);
}

void prepareVideo(int8_t *data, int len){


    int Spos=KMP::KMPPos(data,len,Delimiter::Dpps,4);
    if(Spos!=-1){
        int16_t spslen=static_cast<int16_t >(Spos-4);
        liveptr->setSpsLen(spslen);
        int8_t *SpsArea=new int8_t[spslen];
        memcpy(SpsArea,data+4,spslen);
        liveptr->setSps(SpsArea);
        int16_t ppslen= (int16_t )(len-(4+spslen)-4);
        liveptr->setPpsLen(ppslen);
        int8_t *PpsArea=new int8_t[ppslen];
        memcpy(PpsArea,data+4+spslen+4,ppslen);
        liveptr->setPps(PpsArea);
        LOGE("Spslen: %d, Ppslen: %d",spslen,ppslen);
    }


    /*for(int i=0;i<len;i++){
        livePtr->setPps(static_cast<int8_t *>malloc())
    }*/
}

std::unique_ptr<RTMPPacket> createVideoPackage(){
    std::unique_ptr<RTMPPacket >packet(new RTMPPacket());
    int body_size=16+liveptr->getSpsLen()+liveptr->getPpsLen();
    RTMPPacket_Alloc(packet.get(),body_size);
    int i=0;
    packet->m_body[i++]=0x17;
    //AVC sequence header,与IDR一样
    packet->m_body[i++]=0x00;
    //Compositiontime
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=0x01;//configuration version

    packet->m_body[i++]=liveptr->getSpsSpecified(1); //profile baseline,main,high
    packet->m_body[i++]=liveptr->getSpsSpecified(2);//profile_compatibility兼容性
    packet->m_body[i++]=liveptr->getSpsSpecified(3);//profile level
    packet->m_body[i++]= static_cast<char>(0xFF);
    int16_t spslen=liveptr->getSpsLen();
    packet->m_body[i++]= static_cast<char>((spslen >> 8) & 0xff);
    packet->m_body[i++]= static_cast<char>(spslen & 0xff);
    memcpy(&packet->m_body[i],liveptr->getSps(),spslen);
    i+=spslen;
    int16_t ppslen=liveptr->getPpsLen();
    packet->m_body[i++]= static_cast<char>((ppslen >> 8) & 0xff);
    packet->m_body[i++]= static_cast<char>(ppslen & 0xff);
    memcpy(&packet->m_body[i],liveptr->getPps(),ppslen);
    //配置packet的参数
    packet->m_packetType=RTMP_PACKET_TYPE_VIDEO;//类型
    packet->m_nBodySize=body_size;//数据长度
    packet->m_nChannel=0x04;
    packet->m_hasAbsTimestamp=0;
    packet->m_headerType=RTMP_PACKET_SIZE_LARGE;//大数据包
    packet->m_nInfoField2=liveptr->getRtmp()->m_stream_id;
    return packet;
}

std::unique_ptr<RTMPPacket> createVideoPackage(int8_t *buf,int len,const long tms){
    buf+=4;
    len-=4;
    //组装I帧数据包
    std::unique_ptr<RTMPPacket >packet(new RTMPPacket());
    int body_size=len+9;
    RTMPPacket_Alloc(packet.get(),body_size);
    int i=0;
    if(buf[0]==0x65){
        packet->m_body[i++]=0x17;
        LOGE("this is key Frame I");
    }else{
        packet->m_body[i++]=0x27;
    }
    packet->m_body[i++]=0x01;
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=0x00;
    packet->m_body[i++]=(len>>24)&0xff;
    packet->m_body[i++]=(len>>16)&0xff;
    packet->m_body[i++]=(len>>8)&0xff;
    packet->m_body[i++]=(len)&0xff;
    memcpy(&packet->m_body[i++],buf,len);

    packet->m_packetType=RTMP_PACKET_TYPE_VIDEO;
    packet->m_nChannel=0x04;
    packet->m_nBodySize=body_size;
    packet->m_hasAbsTimestamp=0;//
    packet->m_headerType=RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2=liveptr->getRtmp()->m_stream_id;
    return packet;
}

int sendPacket(RTMPPacket * packet){
    int r=RTMP_SendPacket(liveptr->getRtmp(),packet,1);
    return r;
}

int sendVideo(int8_t *buf,int len,long tms){
    int ret;
    if(buf[4]==0x67){
        //第一次调用 解析了sps pps,缓存下来
        prepareVideo(buf,len);
    }else{
        if(buf[4]==0x65){//第二次调用，判断I帧 0x65
            //先发送sps pps的数据包
            std::unique_ptr<RTMPPacket> packet=createVideoPackage();
            sendPacket(packet.get());//发送sps pps
        }
        std::unique_ptr<RTMPPacket> mpacket=  createVideoPackage(buf,len,tms);
        sendPacket(mpacket.get());
    }
}

jboolean NativeSendData(JNIEnv *env,jobject jobject1,jbyteArray jdata,jint len,jlong tms){
    int ret;
    jbyte *data=env->GetByteArrayElements(jdata,NULL);
    ret=sendVideo(data,len,tms);
    env->ReleaseByteArrayElements(jdata,data,0);
    return static_cast<jboolean >(ret);
}
