package com.example.apple.glidetest;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.OsUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Apple on 17/7/18.
 */

public class PictureShowPopup implements ViewPager.OnPageChangeListener {

    private ImageView checkBox;
    private ViewPager viewPager;
    private PopupWindow popupWindow;
    private List<String> mDataList;
    private Activity activity;
    private final MyPagerAdapter mPagerAdapter;
    private final SelectImageProvider imageProvider;

    public PictureShowPopup( Activity context) {
        this.activity = context;
        imageProvider = SelectImageProvider.getInstance();
        View view = View.inflate(activity, R.layout.layout_big_image, null);
        view.findViewById(R.id.iv_big_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        checkBox = (ImageView) view.findViewById(R.id.cb_big_check);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, OsUtils.getScreenHeight(activity) - OsUtils.getStatusBarHeight(activity), true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        mPagerAdapter = new MyPagerAdapter();
        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mDataList.get(viewPager.getCurrentItem());
                if (imageProvider.isSelectedMax() && !checkBox.isSelected())
                    Toast.makeText(activity, "每次最多只能选择"+imageProvider.maxSelect+"张图片！", Toast.LENGTH_SHORT).show();
                else {
                    Log.e("TAG", checkBox.isSelected()+"");
                    checkBox.setSelected(!checkBox.isSelected());
                    Log.e("TAG", checkBox.isSelected()+"");
                    if(checkBox.isSelected()) imageProvider.add(path);
                    else imageProvider.remove(path);
                }
            }
        });
    }

    public void show(View parentView) {
        popupWindow.showAtLocation(parentView, Gravity.TOP, 0, OsUtils.getStatusBarHeight(activity));
    }

    public PictureShowPopup setDataAndPosition(@NonNull List<String> list, int currPos) {
        mDataList = list;
        mPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(currPos);
        onPageSelected(currPos);
        return this;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        checkBox.setSelected(imageProvider.getSelectedImgs().contains(mDataList.get(position)));
        Log.e("onPageSelected", checkBox.isSelected() + "");
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(activity);
            File file = new File(mDataList.get(position));
            if (file.exists())
                Glide.with(activity).load(file).into(imageView);
            else Log.e("TAG", "文件不存在！");
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
