package com.example.h264player;

import android.media.projection.MediaProjection;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class ScreenLive extends Thread{
    private boolean isLiving;
    private String url;
    private MediaProjection mediaProjection;
    private LinkedBlockingQueue<RTMPPackage> queue=new LinkedBlockingQueue<>();
    private JniHelper jniHelper;
    private String TAG="zyh";

    public ScreenLive(String url,MediaProjection mediaProjection){
        this.url=url;
        this.mediaProjection=mediaProjection;
        jniHelper=new JniHelper();
        start();
    }
    public void addPackage(RTMPPackage rtmpPackage){
        if(isLiving){
            queue.add(rtmpPackage);
        }
    }

    @Override
    public void run() {
        if(!JniHelper.connect(url)){
            Log.e("zyh","连接失败");
            return;
        }
        VideoCodec videoCodec=new VideoCodec(this);
        videoCodec.startLive(mediaProjection);
        while(isLiving){
            RTMPPackage rtmpPackage=null;
            try{
                rtmpPackage=queue.take();

            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(rtmpPackage==null){
                continue;
            }
            if(rtmpPackage.getBuffer()!=null&&rtmpPackage.getBuffer().length!=0){
                Log.e(TAG,"run: 推送"+rtmpPackage.getBuffer().length);
                jniHelper.sendData(rtmpPackage.getBuffer(),rtmpPackage.getBuffer().length,rtmpPackage.getTms());
            }
        }
    }
}
