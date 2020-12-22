package com.example.h264player.CameraPlay;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;

import com.example.h264player.R;

public class CameraActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if(null==savedInstanceState){
            /*getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,Camera2VideoFragment.newInstance()).commit();*/
        }
    }

}
