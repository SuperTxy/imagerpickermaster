package com.example.apple.glidetest.utils;

import android.util.Log;

import java.util.List;

/**
 * Created by Apple on 17/5/27.
 */

public class ListUtils {
    public static int getIndexInList(List<String> list,String str ){
        if(list == null || list.size() == 0 || str == null) {
            Log.e("ListUtils","List不可为null或者空！");
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(str)) {
                return i;
            }
        }
        return -1;
    }
}
