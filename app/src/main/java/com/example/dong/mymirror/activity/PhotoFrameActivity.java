package com.example.dong.mymirror.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dong.mymirror.R;

public class PhotoFrameActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener{

    private GridView gridView;
    private TextView textView;
    private int[] photo_styles;
    private String[] photo_name;
    private Bitmap[] bitmaps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_frame);

        textView = (TextView) findViewById(R.id.back_to_main);
        gridView = (GridView) findViewById(R.id.photo_frame_list);
        initDatas();
        textView.setOnClickListener(this);

        PhotoFrameAdapter adapter = new PhotoFrameAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }
    private void initDatas(){
        photo_styles = new int[]{R.mipmap.mag_0001,R.mipmap.mag_0003,R.mipmap.mag_0005,
                R.mipmap.mag_0006,R.mipmap.mag_0007,R.mipmap.mag_0008,R.mipmap.mag_0009,
                R.mipmap.mag_0011,R.mipmap.mag_0012,R.mipmap.mag_0014};
        photo_name = new String[]{"1", "2","3", "4","5", "6","7", "8","9", "10"};
        bitmaps = new Bitmap[photo_styles.length];
        for(int i = 0; i < photo_styles.length; i++){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), photo_styles[i]);
            bitmaps[i] = bitmap;
        }
    }

    class PhotoFrameAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return photo_name.length;
        }

        @Override
        public Object getItem(int i) {
            return photo_name[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                holder = new ViewHolder();
                view = getLayoutInflater().inflate(R.layout.item_gridview, null);
                holder.image = (ImageView) view.findViewById(R.id.item_pic);
                holder.text = (TextView) view.findViewById(R.id.item_text);
                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }
            setData(holder, i);
            return view;
        }

        private void setData(ViewHolder holder, int position){
            holder.image.setImageBitmap(bitmaps[position]);
            holder.text.setText(photo_name[position]);
        }
        class ViewHolder{
            ImageView image;
            TextView text;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_to_main:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        intent.putExtra("POSITION", i);
        setResult(RESULT_OK, intent);
        finish();
    }
}
