package com.supertxy.media.media

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
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.supertxy.media.R
import com.supertxy.media.bean.Media
import com.txy.androidutils.TxyScreenUtils
import com.txy.androidutils.TxyToastUtils
import kotlinx.android.synthetic.main.videoview.view.*
import java.io.File

/**
 * Created by Apple on 17/9/12.
 */

class VideoView : FrameLayout, SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private var dataResource: String? = null
    private var player: MediaPlayer? = null
    private var position: Int = 0
    var media: Media? = null
    private var toastUtils: TxyToastUtils? = null
    private var view: View? = null

    constructor(context: Context) : this(context, null) {

    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        view = LayoutInflater.from(context).inflate(R.layout.videoview, this)
        view!!.surfaceView.holder.addCallback(this)
        toastUtils = TxyToastUtils(context)
        player = MediaPlayer()
        setOnClickListener {
            this.visibility = View.GONE
            pause()
        }
    }

    fun play(dataResource: String, isRepeat: Boolean = false) {
        if (!TextUtils.equals(dataResource, this.dataResource)) {
            stop()
            this.dataResource = dataResource
            position = 0
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                if (!File(dataResource).exists())
                    toastUtils?.toastCenterStr("文件不存在")
                player!!.setDataSource(dataResource)
                player!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                player!!.setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
                    updateVideoViewSize(player!!.getVideoWidth().toFloat(), player!!
                            .getVideoHeight().toFloat())
                })
                player!!.isLooping = isRepeat
                player!!.setScreenOnWhilePlaying(true)
                player!!.setOnCompletionListener(this)
                player!!.setOnPreparedListener(this)
                player!!.setOnErrorListener(this)
                player!!.prepare()
            } catch(e: Exception) {
                Logger.e(e.message)
                Toast.makeText(context, "无法播放此视频文件！", Toast.LENGTH_SHORT).show()
                return
            }
        }
        player!!.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Logger.d("mediaplayer   onError------------" + what + "extra-->" + extra)
//        toastUtils?.toastCenterStr("播放出错了！")
        this.visibility = View.GONE
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        media?.duration = player!!.duration.toLong()
        media?.width = player!!.videoWidth.toString()
        media?.height = player!!.videoHeight.toString()
        Logger.e(media?.toString())
    }

    private fun updateVideoViewSize(videoWidth: Float, videoHeight: Float) {
        val screenWidth = TxyScreenUtils.getScreenWidth(context)
        val lp = view!!.surfaceView.layoutParams
        lp.width = screenWidth
        lp.height = (screenWidth *videoHeight / videoWidth).toInt()
        view!!.surfaceView.layoutParams = lp
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp!= null && !mp.isLooping) this.visibility = View.GONE
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
//        stop()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        player!!.setDisplay(holder)
        if (position > 0 && dataResource != null) {
            play(dataResource!!)
            player!!.seekTo(position)
            position = 0
        }
    }

    // 当其他Activity被打开，暂停播放
    fun pause() {
        if (player != null && player!!.isPlaying) {
            position = player!!.currentPosition
            player!!.pause()
        }
    }

    fun stop() {
        if (player != null && dataResource != null) {
            player!!.stop()
            player!!.reset()
            dataResource = null
        }
    }

    fun destroy() {
        stop()
        player?.release()
        player = null
        toastUtils?.destroy()
    }
}
