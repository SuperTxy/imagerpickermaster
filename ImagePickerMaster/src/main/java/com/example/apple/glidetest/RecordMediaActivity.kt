package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import com.example.apple.glidetest.media.VideoRecordBtn
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.orhanobut.logger.Logger
import com.txy.androidutils.ScreenUtils
import kotlinx.android.synthetic.main.activity_record_media.*
import kotlinx.android.synthetic.main.slide_view.*

class RecordMediaActivity : Activity(), VideoRecordBtn.OnRecordListener {

    private var isCamera: Boolean = false

    companion object {
        fun startForResult(context: Activity, isCamera: Boolean) {
            val intent = Intent(context, RecordMediaActivity::class.java)
            intent.putExtra(PickerSettings.IS_CAMERA, isCamera)
            context.startActivityForResult(intent, PickerSettings.RECORD_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_media)
        StatusBarUtil.setStatusBarColorBlack(this)
        window.setFormat(PixelFormat.TRANSLUCENT)
        btnRecord.setOnRecordListener(this)
        isCamera = intent.getBooleanExtra(PickerSettings.IS_CAMERA,true)
        changeMediaType(isCamera)
        initListener()
        initSurface()
        resetView(false)
    }

    private fun initSurface() {
        ivSwitch.visibility = if (surfaceView!!.camerasCount > 1) View.VISIBLE else View.GONE

        val screenWidth = ScreenUtils.getScreenWidth(this)
//        surfaceView.layoutParams.height = screenWidth * cameraHelper!!.size!!.height / cameraHelper!!.size!!.width
//        surfaceView.layoutParams.width = screenWidth
        Logger.e(surfaceView.width.toString() + "--->" + surfaceView.height)
    }

    override fun onRecordStart(isCamera: Boolean) {
        if (!isCamera) {
//            videoView.visibility = View.GONE
//            surfaceView.visibility = View.VISIBLE
            surfaceView.startRecord()
        }
    }

    override fun onRecordFinish(isCamera: Boolean) {
        if (isCamera) {
            surfaceView.takePicture(ivPreview)
        } else {
//            videoView.play(mediaRecorderHelper!!.videoFile!!.absolutePath)
            surfaceView.stopRecord()
        }
        resetView(true)
    }



    fun initListener() {

        tvBack.setOnClickListener {
            resetView(false)
//            TODO()d
        }
        tvCancel.setOnClickListener {
            finish()
        }
        tvOk.setOnClickListener {
            val result = surfaceView.mediaFile!!.absolutePath
            intent.putExtra(PickerSettings.RESULT, result)
            intent.putExtra(PickerSettings.IS_CAMERA, isCamera)
            setResult(RESULT_OK, intent)
            finish()
        }
        ivFlash.setOnClickListener {
            surfaceView.switchFlash(ivFlash)
        }
        ivSwitch.setOnClickListener {
            surfaceView.changeCameraFacing(ivFlash)
        }
    }


    private fun changeMediaType(isCamera: Boolean) {
        this.isCamera = isCamera
        btnRecord.isCamera = isCamera
        slideView.isRedLeft = isCamera
        surfaceView.isCamera = isCamera
        surfaceView.setFlashMode(ivFlash)
    }


    fun resetView(isFinish: Boolean) {
        tvCancel.visibility = if (isFinish) View.GONE else View.VISIBLE
        ivSwitch.visibility = if (isFinish) View.GONE else View.VISIBLE
        tvBack.visibility = if (!isFinish) View.GONE else View.VISIBLE
        tvOk.visibility = if (!isFinish) View.GONE else View.VISIBLE
        tvCamera.visibility = if (btnRecord.isCamera || (!btnRecord.isCamera && !isFinish)) View.VISIBLE else View.GONE
//        videoView.visibility = if (isFinish && !isCamera) View.VISIBLE else View.GONE
        surfaceView.visibility = if (isFinish) View.INVISIBLE else View.VISIBLE
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        btnRecord.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        btnRecord.destroy()
    }
}
