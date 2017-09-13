package com.example.apple.glidetest.bean

import java.io.File
import java.io.Serializable

/**
 * Created by Apple on 17/9/11.
 */

class Media(var date: String, var path: String, var size: String, var type: MediaType, var duration: String="0:00") : Serializable {
    var dir: String = ""
        get() {
            if (File(path).exists())
                return File(path).parentFile.absolutePath
            else return field
        }

    enum class MediaType {
        VID,
        IMG,
        GIF
    }

    var isVideo: Boolean = false
        get() {
            return type == MediaType.VID
        }
    var isGif: Boolean = false
        get() {
            return type == MediaType.GIF
        }

    override fun toString(): String {
        return "date: " + date + "\nsize: " + size + "\npath:" + path + "\nmediaType:" + type
    }
}
