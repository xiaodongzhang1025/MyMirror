package com.example.dong.mymirror.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.dong.mymirror.R;


public class AudioRecordManger {
    private static final String TAG = "AudioRecordManger";
    public static final int SAMPLE_RATE_IN_HZ = 8000;
    public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;
    public boolean isGetVoiceRun;
    private Handler mHandler;
    private int mWhat;
    public Object mLock;




    public AudioRecordManger(Handler handler, int what){
        mLock = new Object();//同步锁
        this.mHandler = handler;//获取句柄
        this.mWhat = what;//动作ID
    }
    public void getNoiseLevel(){
        if(isGetVoiceRun){
            Log.e(TAG, "还在录着呢");
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null){
            Log.e(TAG, "mAudioRecord创建失败");
        }
        isGetVoiceRun = true;//开始录音
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun){
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    //将buffer内容取出 进行平方和运算
                    for(int i = 0; i < buffer.length; i++){
                        v += buffer[i]*buffer[i];
                    }
                    double mean = v/(double)r;//平方和除以数据总长度 得到音量大小
                    double volume = 10*Math.log10(mean);

                    //大概一秒十次 锁
                    synchronized (mLock) {
                        try {
                            mLock.wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Message message = Message.obtain();
                    message.what = mWhat;
                    message.obj = volume;
                    mHandler.sendMessage(message);
                }//end of while
                if(mAudioRecord != null){
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                }
            }
        }).start();//启动线程
    }


}
