package com.example.dong.mymirror.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.dong.mymirror.R;

/**
 * Created by dong on 2018/3/17.
 */

public class FunctionView extends LinearLayout implements View.OnClickListener {

    private LayoutInflater mInflater;
    private ImageView hint, choose, down, up;
    public static final int HINT_ID = R.id.hint;
    public static final int CHOOSE_ID = R.id.choose;
    public static final int DOWN_ID = R.id.light_down;
    public static final int UP_ID = R.id.light_up;
    onFunctionViewItemClickListener listener;
    public interface onFunctionViewItemClickListener{
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
    private void init(){
        View view = mInflater.inflate(R.layout.view_function, this);
        hint = (ImageView)findViewById(HINT_ID);
        choose = (ImageView)findViewById(CHOOSE_ID);
        up = (ImageView)findViewById(UP_ID);
        down = (ImageView)findViewById(DOWN_ID);
        setView();
    }
    private void setView(){
        hint.setOnClickListener(this);
        choose.setOnClickListener(this);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        if(listener != null){
            switch(view.getId()){
                case HINT_ID:
                    listener.hint();
                    break;
                case CHOOSE_ID:
                    listener.choose();
                    break;
                case UP_ID:
                    listener.up();
                    break;
                case DOWN_ID:
                    listener.down();
                    break;
                default:
                    break;
            }
        }
    }

    public void setOnFunctionViewItemClickListener(onFunctionViewItemClickListener monFunctionViewItemClickListener){
        this.listener = monFunctionViewItemClickListener;
    }
}
