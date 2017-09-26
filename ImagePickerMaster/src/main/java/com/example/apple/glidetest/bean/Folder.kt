package com.example.apple.glidetest.bean

import java.util.*

/**
 * Created by Apple on 17/5/26.
 */

class Folder(var dir: String, var name: String) {

    var firstMedia: Media? = null
    var medias = ArrayList<Media>()

    constructor(dir: String, name: String, firstMedia: Media) : this(dir, name) {
        this.firstMedia = firstMedia
    }

    fun addMedia(media: Media, index:Int?=null) {
        if (!medias.contains(media)) {
//            java.lang.ArrayIndexOutOfBoundsException: length=10; index=10  java.lang.ArrayIndexOutOfBoundsException: length=15; index=15
            if (index!=null && index >= 0) medias.add(index,media)
            else medias.add(media)
        }
    }

    fun sort(){
        Collections.sort(medias)
    }

    var count: Int = 0
        get() {return medias.size}

    fun clear(){
        dir = ""
        name =""
        firstMedia = null
        medias.clear()
    }
}
