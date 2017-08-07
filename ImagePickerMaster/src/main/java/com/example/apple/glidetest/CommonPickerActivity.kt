package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.listener.OnCameraClickListener
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.dp2px
import com.example.apple.glidetest.view.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_common_picker.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class CommonPickerActivity : PickerBaseActivity() {

    companion object {
        fun start(context: Activity, maxSelect: Int, initialSelect: ArrayList<String>?) {
            val intent = Intent(context, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT,initialSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_picker)
        recyclerView.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerView.addItemDecoration(SpaceItemDecoration(dp2px(2.0f), HORIZONTAL_COUNT))
        btnPickOk.text = "确定 (0/" + intent.getIntExtra(PickerSettings.MAX_SELECT, 1) + ")"
        btnOk = btnPickOk
        btnCenter = tvCenter
        btnLeft = ivLeft
        tvRight.setOnClickListener {
            finish()
        }
        initView()
    }

    override fun update(o: Observable?, arg: Any?) {
        val provider = SelectImageProvider.instance
        btnPickOk.isEnabled = provider.size > 0
        btnPickOk.text = "确定 (" + provider.size + "/" + provider.maxSelect + ")"
    }

    override fun initData() {
        val selectedFolder = FolderProvider.instance.selectedFolder
        adapter = CommonImageAdapter(this, selectedFolder!!.imgs, true)
        recyclerView.adapter = adapter
        tvCenter.text = selectedFolder.name
        adapter!!.cameraClickListener = object : OnCameraClickListener {
            override fun onCameraClick() {
                launchCamera()
            }
        }
        adapter!!.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                BigImageActivity.start(this@CommonPickerActivity, position)
            }
        }
    }

    override fun onBigResult() {
        update(null, null)
    }
}
