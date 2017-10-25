package com.supertxy.media.bean

/**
 * Created by Apple on 17/7/17.
 */

class Change(private val type: Int, var media: Media) {

    val isAdd: Boolean
        get() = type == ADD

    companion object {
        val ADD = 0
        val REMOVE = 1
    }
}
