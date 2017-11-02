package com.supertxy.media.media

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.*
import android.media.AudioManager
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import com.orhanobut.logger.Logger
import com.supertxy.media.R
import com.supertxy.media.bean.Media
import com.txy.androidutils.TxyFileUtils
import com.txy.androidutils.TxyScreenUtils
import com.txy.androidutils.TxyToastUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Apple on 17/9/16.
 */
class MediaSurfaceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : SurfaceView(context, attrs, defStyleAttr), Camera.PreviewCallback {

    var camera: Camera? = null
    var camerasCount = 1
    private var mediaRecorder: MediaRecorder? = null
    var mediaFile: File? = null
    private var currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
    private var cameraFlashType = Camera.Parameters.FLASH_MODE_AUTO
    private var videoFlashType = Camera.Parameters.FLASH_MODE_OFF
    var isCamera: Boolean = false
    private var toastUtils: TxyToastUtils? = null
    private var screenProp = -1f
    private var cameraAngle = 90
    private var angle = 0
    private var sm: SensorManager? = null
    private var isPlaying: Boolean = false
    var isShowPicture: Boolean = false
    private var player: MediaPlayer? = null
    var media: Media? = null

    private var surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            if (isPlaying) {
                stopVideo()
                isPlaying = true
            } else if (isCamera && mediaFile != null)
                isShowPicture = true
            else stopPreview()
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            if (isPlaying)
                playVideo()
            else if (isShowPicture)
                listener?.reviewImage()
            else startPreview(holder!!)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (screenProp < 0) {
            screenProp = measuredHeight.toFloat() / measuredWidth
        }
    }

    init {
        getCamera()
        toastUtils = TxyToastUtils(context)
        camerasCount = Camera.getNumberOfCameras()
        holder.setKeepScreenOn(true)
        holder.addCallback(surfaceCallBack)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.type) {
                return
            }
            val values = event.values
            angle = getSensorAngle(values[0], values[1])
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun takePicture() {
        var nowAngle: Int = 0
        if (cameraAngle == 90)
            nowAngle = Math.abs(angle + cameraAngle) % 360
        else if (cameraAngle == 270)
            nowAngle = Math.abs(cameraAngle - angle)
        camera!!.takePicture(null, null, object : Camera.PictureCallback {
            override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                mediaFile = TxyFileUtils.createIMGFile(context)
                val fos = FileOutputStream(mediaFile)
                val matrix = Matrix()
                if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    matrix.setRotate(nowAngle.toFloat())
                } else {
                    matrix.setRotate(360f - nowAngle)
                    matrix.postScale(-1f, 1f)
                }
                var bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                media = Media(null, mediaFile!!.absolutePath, null, Media.MediaType.IMG)
                media!!.date = System.currentTimeMillis().toString()
                media!!.size = mediaFile!!.length().toString()
                listener?.finish()
                camera?.stopPreview()
            }
        })
    }

    fun resetState() {
        mediaFile = deleteMediaFile(mediaFile)
        if (isPlaying) stopVideo()
        startPreview(holder)
        val lp = this.layoutParams
        lp.height = TxyScreenUtils.getScreenHeight(context)
        this.layoutParams = lp
    }

    fun startRecord() {
        camera!!.unlock()
        resetState()
        mediaFile = TxyFileUtils.createVIDFile(context)
        mediaRecorder = MediaRecorder()
        mediaRecorder?.reset()
        mediaRecorder?.setCamera(camera)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
//        设置视频输出格式和编码
        mediaRecorder?.setProfile(CamcorderProfile.get(currentCameraFacing, CamcorderProfile.QUALITY_480P))
        mediaRecorder?.setOrientationHint(getAngle())
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

    private fun getAngle(): Int {
        var nowAngle = (angle + 90) % 360
        if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if (cameraAngle == 270) {
                if (nowAngle == 0) nowAngle = 180
                else if (nowAngle == 270) nowAngle = 270
                else nowAngle = 90
            } else {
                if (nowAngle == 90) nowAngle = 270
                else if (nowAngle == 270) nowAngle = 90
            }
        }
        return nowAngle
    }

    fun stopRecord() {
        mediaRecorder?.setOnErrorListener(null)
        mediaRecorder?.setOnInfoListener(null)
        mediaRecorder?.setPreviewDisplay(null)
        try {
            mediaRecorder?.stop()
        } catch(e: RuntimeException) {
            Logger.e(e.message)
            mediaRecorder = null
            mediaRecorder = MediaRecorder()
            toastUtils!!.toast(context.getString(R.string.record_time_is_too_short))
            if (mediaFile != null && mediaFile!!.exists()) {
                mediaFile!!.delete()
                mediaFile = null
            }
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
        if (mediaFile != null) {
            stopPreview()
            media = Media(null, mediaFile!!.absolutePath, null, Media.MediaType.VID)
            media!!.date = System.currentTimeMillis().toString()
            media!!.size = mediaFile!!.length().toString()
            playVideo()
            listener?.finish()
        }
    }

    private var oldDist = 1f

    private var downX = 0f
    private var downY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val upX = event.rawX
                    val upY = event.rawY
                    val dX = upX - downX
                    if (Math.abs(upY - downY) <= Math.abs(dX)) {
                        if (Math.abs(dX) < 5) {
                            listener?.touchFocus(event)
                            handleFocusMetering(event)
                        } else {
                            if (dX < 0 && isCamera || (dX > 0 && !isCamera)) {
                                listener?.switch()
                            }
                        }
                    }
                }
            }
        } else when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN -> oldDist = getFingerSpacing(event)
            MotionEvent.ACTION_MOVE -> {
                val newDist = getFingerSpacing(event)
                val zoomGradient = (width / 16f).toInt()
                val scaleDis = newDist - oldDist
                if (scaleDis.toInt() / zoomGradient != 0) {
                    if (newDist > oldDist) {
                        handleZoom(scaleDis)
                    } else if (newDist < oldDist) {
                        handleZoom(scaleDis)
                    }
                    oldDist = newDist
                }
            }
        }
        return true
    }

    private fun handleZoom(scaleDis: Float) {
        val params = camera!!.parameters
        if (params.isZoomSupported) {
            val maxZoom = params.maxZoom
            val scaleZoom = scaleDis / (width / 2) * maxZoom
            var zoom = params.zoom + scaleZoom.toInt()
            if (zoom > maxZoom) zoom = maxZoom
            else if (zoom < 0) zoom = 0
            params.zoom = zoom
            camera!!.parameters = params
            Logger.e(zoom.toString() + "--------------")
        } else {
            Logger.i("zoom not supported")
        }
    }

    private fun handleFocusMetering(event: MotionEvent) {
        val focusRect = calculateTapArea(event.x, event.y, 1f, width, height)
        val meteringRect = calculateTapArea(event.x, event.y, 1.5f, width, height)
        camera!!.cancelAutoFocus()
        val params = camera!!.parameters
        if (params.maxNumFocusAreas > 0) {
            val focusAreas = java.util.ArrayList<Camera.Area>()
            focusAreas.add(Camera.Area(focusRect, 800))
            params.focusAreas = focusAreas
        } else {
            Logger.i("focus areas not supported")
            listener?.onFocusSuccess()
            return
        }
        if (params.maxNumMeteringAreas > 0) {
            val meteringAreas = java.util.ArrayList<Camera.Area>()
            meteringAreas.add(Camera.Area(meteringRect, 800))
            params.meteringAreas = meteringAreas
        } else {
            Logger.i("metering areas not supported")
            listener?.onFocusSuccess()
            return
        }
        val currentFocusMode = params.focusMode
        try {
            params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            camera!!.parameters = params
            camera!!.autoFocus { success, camera ->
                val params1 = camera.parameters
                params.focusMode = currentFocusMode
                camera.parameters = params1
                listener?.onFocusSuccess()
            }
        } catch (e: Exception) {
            Logger.e(e.message + "--->autoFocus fail")
        }
    }

    fun changeCameraFacing(ivFlash: ImageView): Int {
        if (camerasCount > 1) {
            if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT
                ivFlash.visibility = View.INVISIBLE
            } else {
                ivFlash.visibility = View.VISIBLE
                currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
            }
            releaseCamera()
            camera = Camera.open(currentCameraFacing)
            setCameraParameters(camera!!, screenProp)
            startPreview(holder)
        } else toastUtils!!.toast("手机不支持前置摄像头！")
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
        if (camera == null) getCamera()
        val parameters = camera!!.parameters
        parameters.flashMode = flashType
        camera!!.parameters = parameters
    }

    private fun playVideo() {
        if (mediaFile == null || !mediaFile!!.exists()) return
        if (player == null)
            player = MediaPlayer()
        player!!.reset()
        player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player!!.setDataSource(mediaFile!!.absolutePath)
        player!!.setSurface(holder.surface)
        player!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        player!!.setOnVideoSizeChangedListener { mp, width, height ->
            updateVideoViewSize(player!!.getVideoHeight().toFloat() / player!!.getVideoWidth())
        }
        player!!.isLooping = true
        player!!.setScreenOnWhilePlaying(true)
        player!!.setOnPreparedListener {
            if (media != null && media!!.duration == 0L) {
                media?.duration = player!!.duration.toLong()
                media?.width = player!!.videoWidth.toString()
                media?.height = player!!.videoHeight.toString()
                Logger.e(media?.toString())
            }
            player!!.start()
            isPlaying = true
        }
        player!!.setOnErrorListener { mp, what, extra ->
            Logger.e("mediaplayer   onError------------" + what + "extra-->" + extra)
            false
        }
        player!!.prepare()
    }

    fun stopVideo() {
        isPlaying = false
        if (player != null && player!!.isPlaying) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    private fun updateVideoViewSize(rate: Float) {
        val screenWidth = TxyScreenUtils.getScreenWidth(context)
        val lp = this.layoutParams
        lp.width = screenWidth
        lp.height = (screenWidth * rate).toInt()
        this.layoutParams = lp
    }

    interface OnMediaListener {
        fun finish()
        fun onFocusSuccess()
        fun touchFocus(event: MotionEvent)
        fun switch()
        fun reviewImage()
    }

    private var listener: OnMediaListener? = null

    fun setOnMediaFinishListener(listener: OnMediaListener) {
        this.listener = listener
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
//  每一帧的回调
    }

    fun startPreview(holder: SurfaceHolder) {
        camera!!.setPreviewCallback(this)
        try {
            camera!!.setPreviewDisplay(holder)
            cameraAngle = setCameraDisplayOrientation(context as Activity, currentCameraFacing, camera!!)
            setCameraParameters(camera!!, screenProp)
            Logger.e("-----startPreview-------")
            camera!!.startPreview()
        } catch(e: IOException) {
            Logger.e(e.message)
            Logger.e(holder.isCreating.toString())
        }
    }

    private fun stopPreview() {
        try {
            Logger.e("-----stopPreview-------")
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
            camera?.setPreviewDisplay(null)
        } catch (e: IOException) {
            Logger.e(e.message)
        }
    }

    fun registerSensorManager(context: Context) {
        if (sm == null) {
            sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        sm!!.registerListener(sensorEventListener, sm!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL)
    }

    fun unregisterSensorManager(context: Context) {
        if (sm == null) {
            sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        sm!!.unregisterListener(sensorEventListener)
    }

    fun releaseCamera() {
        try {
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
//            这句要在stopPreview后执行，不然会卡顿或者花屏
            camera?.setPreviewDisplay(null)
            camera?.release()
            camera = null
        } catch(e: Exception) {
            Logger.e(e.message)
        }
    }

    fun getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open(currentCameraFacing)
            } catch(e: Exception) {
                Logger.e(e.message)
            }
        }
    }

    fun destroy() {
        toastUtils?.destroy()
        mediaRecorder?.release()
        mediaRecorder = null
        releaseCamera()
    }
}