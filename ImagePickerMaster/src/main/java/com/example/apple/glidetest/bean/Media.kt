package com.example.apple.glidetest.bean

import java.io.File
import java.io.Serializable

/**
 * Created by Apple on 17/9/11.
 */

class Media(var date: String?=null, var path: String, var size: String?=null, var type: MediaType, var duration: String = "0:00") : Serializable {
    var dir: String = ""
        get() {
            if (File(path).exists())
                if (isVideo)
                    return "video"
                else return File(path).parentFile.absolutePath
            else return field
        }

    var height: String? = null
    var width: String? = null

    enum class MediaType {
        VID,
        IMG,
        GIF
    }

    var isVideo: Boolean = false
        get() {
            return type == MediaType.VID
        }
    var isIMG: Boolean = false
        get() {
            return type == MediaType.IMG
        }
    var isGif: Boolean = false
        get() {
            return type == MediaType.GIF
        }

    override fun toString(): String {
        return "date: " + date + "\nsize: " + size + "\npath:" + path + "\nmediaType:" + type
    }
}
