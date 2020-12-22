package com.example.h264player.ToolClass;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AACToPCM {
    private static final String TAG="AACToPCM";
    public static final int ERROR_INPUT_INVALID = 100;
    public static final int ERROR_OUTPUT_FAILED = 200;
    public static final int ERROR_OPEN_CODEC = 300;
    public static final int OK = 0;
    private static final int TIMEOUT_USEC = 10000;
    private MediaExtractor mediaExtractor;
    private MediaFormat format;
    private MediaCodec mediaCodec;
    private FileOutputStream outputStream;
    private ByteBuffer[] inputBuffers,outputBuffers;
    private boolean decodeEnd;
    public AACToPCM(){ }
    private int checkPath(String path){
        if (path == null || path.isEmpty()) {
            Log.d(TAG, "invalid path, path is empty");
            return ERROR_INPUT_INVALID;
        }
        File file = new File(path);
        if (!file.isFile()) {
            Log.d(TAG, "path is not a file, path:" + path);
            return ERROR_INPUT_INVALID;
        } else if (!file.exists()) {
            Log.d(TAG, "file not exists, path:" + path);
            return ERROR_INPUT_INVALID;
        } else {
            Log.d(TAG, "path is a file, path:" + path);
        }
        return OK;
    }
    private int openInput(String aacpath){
        Log.e(TAG,"aacpath: "+aacpath);
        int ret;
        if(OK!=(ret=checkPath(aacpath))){
            return ret;
        }
        mediaExtractor=new MediaExtractor();
        int audioTrack=-1;
        boolean hasAudio=false;
        try{
            mediaExtractor.setDataSource(aacpath);
            for(int i=0;i<mediaExtractor.getTrackCount();i++){
                MediaFormat mediaFormat=mediaExtractor.getTrackFormat(i);
                String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
                if(mime.startsWith("audio/")){//音频帧
                    audioTrack=i;
                    hasAudio=true;
                    format=mediaFormat;
                    break;
                }
            }
            if(!hasAudio){
                Log.e(TAG,"input contains no audio");
                return ERROR_INPUT_INVALID;
            }
            mediaExtractor.selectTrack(audioTrack);
        }catch (IOException e){
            e.printStackTrace();
        }
        return OK;
    }
    private int openOutput(String pcmpath){
        Log.e(TAG,"PCM path:"+pcmpath);
        try {
            outputStream=new FileOutputStream(pcmpath);
        }catch (IOException e){
            return ERROR_OUTPUT_FAILED;
        }
        return OK;
    }
    private int openCodec(MediaFormat format){
        String type=format.getString(MediaFormat.KEY_MIME);
        if(type==null){
            return ERROR_OPEN_CODEC;
        }
        Log.e(TAG,"openCodec, format mime:"+type);
        try{
            mediaCodec=MediaCodec.createDecoderByType(type);
        }catch (IOException e){
            e.printStackTrace();
            return ERROR_OPEN_CODEC;
        }
        mediaCodec.configure(format,null,null,0);
        mediaCodec.start();
        return OK;
    }
    private int decode(MediaCodec codec,MediaExtractor extractor){
        int inputIndex=codec.dequeueInputBuffer(TIMEOUT_USEC);//从输入流队列中寻找空闲的队列编号
        if(inputIndex>=0){
            ByteBuffer inputBuffer=codec.getInputBuffer(inputIndex);//获取需要编码数据的输入流队列
            inputBuffer.clear();//将该队列清空
            int sampleSize=extractor.readSampleData(inputBuffer,0);//将extractor中每一帧存入inputBuffer
            if(sampleSize<0){
                codec.queueInputBuffer(inputIndex,0,0,0L,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }else{
                codec.queueInputBuffer(inputIndex,0,sampleSize,extractor.getSampleTime(),0);//输入流入队列
                extractor.advance();
            }
        }
        //取数据
        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
        int outputIndex=codec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
        if(outputIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
            Log.d(TAG,"INFO_TRY_AGAIN_LATER");
        }else if(outputIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            Log.e(TAG,"output format changed");
        }else if (outputIndex<0){
            Log.d(TAG,"outputIndex: "+outputIndex);
        }else{
            ByteBuffer outputBuffer;
            outputBuffer=codec.getOutputBuffer(outputIndex);
            byte[] buffer=new byte[bufferInfo.size];
            outputBuffer.get(buffer);//outputBuffer数据存入buffer中
            try{
                outputStream.write(buffer);
                outputStream.flush();
            }catch (IOException e){
                e.printStackTrace();
                return ERROR_OUTPUT_FAILED;
            }
            codec.releaseOutputBuffer(outputIndex,false);//释放buffer
            if((bufferInfo.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0){
                decodeEnd=true;
            }
        }
        return OK;
    }

    public int decodeAACToPCM(String aacpath,String pcmpath){
        int ret;
        if(OK!=(ret=openInput(aacpath))){
            return ret;
        }
        if(OK!=(ret=openOutput(pcmpath))){
            return ret;
        }
        if(OK!=(ret=openCodec(format))){
            return ret;
        }
        decodeEnd=false;
        while(!decodeEnd){
            if(OK!=(ret=decode(mediaCodec,mediaExtractor))){
                Log.e(TAG,"decode failed ret="+ret);
                break;
            }
        }
        close();
        return ret;
    }
    private void close(){
        mediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();
        try{
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
