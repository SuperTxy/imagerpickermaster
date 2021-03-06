package com.supertxy.media.media

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.view.MotionEvent
import android.view.Surface
import java.io.File
import java.util.*

/**
 * Created by Apple on 17/9/23.
 */
private val sizeComparator = CameraSizeComparator()

fun getSuitableSize(list: List<Camera.Size>, th: Int, rate: Float): Camera.Size {
    Collections.sort(list, sizeComparator)
    for (it in list) {
        if (it.width > th && equalRate(it, rate))
            return it
    }
    return getBestSize(list, rate)
}

private fun getBestSize(list: List<Camera.Size>, rate: Float): Camera.Size {
    var previewDisparity = 100f
    var size = list.get(0)
    for (it in list) {
        val prop = it.width.toFloat() / it.height.toFloat()
        if (Math.abs(rate - prop) < previewDisparity) {
            previewDisparity = Math.abs(rate - prop)
            size = it
        }
    }
    return size
}

private fun equalRate(s: Camera.Size, rate: Float): Boolean {
    val r = s.width.toFloat() / s.height.toFloat()
    return Math.abs(r - rate) <= 0.2
}

fun setCameraDisplayOrientation(activity: Activity,
                                cameraId: Int, camera: android.hardware.Camera): Int {
    val info = android.hardware.Camera.CameraInfo()
    android.hardware.Camera.getCameraInfo(cameraId, info)
    val rotation = activity.windowManager.defaultDisplay.rotation
    var degrees = 0
    when (rotation) {
        Surface.ROTATION_0 -> degrees = 0
        Surface.ROTATION_90 -> degrees = 90
        Surface.ROTATION_180 -> degrees = 180
        Surface.ROTATION_270 -> degrees = 270
    }

    var result: Int
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        result = (info.orientation + degrees) % 360
        result = (360 - result) % 360  //  compensate the mirror
    } else {  // back-facing
        result = (info.orientation - degrees + 360) % 360
    }
    camera.setDisplayOrientation(result)
    return result
}

fun setCameraParameters(camera: Camera, screenProp: Float): Camera.Size {
    val parameters = camera.getParameters() // 获取相机参数
    val previewSize = getSuitableSize(parameters.supportedPreviewSizes, 1000, screenProp)
    val pictureSize = getSuitableSize(parameters.supportedPictureSizes, 1200, screenProp)
    parameters?.setPreviewSize(previewSize.width, previewSize.height) // 设置预览大小
    parameters?.setPictureSize(pictureSize.width, pictureSize.height) // 设置保存的图片尺寸
    val pictureFormats = parameters!!.supportedPictureFormats
    for (it in pictureFormats) {
        if (it == ImageFormat.JPEG) {
            parameters.setPictureFormat(ImageFormat.JPEG)
            parameters.setJpegQuality(100)
            break
        }
    }
    if (!setFoucusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, parameters))
        setFoucusMode(Camera.Parameters.FOCUS_MODE_AUTO, parameters)

    camera.setParameters(parameters)
    return previewSize
}

fun setFoucusMode(focusMode: String, params: Camera.Parameters): Boolean {
    if (params.supportedFocusModes.contains(focusMode)) {
        params.focusMode = focusMode
        return true
    }
    return false
}

fun calculateTapArea(x: Float, y: Float, coefficient: Float, width: Int, height: Int): Rect {
    val focusAreaSize = 300f
    val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient)!!.toInt()
    val centerX = (x / width * 2000 - 1000).toInt()
    val centerY = (y / height * 2000 - 1000).toInt()

    val halfAreaSize = areaSize / 2
    val rectF = RectF(clamp(centerX - halfAreaSize, -1000, 1000).toFloat(), clamp(centerY - halfAreaSize, -1000, 1000).toFloat(), clamp(centerX + halfAreaSize, -1000, 1000).toFloat(), clamp(centerY + halfAreaSize, -1000, 1000).toFloat())
    return Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom))
}

fun clamp(x: Int, min: Int, max: Int): Int {
    if (x > max) {
        return max
    }
    if (x < min) {
        return min
    }
    return x
}

fun getFingerSpacing(event: MotionEvent): Float {
    val x = event.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)
    return Math.sqrt((x * x + y * y).toDouble()).toFloat()
}

fun getSensorAngle(x: Float, y: Float): Int {
    if (Math.abs(x) > Math.abs(y)) {
        /**
         * 横屏倾斜角度比较大
         */
        if (x > 4) {
            /**
             * 左边倾斜
             */
            return 270
        } else if (x < -4) {
            /**
             * 右边倾斜
             */
            return 90
        } else {
            /**
             * 倾斜角度不够大
             */
            return 0
        }
    } else {
        if (y > 7) {
            /**
             * 左边倾斜
             */
            return 0
        } else if (y < -7) {
            /**
             * 右边倾斜
             */
            return 180
        } else {
            /**
             * 倾斜角度不够大
             */
            return 0
        }
    }
}

fun deleteMediaFile(file: File?): File? {
    if (file != null && file.exists()) {
        file.delete()
    }
    return null
}



