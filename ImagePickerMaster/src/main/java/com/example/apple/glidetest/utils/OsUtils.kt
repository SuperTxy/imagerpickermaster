package com.example.apple.glidetest.utils

/**
 * Created by Apple on 17/5/31.
 */

object OsUtils {
    fun getIndexInList(list: List<String>, str: String): Int {
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                if (list[i] == str) {
                    return i
                }
            }
        }
        return -1
    }
}
