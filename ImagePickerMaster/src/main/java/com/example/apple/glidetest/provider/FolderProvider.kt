package com.example.apple.glidetest.provider

import android.text.TextUtils
import com.example.apple.glidetest.bean.Folder
import com.example.apple.glidetest.bean.Media

/**
 * Created by Apple on 17/7/31.
 */
class FolderProvider private constructor() {

    private var foldersMap = HashMap<String, Folder>()
    var folders = ArrayList<Folder>()
    var allFolder: Folder? = null
    var selectedFolder: Folder? = null
        set(value) {
            field = if (value != null) value else throw IllegalStateException("不能设置selectedFolder为null")
        }
        get() {
            return field ?: throw IllegalStateException("selectedFolder为null")
        }

    init {
        allFolder = Folder("", "所有图片")
        addFolder(allFolder!!)
        selectedFolder = allFolder
    }

    companion object {
        val instance: FolderProvider by lazy { FolderProvider() }
    }

    fun addFolder(folder: Folder) {
        if (!foldersMap.containsKey(folder.dir)) {
            folders.add(folder)
            foldersMap.put(folder.dir, folder)
        }
    }

    fun hasFolder(dir: String): Boolean {
        return foldersMap.containsKey(dir)
    }

    fun getFolderByDir(dir: String): Folder? {
        return foldersMap.get(dir)
    }

    fun clear() {
        folders.clear()
        foldersMap = HashMap<String, Folder>()
        allFolder = Folder("", "所有图片")
        selectedFolder = allFolder
        addFolder(selectedFolder!!)
    }

    fun addCameraImage(media: Media) {
        if (!hasFolder(media.dir)) {
            val name = media.dir.substring(media.dir.lastIndexOf('/') + 1)
            addFolder(Folder(media.dir, name, media))
        }
        getFolderByDir(media.dir)!!.addMedia(media, 0)
        if (!TextUtils.equals(getFolderByDir(media.dir)!!.name, allFolder!!.name)) {
            allFolder!!.addMedia(media, 0)
            allFolder!!.firstMedia = media
        }
        getFolderByDir(media.dir)!!.firstMedia = media
    }

    val count: Int
        get() {
            return folders.size
        }

}