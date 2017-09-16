package com.example.apple.glidetest.media

import android.content.Context
import android.hardware.Camera
import android.preference.PreferenceManager
import com.orhanobut.logger.Logger

/**
 * Created by Apple on 17/9/16.
 */
class SizeUtils(private val camera: Camera) {
    private var previewSizes: List<Camera.Size>? = null
    private var pictureSizes: List<Camera.Size>? = null
    private var videoSizes: List<Camera.Size>? = null
    var size: Camera.Size? = null

     fun getConsistentSize(context: Context): Camera.Size {
        previewSizes = camera.parameters.supportedPreviewSizes
        pictureSizes = camera.parameters.supportedPictureSizes
        videoSizes = camera.parameters.supportedVideoSizes
        size = camera.Size(480, 640)
        if (!isSizeSuit()) {
            size = camera.Size(480, 720)
            if (!isSizeSuit()) {
//                TODO("遍历")
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("width",size!!.width).putInt("height",size!!.height).apply()
        return size!!
    }

    private fun isSizeSuit(): Boolean {
        Logger.d(size!!.width.toString() + "-->" + size!!.height)
        return containsSize(previewSizes!!) && containsSize(pictureSizes!!) && containsSize(videoSizes!!)
    }

    private fun containsSize(list: List<Camera.Size>): Boolean {
        for (it in list) {
            if (it.width == size!!.width && it.height == size!!.height) return true
        }
        return false
    }
}