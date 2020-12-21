package com.example.h264player;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class H264Player implements Runnable{
    private Context context;
    private String path;
    private MediaCodec mediaCodec;
    private Surface surface;
    public H264Player(Context context,String path,Surface surface){
        this.context=context;
        this.path=path;
        this.surface=surface;
        try{
            mediaCodec=MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaFormat=MediaFormat.createVideoFormat("video/avc",368,384);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);
            mediaCodec.configure(mediaFormat,surface,null,0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void play(){
        mediaCodec.start();

    }

    @Override
    public void run() {
        try{
            decodeH264();
        }catch (Exception e){
            Log.e("zyh","decode error");
        }
    }
    private void decodeH264(){
        byte[] bytes=null;
        try{
            bytes=getBytes(path);
        }catch (Exception e){
            e.printStackTrace();
        }
        //ByteBuffer[] inputBuffers=mediaCodec.getInputBuffers();
        int startIndex=0;
        int totalSize=bytes.length;
        while(true) {
            if(totalSize==0||startIndex>=totalSize){
                break;
            }
            int nextFrame=findByFrame(bytes,startIndex+3,totalSize);
            int inIndex = mediaCodec.dequeueInputBuffer(10000);
            if (inIndex >= 0) {//有空闲的ByteBuffer,接下来放一帧数据到Buffer中
                ByteBuffer byteBuffer=mediaCodec.getInputBuffer(inIndex);
                byteBuffer.clear();
                byteBuffer.put(bytes,startIndex,nextFrame-startIndex);
                mediaCodec.queueInputBuffer(inIndex,0,nextFrame-startIndex,0,0);
                startIndex=nextFrame;
            }else {
                continue;
            }
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            int outIndex=mediaCodec.dequeueOutputBuffer(info,10000);
            if(outIndex>=0){//解码成功
                ByteBuffer byteBuffer=mediaCodec.getOutputBuffer(outIndex);//解码出数据为一张图片信息
                byteBuffer.position(info.offset);//info中存储了解码后的数据信息
                byteBuffer.limit(info.offset+info.size);
                byte[] ba=new byte[byteBuffer.remaining()];
                byteBuffer.get(ba);//传到ba
                YuvImage yuvImage=new YuvImage(ba, ImageFormat.NV21,368,384,null);
                //yuvImage.compressToJpeg(new Rect(0,0,267,384),100,baos);
                //byte[] jdata=
                try {
                    Thread.sleep(33);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                mediaCodec.releaseOutputBuffer(outIndex,true);//render,渲染到屏幕则传true
            }else{

            }
        }
    }
    private int findByFrame(byte[] bytes,int start,int totalSize){
        int j=0;
        for(int i=start;i<totalSize-4;i++){
            if(bytes[i]==0x00 &&bytes[i+1]==0x00&&bytes[i+2]==0x00&&bytes[i+3]==0x01){
                return i;
            }
        }
        return -1;
    }

    public byte[] getBytes(String path)throws IOException{
        InputStream is=new DataInputStream(new FileInputStream(new File(path)));
        int len;
        int size=1024;
        byte[] buf;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        buf=new byte[size];
        while((len=is.read(buf,0,size))!=-1){
            bos.write(buf,0,len);
        }
        buf=bos.toByteArray();
        return buf;
    }
}
