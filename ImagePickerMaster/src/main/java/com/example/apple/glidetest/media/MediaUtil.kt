package com.example.apple.glidetest.media

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.view.MotionEvent
import android.view.Surface
import com.orhanobut.logger.Logger

/**
 * Created by Apple on 17/9/23.
 */

private val DEFAULT_WIDTH = 1920
private val DEFAULT_HEIGHT = 1080

fun getBestPreviewSizeValue(sizeList: List<Camera.Size>): Point {
    var bestX = 0
    var bestY = 0
    var size = 0
    for (it in sizeList){
        if (it.width == DEFAULT_WIDTH && it.height == DEFAULT_HEIGHT) {
            Logger.d("get default preview size!!!")
            return Point(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        }
    }
    for (it in sizeList) {
        val newSize = Math.abs(it.width * it.width) + Math.abs(it.height * it.height)
        val ratio = it.height.toFloat() / it.width.toFloat()
        if (newSize >= size && ratio == 9f/16f) {
            bestX = it.width
            bestY = it.height
            size = newSize
        } else if (newSize < size) continue
    }
    if (bestX > 0 && bestY > 0)
        return Point(bestX, bestY)
    else return Point(sizeList.get(0).width, sizeList.get(0).height)
}

fun getBestPictureSizeValue(sizeList: List<Camera.Size>, screenResolution: Point): Point {
    val tempList = ArrayList<Camera.Size>()
    for (it in sizeList) {
        if (it.width == DEFAULT_WIDTH && it.height == DEFAULT_HEIGHT) {
            Logger.d("get default picture size!!!")
            return Point(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        }
        if (it.width >= screenResolution.x && it.height >= screenResolution.y)
            tempList.add(it)
    }
    var bestX = 0
    var bestY = 0
    var diff = Integer.MAX_VALUE
    if (tempList.size > 0) {
        for (it in tempList) {
            val newDiff = Math.abs(it.width - screenResolution.x) + Math.abs(it.height - screenResolution.y)
            val ratio = it.height.toFloat() / it.width.toFloat()
            if (newDiff < diff && ratio == 9f/16f) {
                bestX = it.width
                bestY = it.height
                diff = newDiff
            }
        }
    }
    if (bestX > 0 && bestY > 0)
        return Point(bestX, bestY)
    else return Point(sizeList.get(7).width, sizeList.get(7).height)
}

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

fun setCameraParameters(camera: Camera): Point {
    val parameters = camera.getParameters() // 获取相机参数
    val previewSize = getBestPreviewSizeValue(parameters.supportedPreviewSizes)
    val screenResolution = Point(parameters.pictureSize.width,parameters.pictureSize.height)
    val pictureSize = getBestPictureSizeValue(parameters.supportedPictureSizes,screenResolution)
    Logger.e(previewSize.x.toString()+"--->previewSize-->"+previewSize.y)
    Logger.e(pictureSize.x.toString()+"--->pictureSize-->"+pictureSize.y)
//        holder.setFixedSize(width, height)//照片的大小
    parameters?.setPictureFormat(ImageFormat.JPEG) // 设置图片格式
    parameters?.setPreviewSize(previewSize.x, previewSize.y) // 设置预览大小
    parameters?.setPictureSize(pictureSize.x, pictureSize.y) // 设置保存的图片尺寸
    parameters?.setJpegQuality(100) // 设置照片质量
    val supportedFocusModes = parameters?.getSupportedFocusModes()
    if (supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)//连续对焦
        camera.cancelAutoFocus()//如果要实现连续的自动对焦，这一句必须加上
    } else if(supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
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

fun getFingerSpacing(event: MotionEvent): Float {
    val x = event.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)
    return Math.sqrt((x * x + y * y).toDouble()).toFloat()
}



