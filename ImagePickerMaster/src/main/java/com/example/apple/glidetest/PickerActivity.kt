package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.adapter.ImageSelectedAdapter
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.dp2px
import com.example.apple.glidetest.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class PickerActivity : PickerBaseActivity() {
    companion object {
        fun start(context: Activity, maxSelect: Int) {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }
    }

    private var selectedAdapter: ImageSelectedAdapter? = null
    private var imageSelector = SelectImageProvider.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        recyclerViewAll.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerViewAll.addItemDecoration(SpaceItemDecoration(dp2px(2.0f), HORIZONTAL_COUNT))
        recyclerViewSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tvCamera.setOnClickListener { launchCamera() }
        btnPickOk.text = if (imageSelector.size >0 ) "完成" else "跳过"
        btnOk = btnPickOk
        btnCenter = tvCenter
        btnLeft = ivLeft
        btnRight = tvRight
        initView()
    }

    override fun initData() {
        val selectedFolder = FolderProvider.instance.selectedFolder
        adapter = CommonImageAdapter(this, selectedFolder!!.imgs)
        recyclerViewAll.adapter = adapter
        selectedAdapter = ImageSelectedAdapter(this, imageSelector.selectedImgs)
        recyclerViewSelected.adapter = selectedAdapter
        adapter!!.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                BigImageActivity.start(this@PickerActivity, position)
            }
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change ) {
            recyclerViewSelected.scrollToPosition(selectedAdapter!!.itemCount)
            btnPickOk.text = if (imageSelector.size >0 ) "完成" else "跳过"
        }
    }
    override fun onBigResult() {
        selectedAdapter!!.refresh(imageSelector.selectedImgs)
        recyclerViewSelected.scrollToPosition(selectedAdapter!!.itemCount)
    }
}
