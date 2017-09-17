package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import com.example.apple.glidetest.media.SizeUtils
import com.example.apple.glidetest.media.SlideHolder
import com.example.apple.glidetest.media.VideoRecordBtn
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.orhanobut.logger.Logger
import com.txy.androidutils.ScreenUtils
import kotlinx.android.synthetic.main.activity_record_media.*
import kotlinx.android.synthetic.main.slide_view.*

class RecordMediaActivity : Activity(), VideoRecordBtn.OnRecordListener {

    var isCamera: Boolean = false
    private var slideHolder: SlideHolder? = null

    companion object {
        fun startForResult(context: Activity, isCamera: Boolean) {
            val intent = Intent(context, RecordMediaActivity::class.java)
            intent.putExtra(PickerSettings.IS_CAMERA, isCamera)
            context.startActivityForResult(intent, PickerSettings.RECORD_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View.inflate(this, R.layout.activity_record_media, null)
        setContentView(view)
        StatusBarUtil.setStatusBarColorBlack(this)
        window.setFormat(PixelFormat.TRANSLUCENT)
        btnRecord.setOnRecordListener(this)
        isCamera = intent.getBooleanExtra(PickerSettings.IS_CAMERA, true)
        slideHolder = SlideHolder(view)
        changeMediaType(isCamera)
        initListener()
        initSurface()
        resetView(false)
    }

    private fun initSurface() {
        surfaceView.ivPreview = ivPreview
        ivSwitch.visibility = if (surfaceView!!.camerasCount > 1) View.VISIBLE else View.GONE
        Logger.e(surfaceView.width.toString() + "--->" + surfaceView.height)
        val screenWidth = ScreenUtils.getScreenWidth(this)
        val size = SizeUtils(surfaceView.camera!!).getConsistentSize(this)
        val params = surfaceView.layoutParams
        params.height = screenWidth * size.height / size.width
        params.width = screenWidth
        surfaceView.layoutParams = params
        Logger.e(surfaceView.width.toString() + "--->" + surfaceView.height)
    }

    override fun onRecordStart(isCamera: Boolean) {
        if (!isCamera) {
            videoView.visibility = View.GONE
            ivPreview.visibility = View.GONE
//            surfaceView.visibility = View.VISIBLE
            surfaceView.startRecord()
        }
    }

    override fun onRecordFinish(isFail: Boolean) {
        if (isCamera) {
            surfaceView.takePicture(ivPreview)
        } else {
            surfaceView.stopRecord(isFail)
            videoView.media = surfaceView.media
            videoView.play(surfaceView!!.mediaFile!!.absolutePath,true)
        }
        resetView(true)
        slideHolder?.finish()
    }


    fun initListener() {
        tvBack.setOnClickListener {
            slideHolder?.switchStatus()
            resetView(false)
        }
        tvCancel.setOnClickListener {
            val mediaFile = surfaceView.mediaFile
            if (mediaFile != null && mediaFile.exists()) {
                mediaFile.delete()
            }
            finish()
        }
        tvOk.setOnClickListener {
            intent.putExtra(PickerSettings.RESULT, surfaceView.media!!)
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


     fun changeMediaType(isCamera: Boolean) {
        this.isCamera = isCamera
        btnRecord.isCamera = isCamera
        slideHolder!!.isRedLeft = isCamera
        surfaceView.isCamera = isCamera
        surfaceView.setFlashMode(ivFlash)
    }


    fun resetView(isFinish: Boolean) {
        tvCancel.visibility = if (isFinish) View.GONE else View.VISIBLE
        ivSwitch.visibility = if (isFinish) View.GONE else View.VISIBLE
        tvBack.visibility = if (!isFinish) View.GONE else View.VISIBLE
        tvOk.visibility = if (!isFinish) View.GONE else View.VISIBLE
        tvCamera.visibility = if (btnRecord.isCamera || (!btnRecord.isCamera && !isFinish)) View.VISIBLE else View.GONE
        videoView.visibility = if (isFinish && !isCamera) View.VISIBLE else View.GONE
//        surfaceView.visibility = if (isFinish) View.INVISIBLE else View.VISIBLE
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        btnRecord.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        btnRecord.destroy()
        videoView.destroy()
        surfaceView.destroy()
    }
}
