package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.toastStrId
import kotlinx.android.synthetic.main.activity_big_image.*
import kotlinx.android.synthetic.main.title_bar.*
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
    private var selectedImages = ArrayList<String>()
    private var imageProvider = SelectImageProvider.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)
        val pos = intent.getIntExtra(POSITION, 0)
        viewPager.currentItem = pos
        viewPager.adapter = MyPagerAdapter()
        onPageSelected(pos)
        selectedImages.clear()
        selectedImages.addAll(imageProvider.selectedImgs)
        ivLeft.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        btnOK.setOnClickListener {
            imageProvider.selectedImgs = selectedImages
            setResult(RESULT_OK, intent)
            finish()
        }
        ivRight.setOnClickListener {
            btnOK.isEnabled = selectedImages.size > 0
            if (!ivRight.isSelected && selectedImages.size >= imageProvider.maxSelect) {
                toastStrId(R.string.big_max_toast)
            } else {
                val path = images.get(viewPager.currentItem)
                ivRight.isSelected = !ivRight.isSelected
                if (ivRight.isSelected) selectedImages.add(path)
                else selectedImages.remove(path)
            }
        }
    }

    inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = ImageView(this@BigImageActivity)
            Glide.with(this@BigImageActivity).load(File(images.get(position))).into(imageView)
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
        ivRight.isSelected = selectedImages.contains(images.get(position))
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}
