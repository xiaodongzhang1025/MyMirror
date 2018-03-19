package com.example.dong.mymirror.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dong.mymirror.R;
import com.example.dong.mymirror.view.DrawView;
import com.example.dong.mymirror.view.FunctionView;
import com.example.dong.mymirror.view.PictureView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();
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
            Log.i(TAG, "surfaceCreated requestPermissions");
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
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
