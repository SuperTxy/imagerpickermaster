package com.example.apple.glidetest.bean

import com.example.apple.glidetest.utils.mills2Duration
import java.io.File
import java.io.Serializable

/**
 * Created by Apple on 17/9/11.
 */

class Media(var date: String? = null, var path: String, var size: String? = null, var type: MediaType, var duration: Long = 0L)
    : Serializable, Comparable<Media> {

    override fun compareTo(other: Media): Int {
        if (this.date == null) return -1
        else if (other.date == null) return 1
        else return (other.date!!.toLong() - this.date!!.toLong()).toInt()
    }

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
    var durationStr: String = "00:00"
        get() {
            if (duration == 0L) return "00:00"
            else return mills2Duration(duration)
        }
    var isDurationlarge12: Boolean = false
        get() {
            if (isVideo) {
                return duration >= 13 * 1000
            } else return false
        }
    var isSizeLarge10M: Boolean = false
        get() {
            if (size != null) {
                return size!!.toLong() >= 10 * 1024 * 1024
            }
            return false
        }

    override fun toString(): String {
        return "date: " + date + "\nsize: " + size + "\npath:" + path + "\nmediaType:" +
                type + "\nduration: " + duration + "\nwidth: " + width + "\nheight: " + height
    }
}
