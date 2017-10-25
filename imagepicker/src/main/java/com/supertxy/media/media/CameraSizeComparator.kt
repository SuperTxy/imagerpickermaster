package com.supertxy.media.media

import android.hardware.Camera

/**
 * Created by Apple on 17/9/24.
 */
class CameraSizeComparator :Comparator<Camera.Size>{
    override fun compare(o1: Camera.Size?, o2: Camera.Size?): Int {
        if (o1!!.width == o2!!.width) return 0
        else if(o1.width > o2.width) return 1
        else return -1
    }

}