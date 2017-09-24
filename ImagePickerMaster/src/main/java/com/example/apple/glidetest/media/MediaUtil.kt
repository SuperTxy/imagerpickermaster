package com.example.apple.glidetest.media

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.view.Surface

/**
 * Created by Apple on 17/9/23.
 */
fun setCameraDisplayOrientation(activity: Activity,
                                cameraId: Int, camera: android.hardware.Camera) {
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
}

fun setCameraParameters(camera: Camera): Camera.Size {
    val previewSize = camera.parameters.supportedPreviewSizes.get(0)
    val pictureSize = camera.parameters.supportedPictureSizes.get(8)
    val parameters = camera.getParameters() // 获取相机参数
//        holder.setFixedSize(width, height)//照片的大小
    parameters?.setPictureFormat(ImageFormat.JPEG) // 设置图片格式
    parameters?.setPreviewSize(previewSize!!.width, previewSize.height) // 设置预览大小
    parameters?.setPictureSize(pictureSize.width, pictureSize.height) // 设置保存的图片尺寸
    parameters?.setJpegQuality(100) // 设置照片质量
    val supportedFocusModes = parameters?.getSupportedFocusModes()
    if (supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)//连续对焦
        camera.cancelAutoFocus()//如果要实现连续的自动对焦，这一句必须加上
    } else {
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)//自动对焦
    }
    camera.setParameters(parameters)
    return previewSize
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



