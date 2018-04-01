package com.example.dong.mymirror.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.icu.util.Measure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.dong.mymirror.R;
import com.example.dong.mymirror.activity.MainActivity;

/**
 * Created by dong on 2018/3/17.
 */

public class DrawView extends View {
    private static final String TAG = DrawView.class.getSimpleName();
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float moveX, moveY;
    private Bitmap mBitmap, bitmap;
    private volatile boolean mComplete = false;

    public DrawView(Context context){
        this(context, null);
    }
    public DrawView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public DrawView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.glasses).copy(Bitmap.Config.ARGB_8888, true);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(100);
        mPath = new Path();

        Log.i(TAG, "init");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        if(!mComplete){//如果擦除未完成
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mCanvas.drawPath(mPath, mPaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        if(mComplete){//如果擦除干净，则进行资源释放操作
            if(mListener != null){
                mListener.complete();
                setEndValues();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
        mCanvas.drawBitmap(bitmap, 0, 0, null);
    }

    public interface OnEraseCompleteListener{
        void complete();
    }
    private OnEraseCompleteListener mListener;
    public void setOnEraseCompleteListener(OnEraseCompleteListener mListener){
        this.mListener = mListener;
    }
    public void setEndValues(){//变量重置
        moveX = 0;
        moveY = 0;
        mPath.reset();
        mComplete = false;
    }
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0;
            float totalArea = w*h;
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w*h];
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            //计算擦除区域
            for(int i = 0 ; i < w; i++){
                for(int j = 0; j < h; j++){
                    int index = i+j*w;
                    if(mPixels[index] == 0){
                        wipeArea++;
                    }
                }
            }
            //计算擦除区域所占比例
            if(wipeArea > 0 && totalArea > 0){
                int percent = (int)(wipeArea*100/totalArea);
                Log.i(TAG, "percent:" + percent);
                if(percent > 50){
                    mComplete = true;
                    postInvalidate();
                }
            }

        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                moveX = x;
                moveY = y;
                mPath.moveTo(moveX, moveY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int)Math.abs(moveX - x);
                int dy = (int)Math.abs(moveY - y);
                if(dx > 1 || dy > 1){
                    mPath.quadTo(x, y, (moveX+x)/2, (moveY+y)/2);
                }
                moveX = x;
                moveY = y;
                break;
            case MotionEvent.ACTION_UP:
                if(!mComplete){
                    new Thread(mRunnable).start();
                }
                break;
            default:
                break;
        }
        if(!mComplete){
            invalidate();//刷新view控件
        }
        return true;
    }
}
