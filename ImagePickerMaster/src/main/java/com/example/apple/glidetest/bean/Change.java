package com.example.apple.glidetest.bean;

/**
 * Created by Apple on 17/7/17.
 */

public class Change {

    public static final int ADD = 0;
    public static final int REMOVE = 1;
    private int type;
    public String path;

    public Change(int type, String path) {
        this.type = type;
        this.path = path;
    }

    public boolean isAdd() {
        return type == ADD;
    }

//    public int getAllIndex() {
//        List<String> imgs = FolderProvider.getInstance().getSelectedFolder().imgs;
//        return ListUtils.getIndexInList(imgs, path)+1;
//    }
}
