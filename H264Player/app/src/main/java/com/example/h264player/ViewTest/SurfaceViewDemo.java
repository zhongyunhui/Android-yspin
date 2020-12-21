package com.example.h264player.ViewTest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public class SurfaceViewDemo extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    String path;
    private MediaPlayer mediaPlayer;
    private Surface mSurface;
    private String TAG="zyh";
    public SurfaceViewDemo(Context context){
        this(context,null,0);
    }
    public SurfaceViewDemo(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }
    public SurfaceViewDemo(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init();
    }
    public void setPath(String path){
        this.path=path;
    }
    /*public void dispaly(){
        mSurface
    }*/
    public void init(){
        surfaceHolder=getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(1000,562);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        setZOrderOnTop(true);
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        Log.e(TAG,"init finished");
    }
    protected void play(){
        path= Environment.getExternalStorageDirectory().getPath()+"/xunlong.mp4";
        Log.e(TAG+"videopath",path);
        File file=new File(path);
        if(!file.exists()){
            return;
        }
        try{
            mediaPlayer =new MediaPlayer();
            AudioAttributes attributes=new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
            //设置音频
            mediaPlayer.setAudioAttributes(attributes);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(file.getAbsolutePath());

            mediaPlayer.prepareAsync();
            Log.e(TAG,"has prepared");
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.e(TAG,"load video successfully");
                    mediaPlayer.start();
                }
            });
            //播放完成事件绑定事件监听器
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.e(TAG,"play end");
                    //replay();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.e(TAG,"open video failed");
                    return false;
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    protected void replay(){
        if(mediaPlayer!=null){
            mediaPlayer.start();
        }else{
            play();
        }
    }
    private void draw(){
        try{
            Log.e(TAG,"--------dray---------");
            canvas=surfaceHolder.lockCanvas();
            canvas.drawCircle(500,500,300,paint);
            canvas.drawCircle(100,100,20,paint);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(canvas!=null){
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.e(TAG,"---------- SurfaceCreated----------");
        new Thread(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e(TAG,"-------surfaceChanged--------");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.e(TAG,"-------surfaceDestroyed-------");
        if(mediaPlayer!=null){
            stop();
        }
    }
    protected void stop(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
    protected void pause(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }else{
            mediaPlayer.start();
        }

    }
}
