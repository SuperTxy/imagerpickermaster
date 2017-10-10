package com.example.apple.glidetest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.example.apple.glidetest.media.MediaSurfaceView
import com.example.apple.glidetest.media.SlideHolder
import com.example.apple.glidetest.media.VideoRecordBtn
import com.example.apple.glidetest.media.deleteMediaFile
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.orhanobut.logger.Logger
import com.txy.androidutils.TxyScreenUtils
import com.txy.androidutils.dialog.TxyDialogUtils
import kotlinx.android.synthetic.main.activity_record_media.*
import java.io.File

class RecordMediaActivity : Activity(), VideoRecordBtn.OnRecordListener {

    var isCamera: Boolean = false
    private var slideHolder: SlideHolder? = null
    private var dialogUtisl: TxyDialogUtils? = null

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
        dialogUtisl = TxyDialogUtils(this)
        isCamera = intent.getBooleanExtra(PickerSettings.IS_CAMERA, true)
        slideHolder = SlideHolder(view)
        ivSwitch.visibility = if (surfaceView!!.camerasCount > 1) View.VISIBLE else View.GONE
        changeMediaType(isCamera)
        initListener()
        resetView(false)
    }

    override fun onRecordStart() {
        tvVideoHint.visibility = View.INVISIBLE
        surfaceView.visibility = View.VISIBLE
        if (!isCamera) {
            surfaceView.startRecord()
        }
    }

    override fun onRecordFinish() {
        tvVideoHint.visibility = if (!isCamera) View.VISIBLE else View.INVISIBLE
        if (isCamera) {
            surfaceView.takePicture()
        } else {
            surfaceView.stopRecord()
        }
    }

    private val mediaListener = object : MediaSurfaceView.OnMediaListener {
        override fun switchToCamera() {
            slideHolder!!.switchToCamera()
        }

        override fun switchToVideo() {
            slideHolder!!.switchToVideo()
        }

        override fun touchFocus(event: MotionEvent) {
            handleFoucs(event)
        }

        override fun onFocusSuccess() {
            focusView.visibility = View.INVISIBLE
        }

        override fun afterStopRecord() {
            resetView(true)
            slideHolder?.finish()
        }

        override fun afterTakePicture(mediaFile: File) {
            resetView(true)
            slideHolder?.finish()
        }
    }


    fun initListener() {
        surfaceView.setOnMediaFinishListener(mediaListener)
        tvBack.setOnClickListener {
            surfaceView.resetState()
            slideHolder?.switchStatus()
            slideHolder?.isFinish = false
            resetView(false)
        }
        tvCancel.setOnClickListener {
            surfaceView.mediaFile = deleteMediaFile(surfaceView.mediaFile)
            finish()
        }
        tvOk.setOnClickListener {
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(surfaceView.mediaFile)))
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
        tvBack.text = if (isFinish && isCamera) "重拍" else "返回"
        btnRecord.visibility = if (isFinish && isCamera) View.GONE else View.VISIBLE
        tvOk.visibility = if (!isFinish) View.INVISIBLE else View.VISIBLE
        tvCamera.visibility = if (btnRecord.isCamera || (!btnRecord.isCamera && !isFinish)) View.VISIBLE else View.GONE
    }

    fun handleFoucs(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y
        val screenWidth = TxyScreenUtils.getScreenWidth(this)
        val top = llBottom.getTop()
        Logger.e(llBottom.getTop().toString())
        if (y > llBottom.getTop()) {
            return false
        }
        focusView.setVisibility(View.VISIBLE)
        if (x < focusView.getWidth() / 2) {
            x = (focusView.getWidth() / 2).toFloat()
        }
        if (x > screenWidth - focusView.getWidth() / 2) {
            x = (screenWidth - focusView.getWidth() / 2).toFloat()
        }
        if (y < focusView.getWidth() / 2) {
            y = (focusView.getWidth() / 2).toFloat()
        }
        if (y > llBottom.getTop() - focusView.getWidth() / 2) {
            y = (llBottom.getTop() - focusView.getWidth() / 2).toFloat()
        }
        focusView.setX(x - focusView.getWidth() / 2)
        focusView.setY(y - focusView.getHeight() / 2)
        val scaleX = ObjectAnimator.ofFloat(focusView, "scaleX", 1f, 0.7f)
        val scaleY = ObjectAnimator.ofFloat(focusView, "scaleY", 1f, 0.7f)
        val alpha = ObjectAnimator.ofFloat(focusView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f)
        val animSet = AnimatorSet()
        animSet.play(scaleX).with(scaleY).before(alpha)
        animSet.duration = 400
        animSet.start()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        btnRecord.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        surfaceView.registerSensorManager(this)
    }

    override fun onPause() {
        super.onPause()
        surfaceView.unregisterSensorManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        btnRecord.destroy()
        surfaceView.destroy()
        dialogUtisl?.destroy()
    }
}
