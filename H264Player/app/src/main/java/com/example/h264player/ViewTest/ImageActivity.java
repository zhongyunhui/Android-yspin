package com.example.h264player.ViewTest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.example.h264player.R;
import com.example.h264player.ViewTest.SurfaceViewDemo;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {
    //private ImageView imageView;
    SurfaceViewDemo surfaceViewDemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
    }

    void SurfaceViewPrepare(){
        surfaceViewDemo=(SurfaceViewDemo) findViewById(R.id.surfaceviewdemo);
        String filepath= Environment.getExternalStorageState()+"/xunlong.mp4";
        surfaceViewDemo.setPath(filepath);
        //surfaceViewDemo.init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //case R.id.imageshow:imageView=(ImageView)findViewById(R.id.image_pdd);break;
            case R.id.surfaceshow:
                SurfaceViewPrepare();break;
            default:break;
        }
    }
}
