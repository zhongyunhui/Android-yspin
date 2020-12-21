package com.example.h264player.RecordAndPlay;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.h264player.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.h264player.RecordAndPlay.GlobalConfig.AUDIO_FORMAT;
import static com.example.h264player.RecordAndPlay.GlobalConfig.CHANNEL_CONFIG;
import static com.example.h264player.RecordAndPlay.GlobalConfig.SAMPLE_RATE_INHZ;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {
    private AudioRecord audioRecord=null;
    boolean isRecording=false;
    private Button mBtnControl;
    private Button mBtnPlay;
    private Button mBtnConvert;
    private AudioTrack audioTrack;
    private String TAG="zyh";
    private FileInputStream fileInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mBtnControl = (Button) findViewById(R.id.btn_control);
        mBtnControl.setOnClickListener(RecordActivity.this);
        mBtnConvert = (Button) findViewById(R.id.btn_convert);
        mBtnConvert.setOnClickListener(this);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_control:
                Button button = (Button) view;
                if (button.getText().toString().equals(getString(R.string.start_record))) {
                    button.setText(getString(R.string.stop_record));
                    StartAudioRecord();
                } else {
                    button.setText(getString(R.string.start_record));
                    StopAudioRecord();
                }
                break;
            case R.id.btn_convert:
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
                File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.wav");
                if (!wavFile.mkdirs()) {
                    Log.e(TAG, "wavFile Directory not created");
                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());

                break;
            case R.id.btn_play:
                Button btn = (Button) view;
                String string = btn.getText().toString();
                if (string.equals(getString(R.string.start_play))) {
                    btn.setText(getString(R.string.stop_play));
                    playInModeStream();
                    //playInModeStatic();
                } else {
                    btn.setText(getString(R.string.start_play));
                    StopAudioPlay();
                }
                break;

            default:
                break;
        }
    }

    private void StartAudioRecord(){
        final int minBufferSize=AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        Log.e(TAG,"BufSize"+minBufferSize);
        audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT,minBufferSize);
        final byte[] data=new byte[minBufferSize];
        final File file=new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"test.pcm");
        int state = audioRecord.getState();
        Log.e(TAG,"initialize state:"+state);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        if (file.exists()) {
            Log.e(TAG,"File delete");
            file.delete();
        }
        isRecording=true;
        audioRecord.startRecording();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os=null;
                String filepath= getExternalFilesDir(Environment.DIRECTORY_MUSIC)+"test.pcm";
                try {
                    os =new FileOutputStream(file);
                }catch (FileNotFoundException e){
                    e.printStackTrace();

                }
                if(null!=os){
                    while(isRecording){
                        int ret=audioRecord.read(data,0,minBufferSize);
                        if(AudioRecord.ERROR_INVALID_OPERATION!=ret){
                            try{
                                os.write(data);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    try{
                        os.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void StopAudioRecord(){
        isRecording=false;
        if(null!=audioRecord){
            audioRecord.stop();
            audioRecord.release();
            audioRecord=null;
        }
    }
    //stream模式播放
    private void playInModeStream(){
        //RATE_INHZ为采样率，CHANNEL_OUT_MONO为pcm音频的声道 AUDIO_FORMAT为pcm音频的格式
        int channalConfig= AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize=AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ,channalConfig,AUDIO_FORMAT);
        audioTrack=new AudioTrack(
                new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ).setEncoding(AUDIO_FORMAT).setChannelMask(channalConfig).build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        audioTrack.play();
        File file=new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"test.pcm");
        try{
            fileInputStream=new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        byte[] tempBuffer=new byte[minBufferSize];
                        while(fileInputStream.available()>0){
                            int readCount=fileInputStream.read(tempBuffer);
                            /*if(readCount==AudioTrack.ERROR_INVALID_OPERATION||readCount==AudioTrack.ERROR_BAD_VALUE
                                    ||readCount==AudioTrack.ERROR||readCount==AudioTrack.ENCAPSULATION_MODE_NONE){
                                continue;
                            }*/
                            Log.e("player ","readCount "+readCount);
                            if(readCount>0){
                                audioTrack.write(tempBuffer,0,readCount);
                            }
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void StopAudioPlay() {
        if (audioTrack != null) {
            Log.d(TAG, "Stopping");
            audioTrack.stop();
            Log.d(TAG, "Releasing");
            audioTrack.release();
            Log.d(TAG, "Nulling");
        }
    }
}
