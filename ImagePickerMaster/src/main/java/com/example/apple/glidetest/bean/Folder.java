package com.example.apple.glidetest.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apple on 17/5/26.
 */

public class Folder {

    public String dir;
    public String name;
    public String firstImagePath;
    public List<String> imgs = new ArrayList<>();

    public Folder(String dir, String name) {
        this.dir = dir;
        this.name = name;
    }

    public Folder(String dir, String name, String firstImagePath) {
        this.dir = dir;
        this.name = name;
        this.firstImagePath = firstImagePath;
    }

    public void addImage(String path) {
        if (!TextUtils.isEmpty(path)) {
            imgs.add(path);
        }
    }

    public int getCount() {
        return imgs == null ? 0 : imgs.size();
    }
}
