package com.example.apple.glidetest.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Apple on 17/5/27.
 * 图片目录管理
 */

public class FolderProvider {

    private static FolderProvider instance;
    private Folder selectedFolder;

    public static FolderProvider getInstance() {
        if (instance == null) {
            instance = new FolderProvider();
        }
        return instance;
    }

    private FolderProvider() {
        selectedFolder = new Folder("", "所有图片");
        folders.add(selectedFolder);
    }

    public Folder getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(Folder selectedFolder) {
        this.selectedFolder = selectedFolder;
    }

    public List<Folder> folders = new ArrayList<>();
    public HashMap<String, Folder> foldersMap = new HashMap<>();

    public void addFolder(Folder folder) {
        folders.add(folder);
        foldersMap.put(folder.dir, folder);
    }

    public boolean hasFolder(String dir) {
        return foldersMap.get(dir) != null;
    }

    public Folder getFolderByDir(String dir) {
        return foldersMap.get(dir);
    }

    public int getCount() {
        return folders.size();
    }

    public List<Folder> getFolders() {
        return folders;
    }
}
