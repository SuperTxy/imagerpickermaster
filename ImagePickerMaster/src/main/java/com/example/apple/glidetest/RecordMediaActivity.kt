package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.media.MediaSurfaceView
import com.example.apple.glidetest.media.SizeUtils
import com.example.apple.glidetest.media.SlideHolder
import com.example.apple.glidetest.media.VideoRecordBtn
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.orhanobut.logger.Logger
import com.txy.androidutils.ScreenUtils
import kotlinx.android.synthetic.main.activity_record_media.*
import kotlinx.android.synthetic.main.slide_view.*
import java.io.File

class RecordMediaActivity : Activity(), VideoRecordBtn.OnRecordListener {

    var isCamera: Boolean = false
    private var slideHolder: SlideHolder? = null
    var media: Media? = null

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
        val screenWidth = ScreenUtils.getScreenWidth(this)
        val size = SizeUtils(surfaceView.camera!!).previewSize
        val params = surfaceView.layoutParams
        var rate = size!!.height.toFloat() / size.width.toFloat()
        if (rate < 1)
            rate = size.width.toFloat() / size.height.toFloat()
        params.height = (screenWidth * rate).toInt()
        params.width = screenWidth
        surfaceView.layoutParams = params
        Logger.e(params.height.toString() + "----->" + params.width.toString())
    }

    override fun onRecordStart(isCamera: Boolean) {
        surfaceView.visibility = View.VISIBLE
        videoView.visibility = View.GONE
        ivPreview.visibility = View.GONE
        Handler().postDelayed(Runnable {
            if (!isCamera)
                surfaceView.startRecord()
        }, 500)
    }

    override fun onRecordFinish(isFail: Boolean) {
        if (isCamera) {
            surfaceView.takePicture()
        } else {
            surfaceView.stopRecord(isFail)
        }
    }


    private val finishListener = object : MediaSurfaceView.OnMediaFinishListener {
        override fun afterStopRecord(mediaFile: File) {
            media = Media(null, mediaFile.absolutePath, null, Media.MediaType.VID)
            media!!.date = System.currentTimeMillis().toString()
            media!!.size = mediaFile.length().toString()
            resetView(true)
            videoView.play(mediaFile.absolutePath, true)
            videoView.media = media
            slideHolder?.finish()
        }

        override fun afterTakePicture(mediaFile: File) {
            media = Media(null, mediaFile.absolutePath, null, Media.MediaType.IMG)
            media!!.date = System.currentTimeMillis().toString()
            media!!.size = mediaFile.length().toString()
            ivPreview.setImageURI(Uri.fromFile(mediaFile))
            resetView(true)
            slideHolder?.finish()
        }
    }


    fun initListener() {
        surfaceView.setOnMediaFinishListener(finishListener)
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
            intent.putExtra(PickerSettings.RESULT, media!!)
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
        surfaceView.visibility = if (isFinish) View.GONE else View.VISIBLE
        ivPreview.visibility = if (isFinish && isCamera) View.VISIBLE else View.GONE
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
