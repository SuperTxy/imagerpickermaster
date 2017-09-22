package com.example.apple.glidetest.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.annotation.AttrRes
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.utils.mills2Duration
import com.orhanobut.logger.Logger
import com.txy.androidutils.TxyToastUtils
import kotlinx.android.synthetic.main.videoview.view.*

/**
 * Created by Apple on 17/9/12.
 */

class VideoView : FrameLayout, SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, SeekBar.OnSeekBarChangeListener {

    private var dataResource: String? = null
    private var player: MediaPlayer? = null
    private var view: View? = null
    private var position: Int = 0
    private var isPlaying = false
    private var toastUtils: TxyToastUtils? = null
    var media: Media? = null
    private var isRepeat: Boolean = false

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        view = LayoutInflater.from(context).inflate(R.layout.videoview, this)
        player = MediaPlayer()
        view!!.surfaceView.holder.addCallback(this)
        initPlayListener()
        toastUtils = TxyToastUtils(context)
    }

    private fun initPlayListener() {
        ivPlaySmall.setOnClickListener {
            if (!player!!.isPlaying)
                play(dataResource!!)
            else {
                ivPlaySmall.isSelected = false
                player!!.pause()
                isPlaying = false
                listener?.onPause()
            }
        }
    }

    fun play(dataResource: String, isRepeat: Boolean = false) {
        this.isRepeat = isRepeat
        if (!TextUtils.equals(dataResource, this.dataResource)) {
            this.dataResource = dataResource
            player!!.reset()
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                player!!.setDataSource(dataResource)
                player!!.setScreenOnWhilePlaying(true)
                player!!.setOnCompletionListener(this)
                player!!.setOnPreparedListener(this)
                player!!.setOnErrorListener(this)
                view!!.seekBar.setOnSeekBarChangeListener(this)
                player!!.prepare()
            } catch(e: Exception) {
                toastUtils?.toast("无法播放此视频文件！")
                return
            }
        }
        ivPlaySmall.isSelected = true
        player!!.start()
        if (!isRepeat) {
            Thread {
                isPlaying = true
                while (isPlaying) {
                    if (player != null && player!!.isPlaying) {
//                        java.lang.IllegalStateException
//                        at android.media.MediaPlayer.getCurrentPosition(Native Method)
                        seekBar.progress = player!!.currentPosition
                        Thread.sleep(500)
                    }
                }
            }.start()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Logger.d("mediaplayer   onError------------")
        play(dataResource!!)
        isPlaying = false
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Logger.d("mediaplayer   onPrepared------------" + player!!.duration)
        resetView()
        view!!.seekBar.max = player!!.duration
        view!!.tvTotal.text = mills2Duration(player!!.duration.toLong())
        media?.duration = player!!.duration.toLong()
        media?.width = player!!.videoWidth.toString()
        media?.height = player!!.videoHeight.toString()
        Logger.e(media?.toString())
    }

    private fun resetView() {
        view!!.seekBar.progress = 0
        view!!.tvCurrent.text = "00:00"
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Logger.d("mediaplayer   onCompletion------------")
        if (!isRepeat) {
            ivPlaySmall.isSelected = false
            resetView()
            listener?.onFinish()
            isPlaying = false
        } else {
            player?.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Logger.d("VideoView surfaceChanged------------")

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Logger.d("VideoView surfaceDestroyed------------")
        player?.release()
        player = null
        resetView()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Logger.d("VideoView surfaceCreated------------")
        player!!.setDisplay(view!!.surfaceView.holder)
        if (position > 0) {
            play(dataResource!!)
            player!!.seekTo(position)
            view?.tvCurrent?.text = mills2Duration(position.toLong())
            position = 0
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (progress >= 0) {
            if (fromUser)
                player!!.seekTo(progress)
            view?.tvCurrent?.text = mills2Duration(progress.toLong())
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    // 当其他Activity被打开，暂停播放
    fun pause() {
        if (player!!.isPlaying) {
            position = player!!.currentPosition
            player!!.stop()
        }
    }

    fun stop() {
        if (player != null && dataResource != null) {
            dataResource = null
            resetView()
            Logger.d("videoview stop------------")
        }
    }

    fun destroy() {
        if (player != null) {
            if (player!!.isPlaying)
                player!!.stop()
            player!!.release()
            Logger.d("videoview destroy------------")
        }
        toastUtils?.destroy()
    }

    private var listener: OnPlayListener? = null

    fun setOnPlayListener(listener: OnPlayListener) {
        this.listener = listener
    }

    interface OnPlayListener {
        fun onFinish()
        fun onPause()
    }
}
