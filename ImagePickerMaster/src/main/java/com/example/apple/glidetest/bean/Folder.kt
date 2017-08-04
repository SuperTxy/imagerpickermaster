package com.example.apple.glidetest.bean

/**
 * Created by Apple on 17/5/26.
 */

class Folder(dir: String, name: String) {
    var dir: String
    var name: String
    var firstImagePath: String? = null
    var imgs = ArrayList<String>()

    init {
        this.dir = dir
        this.name = name
    }

    constructor(dir: String, name: String, firstImagePath: String) : this(dir, name) {
        this.firstImagePath = firstImagePath
    }

    fun addImage(path: String) {
        if (path.isNotEmpty() && !imgs.contains(path)) {
            imgs.add(path)
        }
    }

    var count: Int = 0
        get() {return imgs.size}
}