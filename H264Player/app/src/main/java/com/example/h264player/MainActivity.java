package com.example.h264player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.example.h264player.RecordAndPlay.RecordActivity;
import com.example.h264player.ViewTest.ImageActivity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //muxtest:
    private static String sdcard_path;
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;
    private Button btn_start;



    private Button ViewTest,RecordTest,MuxTest;
    H264Player h264Player;
    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private List<String> mPermissionList = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdcard_path=getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath();
        ButtonInit();
        checkPermissions();

    }
    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }

    private void ButtonInit(){
        ViewTest=(Button)findViewById(R.id.goView);
        ViewTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });
        RecordTest=findViewById(R.id.goRecord);
        RecordTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });
        MuxTest=findViewById(R.id.goMux);
        MuxTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    process();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

    }


    private void initSurface(){
        //SurfaceView surface=(SurfaceView)findViewById(R.id.pre)
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==100&&resultCode== Activity.RESULT_OK){
            mediaProjection=mediaProjectionManager.getMediaProjection(resultCode,data);
        }
    }
    public void startLive(View view){
        this.mediaProjectionManager=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent=mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent,100);
    }
    public void stopLive(View view){

    }

    private boolean process() throws IOException {
        mediaExtractor=new MediaExtractor();
        mediaExtractor.setDataSource(sdcard_path+"/xunlong.mp4");
        int mVideoTrackIndex=-1;
        int frameRate=0;//I帧出现的频率
        for(int i=0;i<mediaExtractor.getTrackCount();i++){//getTrackCount返回通道数
            MediaFormat mediaFormat=mediaExtractor.getTrackFormat(i);//获取i的通道格式
            String mime=mediaFormat.getString(MediaFormat.KEY_MIME);
            try {
                if(mime.startsWith("video/")){
                    frameRate=mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                    mediaExtractor.selectTrack(i);
                    mediaMuxer=new MediaMuxer(sdcard_path+"/xunlong_1.mp4",MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    mVideoTrackIndex=mediaMuxer.addTrack(mediaFormat);//将mediaFormat存入
                    mediaMuxer.start();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }
        if(mediaMuxer==null){
            return false;
        }
        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();//申请一个空间
        info.presentationTimeUs=0;
        ByteBuffer buffer=ByteBuffer.allocate(500*1024);//500kb
        int sampleSize=0;
        while((sampleSize = mediaExtractor.readSampleData(buffer, 0))>0){
            info.offset=0;
            info.size=sampleSize;
            info.flags=MediaCodec.BUFFER_FLAG_KEY_FRAME;
            info.presentationTimeUs+=1000*1000/frameRate;
            mediaMuxer.writeSampleData(mVideoTrackIndex,buffer,info);
            mediaExtractor.advance();//读取下一帧数据
        }
        mediaExtractor.release();
        mediaMuxer.stop();
        mediaMuxer.release();
        return true;
    }
}
