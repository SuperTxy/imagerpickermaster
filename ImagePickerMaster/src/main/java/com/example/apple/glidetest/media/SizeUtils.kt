package com.example.apple.glidetest.media

import android.hardware.Camera

/**
 * Created by Apple on 17/9/16.
 */
class SizeUtils( camera: Camera) {

    private var previewSizes: List<Camera.Size>? = null
    private var pictureSizes: List<Camera.Size>? = null
    var previewSize: Camera.Size? = null
    var pictureSize: Camera.Size? = null

    init {
        previewSizes = camera.parameters.supportedPreviewSizes
        pictureSizes = camera.parameters.supportedPictureSizes
        pictureSize = pictureSizes!!.get(7)
        previewSize = previewSizes!!.get(0)
    }

    fun getLargestPreviewSize(){
        for (it in previewSizes!!){

        }
    }
}