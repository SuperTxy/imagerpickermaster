package com.supertxy.media.media

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.View
import android.widget.ImageView
import com.txy.androidutils.TxyToastUtils
import java.util.*
/**
 * Created by Apple on 17/9/17.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Helper(private var context: Context, private var toastUtils: TxyToastUtils,
                    private var ivPreview: ImageView, private var surfaceView: MediaSurfaceView) {

    var cameraManager: CameraManager? = null
    var cameraDevice: CameraDevice? = null
    var cameraCaptureSession: CameraCaptureSession? = null
    private var childHandler: Handler? = null
    private var mainHandler: Handler? = null
    private var mImageReader: ImageReader? = null
    private var cameraId: Int = CameraCharacteristics.LENS_FACING_BACK

    private val onImageAvailableListener = object : ImageReader . OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader?) {
            cameraDevice!!.close()
            ivPreview.visibility = View.VISIBLE
            cameraDevice!!.close()
            ivPreview.visibility = View.VISIBLE
            // 拿到拍照照片数据
            val image = reader!!.acquireNextImage()
            val buffer = image.getPlanes()[0].getBuffer()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)//由缓冲区存入字节数组
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            cameraDevice = camera
//            开启预览
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            if (cameraDevice != null) {
                cameraDevice!!.close()
            }
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            toastUtils.toast("摄像头开启失败")
        }
    }

    init {
        initCamera2()
    }

    private fun initCamera2() {
        val handlerThread = HandlerThread("Camera2")
        handlerThread.start()
        childHandler = Handler(handlerThread.getLooper())
        mainHandler = Handler(Looper.getMainLooper())
        mImageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height, ImageFormat.JPEG, 1)
        mImageReader!!.setOnImageAvailableListener(onImageAvailableListener, mainHandler)
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager!!.openCamera(cameraId.toString(), stateCallback, mainHandler)
    }


    private fun startPreview() {
        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(surfaceView.holder.getSurface())
        // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
        cameraDevice!!.createCaptureSession(Arrays.asList(surfaceView.holder.getSurface(), mImageReader!!.getSurface()), object : CameraCaptureSession .StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession?) {
                toastUtils.toast("配置失败")
            }

            override fun onConfigured(session: CameraCaptureSession?) {
                // 当摄像头已经准备好时，开始显示预览
                cameraCaptureSession = session
                // 自动对焦
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                // 打开闪光灯
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                // 显示预览
                val previewRequest = previewRequestBuilder.build()
                cameraCaptureSession!!.setRepeatingRequest(previewRequest, null, childHandler)
            }
        }, childHandler)
    }

    private val mSessionCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            //重启预览
            startPreview()
        }
    }
    
    fun takePicture() {
        val mPreviewBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        //获取屏幕方向
        val rotation = (context as Activity).windowManager.getDefaultDisplay().getRotation()
//设置CaptureRequest输出到mImageReader
//CaptureRequest添加imageReaderSurface，不加的话就会导致ImageReader的onImageAvailable()方法不会回调
        mPreviewBuilder.addTarget(mImageReader!!.getSurface())
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, degrees)
//聚焦
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

//停止预览
        cameraCaptureSession!!.stopRepeating()
//开始拍照，然后回调上面的接口重启预览，因为mPreviewBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
        cameraCaptureSession!!.capture(mPreviewBuilder.build(), mSessionCaptureCallback, null)
    }

    fun releaseCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }
}