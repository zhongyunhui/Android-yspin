package com.example.h264player;

public class JniHelper {
    static{
        System.loadLibrary("zhibo");
    }
    public static native boolean connect(String url);
    public native boolean sendData(byte[] data,int len,long tms);
}
