package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.example.apple.glidetest.utils.toastStr
import kotlinx.android.synthetic.main.activity_big_image.*
import kotlinx.android.synthetic.main.title_bar.*
import uk.co.senab.photoview.PhotoView
import java.io.File

class BigImageActivity : Activity(), ViewPager.OnPageChangeListener {

    companion object {
        val POSITION = "position"
        fun start(context: Activity, position: Int) {
            val intent = Intent(context, BigImageActivity::class.java)
            intent.putExtra(POSITION, position)
            context.startActivityForResult(intent, PickerSettings.BIG_REQUEST_CODE)
        }
    }

    private var images = FolderProvider.instance.selectedFolder!!.imgs
    private var imageProvider = SelectImageProvider.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColor(this, R.color.color1a1a1a)
        setContentView(R.layout.activity_big_image)
        btnOK.isEnabled = imageProvider.selectedImgs.size > 0
        viewPager.addOnPageChangeListener(this)
        viewPager.adapter = MyPagerAdapter()
        val pos = intent.getIntExtra(POSITION, 0)
        viewPager.currentItem = pos
        onPageSelected(pos)
        initListener()
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
            val path = images.get(viewPager.currentItem)
            if (imageProvider.maxSelectToast(this@BigImageActivity, ivRight.isSelected))
            else if (!File(path).exists())
                toastStr("此图片已被删除")
            else {
                ivRight.isSelected = !ivRight.isSelected
                if (ivRight.isSelected) {
                    imageProvider.add(path)
                    ivRight.text = (imageProvider.selectedImgs.indexOf(path) + 1).toString()
                } else {
                    imageProvider.remove(path)
                    ivRight.text = ""
                }
                btnOK.isEnabled = imageProvider.selectedImgs.size > 0
            }
        }
    }

    inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = PhotoView(this@BigImageActivity)
            val path = File(images.get(position))
            val request = Glide.with(this@BigImageActivity)
            if (path.endsWith(".gif"))
                request.load(path).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView)
            else request.load(path).into(imageView)
            container.addView(imageView)
            return imageView
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return images.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
            container.removeView(`object` as View)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        val selectedImgs = imageProvider.selectedImgs
        ivRight.isSelected = selectedImgs.contains(images.get(position))
        if (ivRight.isSelected) ivRight.text = (selectedImgs.indexOf(images.get(position)) + 1).toString()
        else ivRight.text = ""
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}
