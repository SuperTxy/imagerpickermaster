package com.example.apple.glidetest.bean

import android.text.TextUtils
import java.io.File

/**
 * Created by Apple on 17/7/31.
 */
class FolderProvider private constructor() {

    private var foldersMap = HashMap<String, Folder>()
    var selectedFolder: Folder? = null
        set(value) {
            field = if (value != null) value else throw IllegalStateException("不能设置selectedFolder为null")
        }
        get() {
            return field ?: throw IllegalStateException("selectedFolder为null")
        }

    var folders = ArrayList<Folder>()
        get() = field

    init {
        selectedFolder = Folder("", "所有图片")
        folders.add(selectedFolder!!)
    }

    companion object {
        val instance: FolderProvider by lazy { FolderProvider() }
    }

    fun addFolder(folder: Folder) {
        folders.add(folder)
        foldersMap.put(folder.dir, folder)
    }

    fun hasFolder(dir: String): Boolean {
        return foldersMap.containsKey(dir)
    }

    fun getFolderByDir(dir: String): Folder? {
        return foldersMap.get(dir)
    }

    fun clear(){
        folders.clear()
        foldersMap.clear()
    }

    fun addCameraImage(path:String){
        val dir = File(path).parentFile.absolutePath
        if (!hasFolder(dir)) {
            val name = dir.substring(dir.lastIndexOf('/') + 1)
            addFolder(Folder(dir, name, path))
        }
        getFolderByDir(dir)!!.addImage(path,0)
        if (!TextUtils.equals(getFolderByDir(dir)!!.name,folders.get(0).name) ) {
            folders.get(0).addImage(path,0)
            folders.get(0).firstImagePath = path
        }
        getFolderByDir(dir)!!.firstImagePath = path
    }

    val count: Int
        get() {
            return folders.size
        }

}