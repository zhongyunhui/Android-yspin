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

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

public class VideoCodec extends Thread {
    private MediaCodec mediaCodec;//
    private ScreenLive screenLive;
    private MediaProjection mediaProjection;//录屏，一帧原始数据
    private VirtualDisplay virtualDisplay;
    private boolean isLiving;
    private static String TAG="zyh";
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
        mediaCodec.start();//启动编码器
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
            final MediaFormat mediaFormat=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,540,960);//创建mediaFormat
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);//设定编码器颜色格式
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,400_000);//400k的码率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);//帧率
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);//I帧间隔2s
            mediaCodec=MediaCodec.createEncoderByType("video/avc");
           /* mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
                    ByteBuffer inputBuffer=mediaCodec.getInputBuffer(i);
                    mediaCodec.queueInputBuffer(i,0,0,10000,BUFFER_FLAG_CODEC_CONFIG);
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
                    ByteBuffer outputBuffer=mediaCodec.getOutputBuffer(i);
                    MediaFormat format=mediaCodec.getOutputFormat(i);
                    //...
                    mediaCodec.releaseOutputBuffer(i,false);
                }

                @Override
                public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat1) {
                    //mediaFormat=mediaFormat1;
                }
            });*/
            mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);//surface为空表示不需要渲染
            Surface surface=mediaCodec.createInputSurface();
            virtualDisplay=mediaProjection.createVirtualDisplay("screen-codec",540,960,1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,surface,null,null);
        }catch (IOException e){
            e.printStackTrace();
        }
        start();
    }
}
