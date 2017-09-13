package com.txy.imagepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.apple.glidetest.CommonPickerActivity
import com.example.apple.glidetest.PickerActivity
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : Activity() {

    private val medias = ArrayList<Media>()
    private var adapter: MyGridAdapter? = null
    private val maxSelect = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvPicker.setOnClickListener {
           PickerActivity.startForResult(this,6, medias)
        }
        tvCommonPicker.setOnClickListener {
            CommonPickerActivity.startForResult(this,12, medias)
        }
        adapter = MyGridAdapter()
        gvMain.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PickerSettings.PICKER_REQUEST_CODE) {
            medias.clear()
            medias.addAll(data!!.getSerializableExtra(PickerSettings.RESULT) as ArrayList<Media>)
            tvPicker.isEnabled = medias.size != maxSelect
            adapter!!.notifyDataSetChanged()
        }
    }

    internal inner class MyGridAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return medias.size
        }

        override fun getItem(position: Int): Media {
            return medias[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ViewHolder
            if (convertView == null) {
                convertView = View.inflate(this@MainActivity, R.layout.image_seleted_item, null)
                holder = ViewHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            loadImage(getItem(position),holder.ivImage)
            return convertView
        }

    }

    internal class ViewHolder(view: View) {
        var ivImage: ImageView

        init {
            ivImage = view.findViewById(R.id.ivImage) as ImageView
//            ivDel = view.findViewById(R.id.iv_del) as ImageView
        }
    }
}
