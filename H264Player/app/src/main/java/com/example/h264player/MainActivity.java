package com.example.h264player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.h264player.RecordAndPlay.RecordActivity;
import com.example.h264player.ViewTest.ImageActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button ViewTest,RecordTest;
    H264Player h264Player;
    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private List<String> mPermissionList = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
