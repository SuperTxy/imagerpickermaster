package com.example.apple.glidetest.bean;

import java.util.ArrayList;

/**
 * Created by Apple on 17/5/31.
 */

public class SelectImageProvider {

    private static SelectImageProvider provider;
    private OnSelectChangedListener listener;
    public  int maxSelect = 10;

    public static SelectImageProvider getInstance() {
        if (provider == null) {
            provider = new SelectImageProvider();
        }
        return provider;
    }

    private SelectImageProvider() {
    }

    public void setMaxSelect(int maxSelect){
        this.maxSelect = maxSelect;
    }

    private ArrayList<String> selectedImgs = new ArrayList<>();

    public void remove(String path) {
        selectedImgs.remove(path);
        if(listener != null) {
            listener.onSelectChanged();
        }
    }

    public void add(String path) {
        selectedImgs.add(path);
        if(listener != null) {
            listener.onSelectChanged();
        }
    }

    public ArrayList<String> getSelectedImgs() {
        return selectedImgs;
    }

    public int getLastIndex() {
        return selectedImgs.size() - 1;
    }

    public int getCount(){
        return selectedImgs.size();
    }

    public void clear() {
        selectedImgs.clear();
    }

    public boolean isSelectedMax(){
        return selectedImgs.size() == maxSelect;
    }
    public interface OnSelectChangedListener {
        void onSelectChanged();
    }

    public void setOnSelectChangedListener(OnSelectChangedListener listener) {
        this.listener = listener;
    }
}
