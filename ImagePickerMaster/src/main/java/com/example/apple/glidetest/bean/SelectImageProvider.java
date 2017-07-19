package com.example.apple.glidetest.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Apple on 17/5/31.
 */

public class SelectImageProvider extends Observable {

    private static SelectImageProvider provider;
    public int maxSelect = 10;
    private static final String TAG = "SelectImageProvider";
    private ArrayList<String> selectedImgs = new ArrayList<>();

    public static SelectImageProvider getInstance() {
        if (provider == null) {
            provider = new SelectImageProvider();
        }
        return provider;
    }

    private SelectImageProvider() {
    }

    public void setMaxSelect(int maxSelect) {
        this.maxSelect = maxSelect;
    }

    public void remove(String path) {
        if (selectedImgs.contains(path)) {
            selectedImgs.remove(path);
            setChanged();
            notifyObservers(new Change(Change.REMOVE, path));
            Log.e("remove", selectedImgs.size() + "-->" + path);
        }else{
            Log.e("TAG", "此图片不存在，无法移除！");
        }

    }

    public void add(String path) {
        if (!selectedImgs.contains(path)) {
            selectedImgs.add(path);
            setChanged();
            notifyObservers(new Change(Change.ADD, path));
            Log.e("add", selectedImgs.size() + "-->" + path);
        } else {
            Log.e("TAG", "已经存在此图片了！");
        }
    }

    public ArrayList<String> getSelectedImgs() {
        return selectedImgs;
    }

    public int getLastIndex() {
        return selectedImgs.size() - 1;
    }

    public int getCount() {
        return selectedImgs.size();
    }

    public void clear() {
        setChanged();
        notifyObservers();
        selectedImgs.clear();
    }

    public boolean isSelectedMax() {
        return selectedImgs.size() == maxSelect;
    }
    public boolean isPathExist(String path){
        return selectedImgs.contains(path);
    }
}
