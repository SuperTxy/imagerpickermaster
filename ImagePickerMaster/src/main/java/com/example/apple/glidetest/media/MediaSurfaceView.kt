package com.example.apple.glidetest.media

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.example.apple.glidetest.R
import com.orhanobut.logger.Logger
import com.txy.androidutils.FileUtils
import com.txy.androidutils.ToastUtils
import kotlinx.android.synthetic.main.activity_record_media.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Apple on 17/9/16.
 */
class MediaSurfaceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : SurfaceView(context, attrs, defStyleAttr) {

    var camera: Camera? = null
    var camerasCount = 1
    private var mediaRecorder: MediaRecorder? = null
    var mediaFile: File? = null
    private var currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
    private var cameraFlashType = Camera.Parameters.FLASH_MODE_AUTO
    private var videoFlashType = Camera.Parameters.FLASH_MODE_OFF
    var isCamera: Boolean = false
    private var toastUtils: ToastUtils? = null

    private var surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            setCameraParameters()
            Logger.e(camera!!.parameters.previewSize.width.toString() + "--->" + camera!!.parameters.previewSize.height)
            Logger.e(camera!!.parameters.pictureSize.width.toString() + "--->" + camera!!.parameters.pictureSize.height)
            Logger.e(camera!!.parameters.focusMode)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            Logger.d("surfaceDestroyed---->")
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            Logger.d("surfaceCreated---->")
            startPreview(holder!!)
        }
    }

    init {
        getCamera()
        toastUtils = ToastUtils(context)
        camerasCount = Camera.getNumberOfCameras()
        surfaceView.holder.setKeepScreenOn(true)
        surfaceView.holder.addCallback(surfaceCallBack)
    }

    fun startPreview(holder: SurfaceHolder) {
        camera!!.setPreviewDisplay(holder)
        setCameraDisplayOrientation(context as Activity, currentCameraFacing, camera!!)
        camera!!.startPreview()
        Logger.d("------>startPreview")
    }

    fun takePicture() {
        camera!!.takePicture(null, null, object : Camera.PictureCallback {
            override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                Logger.e("onPictureTaken-->" + data + camera)
                mediaFile = FileUtils.createIMGFile(context)
                val fos = FileOutputStream(mediaFile)
                val matrix = Matrix()
                if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    matrix.setRotate(90f)
                } else {
                    matrix.setRotate(-90f)
                }
                var bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                listener?.afterTakePicture(mediaFile!!)
                camera?.stopPreview()
            }
        })
    }

    fun startRecord(btnRecord: VideoRecordBtn) {
        Logger.d("initMediaRecorder")
        camera!!.unlock()
        mediaFile = FileUtils.createVIDFile(context)
        mediaRecorder = MediaRecorder()
        mediaRecorder?.reset()
        mediaRecorder?.setCamera(camera)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
//        设置视频输出格式和编码
        mediaRecorder?.setProfile(CamcorderProfile.get(currentCameraFacing, CamcorderProfile.QUALITY_480P))
        mediaRecorder?.setOrientationHint(90)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
//        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)
//        mediaRecorder?.setVideoFrameRate(4)
//        mediaRecorder?.setVideoSize(camera!!.parameters.previewSize.width,camera!!.parameters.previewSize.height)
//        mediaRecorder?.setMaxDuration(12000)
        mediaRecorder?.setOutputFile(mediaFile!!.getAbsolutePath())
        mediaRecorder?.setPreviewDisplay(holder.surface)
        try {
            mediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            Logger.e("IllegalStateException preparing MediaRecorder: " + e.message)
            mediaRecorder?.release()
        } catch (e: IOException) {
            Logger.e("IOException preparing MediaRecorder: " + e.message)
            mediaRecorder?.release()
        }
        mediaRecorder?.start()

    }

    fun stopRecord(fail: Boolean) {
        Logger.d("------>stopRecord")
        camera?.stopPreview()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        if (!fail)
            listener?.afterStopRecord(mediaFile!!)
        else camera?.startPreview()
    }

    fun setCameraParameters() {
//        holder.setFixedSize(width, height)//照片的大小
        val sizeUtils = SizeUtils(camera!!)
        val parameters = camera!!.getParameters() // 获取相机参数
        parameters?.setPictureFormat(ImageFormat.JPEG) // 设置图片格式
        parameters?.setPreviewSize(sizeUtils.previewSize!!.width, sizeUtils.previewSize!!.height) // 设置预览大小
        parameters?.setPictureSize(sizeUtils.pictureSize!!.width, sizeUtils.pictureSize!!.height) // 设置保存的图片尺寸
        parameters?.setJpegQuality(100) // 设置照片质量
        val supportedFocusModes = parameters?.getSupportedFocusModes()
        if (supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)//连续对焦
            camera?.cancelAutoFocus()//如果要实现连续的自动对焦，这一句必须加上
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)//自动对焦
        }
        camera?.setParameters(parameters)
    }

    fun changeCameraFacing(ivFlash: ImageView): Int {
        if (camerasCount > 1) {
            currentCameraFacing
            if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT
                ivFlash.visibility = View.INVISIBLE
            } else {
                ivFlash.visibility = View.VISIBLE
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
            }
            releaseCamera()
            camera = Camera.open(currentCameraFacing)
            startPreview(holder)
        } else Logger.e("手机不支持前置摄像头！")
        return currentCameraFacing
    }

    fun switchFlash(ivFlash: ImageView) {
        if (isCamera) {
            when (cameraFlashType) {
                Camera.Parameters.FLASH_MODE_AUTO -> cameraFlashType = Camera.Parameters.FLASH_MODE_OFF
                Camera.Parameters.FLASH_MODE_OFF -> cameraFlashType = Camera.Parameters.FLASH_MODE_ON
                Camera.Parameters.FLASH_MODE_ON -> cameraFlashType = Camera.Parameters.FLASH_MODE_AUTO
            }
        } else {
            if (videoFlashType == Camera.Parameters.FLASH_MODE_OFF)
                videoFlashType = Camera.Parameters.FLASH_MODE_TORCH
            else if (videoFlashType == Camera.Parameters.FLASH_MODE_TORCH)
                videoFlashType = Camera.Parameters.FLASH_MODE_OFF
        }
        setFlashMode(ivFlash)
    }

    fun setFlashMode(ivFlash: ImageView) {
        val flashType = if (isCamera) cameraFlashType else videoFlashType
        when (flashType) {
            Camera.Parameters.FLASH_MODE_AUTO -> ivFlash.setImageResource(R.drawable.flash_auto)
            Camera.Parameters.FLASH_MODE_OFF -> ivFlash.setImageResource(R.drawable.flash_off)
            Camera.Parameters.FLASH_MODE_ON -> ivFlash.setImageResource(R.drawable.flash_on)
            Camera.Parameters.FLASH_MODE_TORCH -> ivFlash.setImageResource(R.drawable.flash_on)
        }
        val parameters = camera!!.parameters
        parameters.flashMode = flashType
        camera!!.parameters = parameters
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

    interface OnMediaFinishListener {
        fun afterTakePicture(mediaFile: File)
        fun afterStopRecord(mediaFile: File)
    }

    private var listener: OnMediaFinishListener? = null

    fun setOnMediaFinishListener(listener: OnMediaFinishListener) {
        this.listener = listener
    }

    fun releaseCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }


    fun getCamera() {
        if (camera == null)
            camera = Camera.open(currentCameraFacing)
    }

    fun destroy() {
        toastUtils?.destroy()
        mediaRecorder?.release()
        mediaRecorder = null
        releaseCamera()
    }
}