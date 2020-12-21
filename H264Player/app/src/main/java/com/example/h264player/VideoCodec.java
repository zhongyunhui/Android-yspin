package com.example.h264player;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoCodec extends Thread {
    private MediaCodec mediaCodec;//
    private ScreenLive screenLive;
    private MediaProjection mediaProjection;//录屏，一帧原始数据
    private VirtualDisplay virtualDisplay;
    private boolean isLiving;
    private String TAG="zyh";
    private long startTime;
    private long timeStamp;

    public VideoCodec(ScreenLive screenLive){
        this.screenLive=screenLive;
    }

    @Override
    public void run() {
//通过mediaCodec拿到视频中的数据
        //super.run();
        isLiving=true;
        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
        Log.i(TAG,"run in ViderCodec编码");
        while(isLiving){
            //编码后的索引
            int index=mediaCodec.dequeueOutputBuffer(bufferInfo,10000);
            if(index>=0){
                Log.i(TAG,"run "+index);
                if(System.currentTimeMillis()-timeStamp>=2_000){
                    Bundle params=new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0);
                    mediaCodec.setParameters(params);
                    timeStamp=System.currentTimeMillis();
                }
                ByteBuffer buffer=mediaCodec.getOutputBuffer(index);//返回byteBuffer;压缩后的H264数据
                byte[] outData=new byte[bufferInfo.size];
                buffer.get(outData);
                if(startTime==0){
                    startTime=bufferInfo.presentationTimeUs/1000;
                }
                RTMPPackage rtmpPackage=new RTMPPackage(outData,(bufferInfo.presentationTimeUs/1000));
                screenLive.addPackage(rtmpPackage);
                mediaCodec.releaseOutputBuffer(index,false);
            }
            isLiving=false;
            startTime=0;
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec=null;
            virtualDisplay.release();
            virtualDisplay=null;
            mediaProjection.stop();
            mediaProjection=null;
        }
    }
    public void startLive(MediaProjection mediaProjection){
        this.mediaProjection=mediaProjection;
        try {
            MediaFormat mediaFormat=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,540,960);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);//设为和Surface相同
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,400_000);//400k的码率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);//帧率
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);//I帧间隔2s
            mediaCodec=MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);//surface为空表示不需要渲染
            Surface surface=mediaCodec.createInputSurface();
            virtualDisplay=mediaProjection.createVirtualDisplay("sreen-codec",540,960,1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,surface,null,null);
        }catch (IOException e){
            e.printStackTrace();
        }
        start();
    }
}
