package com.example.apple.glidetest.bean

/**
 * Created by Apple on 17/7/17.
 */

class Change(private val type: Int, var path: String) {

    val isAdd: Boolean
        get() = type == ADD

    companion object {
        val ADD = 0
        val REMOVE = 1
    }

}
