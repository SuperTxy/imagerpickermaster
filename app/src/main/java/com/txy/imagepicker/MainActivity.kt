package com.txy.imagepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.apple.glidetest.PickerActivity
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : Activity() {

    private val imags = ArrayList<String>()
    private var adapter: MyGridAdapter? = null
    private val maxSelect = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMain.setOnClickListener {
            PickerActivity.startForResult(this,12,imags)
        }
        adapter = MyGridAdapter()
        gvMain.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PickerSettings.PICKER_REQUEST_CODE) {
            imags.addAll(data!!.getStringArrayListExtra(PickerSettings.RESULT))
            tvMain.isEnabled = imags.size != maxSelect
            adapter!!.notifyDataSetChanged()
        }
    }

    internal inner class MyGridAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return imags.size
        }

        override fun getItem(position: Int): String {
            return imags[position]
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
            loadImage(File(getItem(position)),holder.ivImage)
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
