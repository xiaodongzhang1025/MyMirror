package com.example.dong.mymirror.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.example.dong.mymirror.R;


/**
 * Created by dong on 2018/3/17.
 */

public class PictureView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "PictureView";
    private int[] bitmap_ID_Array;
    private Canvas mCanvas;
    private int draw_width;
    private int draw_height;
    private Bitmap mBitmap;
    private int bitmap_index;


    public PictureView(Context context){
        this(context, null);
    }
    public PictureView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public PictureView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        getTheWindowSize((Activity)context);
        init();
    }

    private void initBitmaps(){
        bitmap_ID_Array = new int[]{R.mipmap.mag_0001,R.mipmap.mag_0003,R.mipmap.mag_0005,
                R.mipmap.mag_0006,R.mipmap.mag_0007,R.mipmap.mag_0008,R.mipmap.mag_0009,
                R.mipmap.mag_0011,R.mipmap.mag_0012,R.mipmap.mag_0014
        };
    }
    private void init(){
        initBitmaps();
        bitmap_index = 0;
        mBitmap = Bitmap.createBitmap(draw_width, draw_height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    public void setPhotoFrame(int index){
        bitmap_index = index;
        invalidate();
    }
    public int getPhotoFrame(){
        return bitmap_index;
    }
    private void getTheWindowSize(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        draw_width = dm.widthPixels;
        draw_height = dm.heightPixels;
        Log.i(TAG, "屏幕宽度:" + draw_width + "屏幕高度:" + draw_height);
    }
    private Bitmap getNewBitmap(){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmap_ID_Array[bitmap_index]).copy(Bitmap.Config.ARGB_8888, true);
        bitmap = Bitmap.createScaledBitmap(bitmap, draw_width, draw_height, true);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        Rect rect1 = new Rect(0, 0, this.getWidth(), this.getHeight());
        canvas.drawBitmap(getNewBitmap(), null, rect1, null);
    }
}
