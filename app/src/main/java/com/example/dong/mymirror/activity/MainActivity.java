package com.example.dong.mymirror.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dong.mymirror.R;
import com.example.dong.mymirror.utils.AudioRecordManger;
import com.example.dong.mymirror.utils.SetBrightness;
import com.example.dong.mymirror.view.DrawView;
import com.example.dong.mymirror.view.FunctionView;
import com.example.dong.mymirror.view.PictureView;
import com.zys.brokenview.BrokenCallback;
import com.zys.brokenview.BrokenTouchListener;
import com.zys.brokenview.BrokenView;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener, View.OnClickListener,
FunctionView.onFunctionViewItemClickListener, DrawView.OnEraseCompleteListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private PictureView pictureView;
    private FunctionView functionView;
    private SeekBar seekBar;
    private ImageView add, minus;
    private LinearLayout bottom;
    private ImageView save;
    private ProgressDialog dialog;
    private DrawView drawView;

    private boolean haveCamera;
    private int mCurrentCamIndex;
    private int ROTATE;
    private int minFocus;
    private int maxFocus;
    private int everyFocus;
    private int nowFocus;
    private Camera camera;
    private boolean isCameraOn = true;

    //选择镜框
    private int frame_index;
    private int[] frame_index_ID;
    private static final int PHOTO = 1;
    private static final int REQUEST_WRITE_SETTINGS = 2;

    private int brightnessValue;
    private boolean isAutoBrightness;
    private int SegmentLengh;//亮度分为8段，每段为256/8
    private boolean isWriteSettingGranted = false;

    private AudioRecordManger audioRecordManger;
    private static final int RECORD = 3;

    private BrokenView brokenView;
    private boolean isBroken;
    private BrokenTouchListener brokenTouchListener;
    private MyBrokenCallback brokenCallback;
    private Paint brokenPaint;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case RECORD:
                    double soundValues = (double)msg.obj;
                    getSoundValues(soundValues);
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不显示状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();
        mySimpleGestureListener = new MySimpleGestureListener();
        getstureDetector = new GestureDetector(this, mySimpleGestureListener);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)) {
                isWriteSettingGranted = false;
                Log.i(TAG, "onCreate requestPermissions WRITE_SETTINGS");
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
            }else{
                Log.i(TAG, "已拥有WRITE_SETTINGS权限");
                isWriteSettingGranted = true;
                getBrightnessFromWindow();
            }
        }else{
            getBrightnessFromWindow();
        }

        frame_index = 0;
        frame_index_ID = new int[]{R.mipmap.mag_0001,R.mipmap.mag_0003,R.mipmap.mag_0005,
                R.mipmap.mag_0006,R.mipmap.mag_0007,R.mipmap.mag_0008,R.mipmap.mag_0009,
                R.mipmap.mag_0011,R.mipmap.mag_0012,R.mipmap.mag_0014,};

        //运行时权限处理
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "surfaceCreated requestPermissions CAMERA");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }else {
            audioRecordManger = new AudioRecordManger(handler, RECORD);
            audioRecordManger.getNoiseLevel();//打开话筒监听声音
        }

    }

    private void initViews() {
        surfaceView = (SurfaceView)findViewById(R.id.surface);
        pictureView = (PictureView)findViewById(R.id.picture);
        functionView = (FunctionView)findViewById(R.id.function);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        add = (ImageView)findViewById(R.id.add);
        minus = (ImageView)findViewById(R.id.minus);
        bottom = (LinearLayout)findViewById(R.id.bottom_bar);
        drawView = (DrawView)findViewById(R.id.draw_glasses);
    }
    private void setViews(){
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        add.setOnTouchListener(this);
        minus.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        functionView.setOnFunctionViewItemClickListener(this);
        pictureView.setOnTouchListener(this);
        drawView.setOnEraseCompleteListener(this);
        setToBrokenTheView();
    }
    private boolean checkCameraHardware(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }
    private Camera openFrontFacingCameraGingerbread(){
        int cameraCount;
        Camera mCamera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for(int camIdx = 0; camIdx < cameraCount; camIdx++){
             Camera.getCameraInfo(camIdx, cameraInfo);
             if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                 try{
                     mCamera = Camera.open(camIdx);
                     mCurrentCamIndex = camIdx;
                 }catch (RuntimeException e){
                     Log.e(TAG, "相机打开失败:" + e.getLocalizedMessage());
                 }
             }
        }
        return mCamera;
    }
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degress = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
        }
        int result = 0;
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degress)%360;
            result = (360 - result)%360;
        }else{
            result = (info.orientation - degress + 360)%360;
        }
        ROTATE = result + 180;
        camera.setDisplayOrientation(result);
    }
    private void setCamera(){
        if(checkCameraHardware()){
            camera = openFrontFacingCameraGingerbread();
            setCameraDisplayOrientation(this, mCurrentCamIndex, camera);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            List<String> list = parameters.getSupportedFocusModes();
            for(String str:list){
                Log.i(TAG, "支持的对焦模式:" + str);
            }
            List<Camera.Size> pictureList = parameters.getSupportedPictureSizes();
            List<Camera.Size> previewList = parameters.getSupportedPreviewSizes();
            parameters.setPictureSize(pictureList.get(0).width, pictureList.get(0).height);
            parameters.setPreviewSize(previewList.get(0).width, previewList.get(0).height);
            minFocus = parameters.getZoom();
            maxFocus = parameters.getMaxZoom();
            everyFocus = 1;
            nowFocus = minFocus;
            seekBar.setMax(maxFocus);
            Log.i(TAG, "当前镜头距离:"+minFocus+"\t\t获取最大距离:"+maxFocus);
            camera.setParameters(parameters);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("surfaceCreated", "绘制开始");

        //运行时权限处理
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "surfaceCreated requestPermissions CAMERA");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        }else {
            if(isCameraOn) {
                try {
                    setCamera();
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (IOException e) {
                    camera.release();
                    camera = null;
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged 绘制改变");
        if(isCameraOn && camera != null) {
            try {
                camera.stopPreview();
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed 绘制结束");
        if(isCameraOn && camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "you accepted the permission of camera");
                    Toast.makeText(this, "you allowed the permission of camera", Toast.LENGTH_SHORT).show();
                    if(isCameraOn) {
                        try {
                            setCamera();
                            camera.setPreviewDisplay(holder);
                            camera.startPreview();
                        } catch (IOException e) {
                            camera.release();
                            camera = null;
                            e.printStackTrace();
                        }
                    }
                }else{
                    Log.i(TAG, "you denied the permission of camera");
                    Toast.makeText(this, "you denied the permission of camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "you accepted the permission of audio");
                    Toast.makeText(this, "you allowed the permission of audio", Toast.LENGTH_SHORT).show();
                    audioRecordManger = new AudioRecordManger(handler, RECORD);
                    audioRecordManger.getNoiseLevel();//打开话筒监听声音
                }else{
                    Log.i(TAG, "you denied the permission of audio");
                    Toast.makeText(this, "you denied the permission of audio", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void setZoomValues(int want){
        if(camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            seekBar.setProgress(want);
            parameters.setZoom(want);
            camera.setParameters(parameters);
        }
    }
    private int getZoomValues(){
        if(camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            int values = parameters.getZoom();
            return values;
        }
        return 0;
    }
    private void addZoomValues(){
        nowFocus = getZoomValues();
        if(nowFocus + everyFocus > maxFocus){
            Log.e(TAG, "大于maxFocus是不可能的");
        }else if(nowFocus == maxFocus){

        }else{
            setZoomValues(nowFocus + everyFocus);
        }
    }
    private void minusZoomValues(){
        nowFocus = getZoomValues();
        if(nowFocus - everyFocus < 0){
            Log.e(TAG, "小于0是不可能的");
        }else if(nowFocus == 0){

        }else{
            setZoomValues(nowFocus - everyFocus);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            nowFocus = i;
            parameters.setZoom(i);
            camera.setParameters(parameters);
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(view.getId()){
            case R.id.add:
                addZoomValues();
                break;
            case R.id.minus:
                minusZoomValues();
                break;
            case R.id.picture:
                //待添加手势识别
                getstureDetector.onTouchEvent(motionEvent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void hint() {
        Intent intent = new Intent(this, HintActivity.class);
        startActivity(intent);
    }

    @Override
    public void choose() {
        Intent intent = new Intent(this, PhotoFrameActivity.class);
        startActivityForResult(intent, PHOTO);
        Toast.makeText(this, "选择！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void down() {
        if(isWriteSettingGranted == true) {
            downCurrentActivityBrightnessValues();
        }
    }

    @Override
    public void up() {
        if(isWriteSettingGranted == true) {
            upCurrentActivityBrightnessValues();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "返回值:" + resultCode + ",请求值:" + requestCode);
        if(resultCode == RESULT_OK && requestCode == PHOTO){
            int position = data.getIntExtra("POSITION", 0);
            Log.i(TAG, "返回的镜框类别:" + position);
            pictureView.setPhotoFrame(position);
        }else if(requestCode == REQUEST_WRITE_SETTINGS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(this)){
                    Log.i(TAG, "WRITE_SETTINGS权限申请成功");
                    isWriteSettingGranted = true;
                    getBrightnessFromWindow();
                }else {
                    isWriteSettingGranted = false;
                    Log.i(TAG, "WRITE_SETTINGS权限申请失败");
                }
            }
        }
    }

    private void setMyActivityBright(int brightnessValue){
        SetBrightness.setBrightness(this, brightnessValue);
        SetBrightness.saveBrightness(SetBrightness.getResolver(this), brightnessValue);
    }
    private void getAfterMySetBrightnessValues(){
        brightnessValue = SetBrightness.getScreenBrightness(this);
        Log.i(TAG, "当前手机屏幕亮度值：" + brightnessValue);
    }
    private void getBrightnessFromWindow(){
        isAutoBrightness = SetBrightness.isAutoBrightness(SetBrightness.getResolver(this));
        Log.i(TAG,"当前手机是否自动调节明目亮度:" + isAutoBrightness);
        if(isAutoBrightness){
            SetBrightness.stopAutoBrightness(this);
            Log.i(TAG,"关闭自动调节亮度");
            setMyActivityBright(255/2+1);
        }
        SegmentLengh = (255/2+1)/4;
        getAfterMySetBrightnessValues();
    }
    private void downCurrentActivityBrightnessValues(){
        if(brightnessValue - SegmentLengh > 0){
            setMyActivityBright(brightnessValue - SegmentLengh);
        }
        getAfterMySetBrightnessValues();
    }
    private void upCurrentActivityBrightnessValues(){
        if(brightnessValue < 255){
            if(brightnessValue + SegmentLengh >= 256){
                return;
            }
            setMyActivityBright(brightnessValue + SegmentLengh);
        }
        getAfterMySetBrightnessValues();
    }
    private void hideView() {
        bottom.setVisibility(View.INVISIBLE);
        functionView.setVisibility(View.GONE);
    }
    private void showView() {
        pictureView.setImageBitmap(null);
        bottom.setVisibility(View.VISIBLE);
        functionView.setVisibility(View.VISIBLE);
    }
    private void getSoundValues(double values){
        if(values > 50){
            hideView();
            drawView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.in_window);
            drawView.setAnimation(animation);
            audioRecordManger.isGetVoiceRun = false;
            Log.i(TAG, "玻璃显示：执行");
        }
    }

    @Override
    public void complete() {
        showView();
        audioRecordManger.getNoiseLevel();
        drawView.setVisibility(View.GONE);
    }

    private void setToBrokenTheView(){
        brokenPaint = new Paint();
        brokenPaint.setStrokeWidth(5);
        brokenPaint.setColor(Color.BLACK);
        brokenPaint.setAntiAlias(true);
        brokenView = BrokenView.add2Window(this);
        brokenTouchListener = new BrokenTouchListener.Builder(brokenView).setPaint(
                brokenPaint).setBreakDuration(2000).setFallDuration(5000).build();
        brokenView.setEnable(true);
        brokenCallback = new MyBrokenCallback();
        brokenView.setCallback(brokenCallback);

    }
    class MyBrokenCallback extends BrokenCallback{
        @Override
        public void onStart(View v) {//按住控件
            super.onStart(v);
        }

        @Override
        public void onFalling(View v) {//执行碎屏
            super.onFalling(v);
            //soundPool.play(sound.get(1),1,1,0,0,1);
        }

        @Override
        public void onFallingEnd(View v) {
            super.onFallingEnd(v);
            brokenView.reset();
            pictureView.setOnTouchListener(MainActivity.this);
            pictureView.setVisibility(View.VISIBLE);
            isBroken = false;
            brokenView.setEnable(isBroken);//设置碎屏停止
            audioRecordManger.getNoiseLevel();
            showView();
        }

        @Override
        public void onCancelEnd(View v) {//取消碎屏结束
            super.onCancelEnd(v);
        }
    }
    private GestureDetector getstureDetector;
    private MySimpleGestureListener mySimpleGestureListener;
    class MySimpleGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            isBroken = true;
            brokenView.setEnable(true);
            pictureView.setOnTouchListener(brokenTouchListener);//设置碎屏长按监听
            hideView();
            audioRecordManger.isGetVoiceRun = false;
        }
    }
}
