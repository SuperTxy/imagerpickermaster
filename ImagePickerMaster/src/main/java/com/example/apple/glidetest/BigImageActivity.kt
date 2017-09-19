package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.example.apple.glidetest.media.VideoView
import com.example.apple.glidetest.provider.FolderProvider
import com.example.apple.glidetest.provider.SelectMediaProvider
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.example.apple.glidetest.utils.loadImage
import com.txy.androidutils.ToastUtils
import kotlinx.android.synthetic.main.activity_big_image.*
import kotlinx.android.synthetic.main.title_bar.*
import kotlinx.android.synthetic.main.video_pager.view.*

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
    private var toastUtils: ToastUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColor(this, R.color.color1a1a1a)
        setContentView(R.layout.activity_big_image)
        toastUtils = ToastUtils(this)
        btnOK.isEnabled = imageProvider.selectedMedias.size > 0
        viewPager.addOnPageChangeListener(this)
        viewPager.adapter = MyPagerAdapter()
        val pos = intent.getIntExtra(POSITION, 0)
        viewPager.currentItem = pos
        initSurface()
        onPageSelected(pos)
        initListener()
    }

    private fun initSurface() {

//        val sp = PreferenceManager.getDefaultSharedPreferences(this)
//        val lp = videoView.surfaceView.layoutParams
//        var ratio: Float
//        if (sp.getInt("width", 0) != 0 && sp.getInt("height", 0) != 0) {
//            ratio = sp.getInt("height", 0) * 1.0f / sp.getInt("width", 0)
//        } else {
//            val size = SizeUtils(Camera.open()).getConsistentSize(this)
//            ratio = size.height * 1.0f / size.width
//            Logger.e("SizeUtils"+size.height+"-->"+size.width)
//        }
//        lp.width = ScreenUtils.getScreenWidth(this)
//        lp.height = (ScreenUtils.getScreenWidth(this) * ratio).toInt()
//        Logger.e(lp.width.toString() + "--->" + lp.height + "-->ratio-->" + ratio)
//        videoView.surfaceView.layoutParams = lp
    }

    private fun initListener() {
        videoView.setOnPlayListener(object : VideoView.OnPlayListener {
            override fun onPause() {
                videoView.visibility = View.GONE
            }

            override fun onFinish() {
                videoView.visibility = View.GONE
            }
        })
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
                toastUtils?.toast("此图片已被删除")
            else if (imageProvider.damageMedias.contains(media) && !ivRight.isSelected) {
                toastUtils?.toast("此图片文件已损坏！")
            } else if (media.isVideo && media.isDurationlarge12) {
                toastUtils?.toast("视频限定时长12秒！")
            } else if (media.isVideo && media.isSizeLarge3M) {
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

    inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = View.inflate(this@BigImageActivity, R.layout.video_pager, null)
            loadImage(medias.get(position), view.photoView)
            view.ivPlay.visibility = if (medias.get(position).isVideo) View.VISIBLE else View.GONE
            container.addView(view)
            view.ivPlay.setOnClickListener {
                videoView.visibility = View.VISIBLE
                videoView.play(medias.get(viewPager.currentItem).path)
            }
            return view
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

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        val media = medias.get(position)
        ivRight.isSelected = imageProvider.isMediaExist(media)
        ivRight.text = imageProvider.orderOfMedia(media)
        videoView.stop()
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        toastUtils?.destroy()
        videoView.destroy()
    }
}
