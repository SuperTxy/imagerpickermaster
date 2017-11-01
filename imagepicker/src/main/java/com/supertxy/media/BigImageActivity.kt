package com.supertxy.media

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.orhanobut.logger.Logger
import com.supertxy.media.provider.FolderProvider
import com.supertxy.media.provider.SelectMediaProvider
import com.supertxy.media.utils.PickerSettings
import com.supertxy.media.utils.StatusBarUtil
import com.supertxy.media.utils.loadImage
import com.txy.androidutils.TxyScreenUtils
import com.txy.androidutils.TxyToastUtils
import kotlinx.android.synthetic.main.activity_big_image.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.video_pager.view.*
import uk.co.senab.photoview.PhotoView

class BigImageActivity : Activity(), ViewPager.OnPageChangeListener {

    companion object {
        val POSITION = "position"
        fun start(context: Activity, position: Int) {
            val intent = Intent(context, BigImageActivity::class.java)
            intent.putExtra(POSITION, position)
            context.startActivityForResult(intent, PickerSettings.BIG_REQUEST_CODE)
        }
    }

    private var medias = FolderProvider.instance.selectedFolder!!.medias
    private var imageProvider = SelectMediaProvider.instance
    private var toastUtils: TxyToastUtils? = null
    private var player: MediaPlayer? = null
    private var currentPosition: Int = 0
    private var isFull: Boolean = false
    private var lastItemPosition: Int = 0
    private var flItems: ArrayList<FrameLayout> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)
        StatusBarUtil.setStatusBarColor(this, R.color.color1a1a1a)
        toastUtils = TxyToastUtils(this)
        btnOK.isEnabled = imageProvider.selectedMedias.size > 0
        viewPager.addOnPageChangeListener(this)
        viewPager.adapter = MyPagerAdapter()
        initSurfaces()
        handlePos()
        initListener()
    }

    private fun handlePos() {
        val pos = intent.getIntExtra(POSITION, 0)
        viewPager.currentItem = pos
        lastItemPosition = pos
        onPageSelected(pos)
    }

    private fun initListener() {
        ivLeft.setOnClickListener {
            finish()
        }
        btnOK.setOnClickListener {
            setResult(RESULT_OK, intent)
            finish()
        }

        ivRight.setOnClickListener {
            val media = medias.get(viewPager.currentItem)
            if (imageProvider.maxSelectToast(this@BigImageActivity, ivRight.isSelected))
            else if (media.dir.isNullOrEmpty() && !ivRight.isSelected)
                toastUtils?.toast(R.string.delete_image)
            else if (imageProvider.damageMedias.contains(media) && !ivRight.isSelected) {
                toastUtils?.toast(R.string.damage_image)
            } else if (media.isVideo && media.isDurationlarge12) {
                toastUtils?.toast("视频限定时长12秒！")
            } else if (media.isVideo && media.isSizeLarge10M) {
                toastUtils?.toast("视频大小超过限制！")
            } else {
                ivRight.isSelected = !ivRight.isSelected
                if (ivRight.isSelected) {
                    imageProvider.add(media)
                    ivRight.text = (imageProvider.selectedMedias.indexOf(media) + 1).toString()
                } else {
                    imageProvider.remove(media)
                    ivRight.text = ""
                }
                btnOK.isEnabled = imageProvider.selectedMedias.size > 0
            }
        }
    }

    private fun initSurfaces() {
        for (i in 0..4) {
            val contentView = View.inflate(this@BigImageActivity, R.layout.video_pager, null) as FrameLayout
            contentView.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                    Logger.d("-----------surfaceDestroyed--------------")
                    if (player != null && player!!.isPlaying)
                        currentPosition = player!!.currentPosition
                    stopPlay()
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    Logger.d("-----------surfaceCreated--------------")
                    val media = medias[viewPager.currentItem]
                    if (imageProvider.damageMedias.contains(media)) {
                        toastUtils?.toast(R.string.damage_image)
                    } else if (media.dir.isNullOrEmpty()) {
                        toastUtils?.toast(R.string.delete_image)
                    } else {
                        playVideo(media.path, contentView.surfaceView)
                        if (currentPosition > 0) {
                            player!!.seekTo(currentPosition)
                            currentPosition = 0
                        }
                    }
                }

            })
            contentView.ivPlay.setOnClickListener {
                contentView.ivPlay.visibility = View.GONE
                if (!isFull) handleFull()
                if (player != null) player!!.start()
                else {
                    if (contentView.getChildAt(1) is PhotoView)
                        contentView.getChildAt(1).visibility = View.GONE
                    contentView.surfaceView.visibility = View.VISIBLE
                }
            }
            contentView.surfaceView.setOnClickListener {
                if (player != null && player!!.isPlaying) {
                    player?.pause()
                    if (isFull) handleFull()
                    contentView.ivPlay.visibility = View.VISIBLE
                } else handleFull()
            }
            flItems.add(contentView)
        }
    }

    inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val media = medias.get(position)
            val contentView = flItems.get(position % 5)
            if (contentView.parent != null) {
                (contentView.parent as ViewGroup).removeView(contentView)
            }
            if (contentView.getChildAt(1) is PhotoView) {
                Logger.e("is PhotoView")
                contentView.removeView(contentView.getChildAt(1))
            }
            val photoView = PhotoView(this@BigImageActivity)
            contentView.addView(photoView, 1)
            contentView.surfaceView.visibility = View.GONE
            contentView.ivPlay.visibility = if (media.isVideo) View.VISIBLE else View.INVISIBLE
            loadImage(media, photoView)
            photoView.setOnPhotoTapListener { view, x, y ->
                handleFull()
            }
            container.addView(contentView)
            return contentView
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return medias.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
            container.removeView(`object` as View)
        }
    }

    private fun handleFull() {
        if (isFull) {
            ObjectAnimator.ofFloat(flBottom, "translationY", flBottom.translationY, 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(titleBar, "translationY", titleBar.translationY, 0f).setDuration(300).start()
        } else {
            ObjectAnimator.ofFloat(flBottom, "translationY", 0f, flBottom.height.toFloat()).setDuration(300).start()
            ObjectAnimator.ofFloat(titleBar, "translationY", 0f, -titleBar.height.toFloat()).setDuration(300).start()
        }
        isFull = !isFull
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        val media = medias.get(position)
        ivRight.isSelected = imageProvider.isMediaExist(media)
        ivRight.text = imageProvider.orderOfMedia(media)
        if (isFull) handleFull()
        if (medias[lastItemPosition].isVideo) {
            val fl = flItems[lastItemPosition % 5]
            if (player != null) {
                fl.ivPlay.visibility = View.VISIBLE
                if (fl.getChildAt(1) is PhotoView)
                    fl.getChildAt(1).visibility = View.VISIBLE
                fl.surfaceView.visibility = View.GONE
            }
        }
        lastItemPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun playVideo(dataSource: String, surfaceView: SurfaceView) {
        if (player == null)
            player = MediaPlayer()
        player!!.reset()
        player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player!!.setDataSource(dataSource)
        player!!.setDisplay(surfaceView.holder)
        player!!.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
        player!!.setOnVideoSizeChangedListener { mp, width, height ->
            updateVideoViewSize(player!!.getVideoHeight().toFloat() / player!!.getVideoWidth(), surfaceView)
        }
        player!!.setScreenOnWhilePlaying(true)
        player!!.setOnErrorListener { mp, what, extra ->
            Logger.e("mediaplayer   onError------------" + what + "extra-->" + extra)
            false
        }
        player!!.setOnCompletionListener {
            flItems[viewPager.currentItem % 5].ivPlay.visibility = View.VISIBLE
            if (isFull) handleFull()
        }
        player!!.prepare()
        player!!.start()
    }

    private fun updateVideoViewSize(rate: Float, surfaceView: SurfaceView) {
        val screenWidth = TxyScreenUtils.getScreenWidth(this)
        val lp = surfaceView.layoutParams
        lp.width = screenWidth
        lp.height = (screenWidth * rate).toInt()
        surfaceView.layoutParams = lp
    }

    fun stopPlay() {
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toastUtils?.destroy()
    }
}
