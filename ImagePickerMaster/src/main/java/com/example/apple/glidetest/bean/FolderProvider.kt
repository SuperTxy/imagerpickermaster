package com.example.apple.glidetest.bean

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

    val count: Int
        get() {
            return folders.size
        }

}