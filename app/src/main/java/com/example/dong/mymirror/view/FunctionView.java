package com.example.dong.mymirror.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.dong.mymirror.R;

/**
 * Created by dong on 2018/3/17.
 */

public class FunctionView extends LinearLayout implements View.OnClickListener {

    private LayoutInflater mInflater;
    private interface onFunctionViewItemClickListener{
        void hint();
        void choose();
        void down();
        void up();
    }
    public FunctionView(Context context){
        this(context, null);
    }
    public FunctionView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public FunctionView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public void init(){
        View view = mInflater.inflate(R.layout.view_function, this);
    }
    @Override
    public void onClick(View view) {

    }
}
