package com.example.apple.glidetest.media

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.view.MotionEvent
import android.view.Surface
import com.orhanobut.logger.Logger
import java.util.*

/**
 * Created by Apple on 17/9/23.
 */
private val sizeComparator = CameraSizeComparator()
fun getPreviewOrPictureSize(list:List<Camera.Size> ,th:Int,rate:Float):Camera.Size{
    Collections.sort(list, sizeComparator)
    for (it in list){
        if (it.width > th && equalRate(it,rate))
            return  it
    }
    return getBestSize(list,rate)
}

private fun getBestSize(list:List<Camera.Size>,rate:Float):Camera.Size{
    var previewDisparity = 100f
    var size = list.get(0)
    for (it in list){
        val prop = it.width.toFloat() / it.height.toFloat()
        if (Math.abs(rate -prop) < previewDisparity){
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

fun setCameraParameters(camera: Camera,screenProp:Float): Camera.Size {
    val parameters = camera.getParameters() // 获取相机参数
    val previewSize = getPreviewOrPictureSize(parameters.supportedPreviewSizes,1000,screenProp)
    val pictureSize = getPreviewOrPictureSize(parameters.supportedPictureSizes,2000,screenProp)
    Logger.e(previewSize.width.toString()+"--->previewSize-->"+previewSize.height)
    Logger.e(pictureSize.width.toString()+"--->pictureSize-->"+pictureSize.height)
//        holder.setFixedSize(width, height)//照片的大小
    parameters?.setPictureFormat(ImageFormat.JPEG) // 设置图片格式
    parameters?.setPreviewSize(previewSize.width, previewSize.height) // 设置预览大小
    parameters?.setPictureSize(pictureSize.width, pictureSize.height) // 设置保存的图片尺寸
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



