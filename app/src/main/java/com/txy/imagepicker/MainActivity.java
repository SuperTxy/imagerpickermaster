package com.txy.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.example.apple.glidetest.ImagePickerActivty;
import com.example.apple.glidetest.utils.GlideUtils;
import com.example.apple.glidetest.utils.PickerSettings;
import java.io.File;
import java.util.ArrayList;
public class MainActivity extends Activity {


    GridView gvMain;
    private ArrayList<String> imags;
    private MyGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gvMain = (GridView) findViewById(R.id.gv_main);
        findViewById(R.id.tv_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, ImagePickerActivty.class), PickerSettings.PICKER_CODE);
            }
        });
        adapter = new MyGridAdapter();
        gvMain.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PickerSettings.PICKER_CODE) {
            imags = data.getStringArrayListExtra(PickerSettings.RESULT);
            adapter.notifyDataSetChanged();
        }
    }

    class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imags == null ? 0 : imags.size();
        }

        @Override
        public String getItem(int position) {
            return imags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.image_seleted_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            GlideUtils.loadImage(new File(getItem(position)), MainActivity.this, holder.ivImage);
            return convertView;
        }

    }

    static class ViewHolder {
        ImageView ivImage;
        ImageView ivDel;

        ViewHolder(View view) {
            ivImage = (ImageView) view.findViewById(R.id.iv_image);
            ivDel = (ImageView) view.findViewById(R.id.iv_del);
        }
    }
}
