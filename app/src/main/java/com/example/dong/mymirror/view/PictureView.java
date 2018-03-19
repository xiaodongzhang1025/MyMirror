package com.example.dong.mymirror.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by dong on 2018/3/17.
 */

public class PictureView extends ImageView {
    public PictureView(Context context){
        this(context, null);
    }
    public PictureView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public PictureView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }
}
