package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.listener.OnCameraClickListener
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.provider.FolderProvider
import com.example.apple.glidetest.provider.SelectMediaProvider
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.StatusBarUtil
import com.example.apple.glidetest.view.GridItemDecoration
import com.txy.androidutils.ScreenUtils
import kotlinx.android.synthetic.main.activity_common_picker.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class CommonPickerActivity : PickerBaseActivity() {

    companion object {
        fun startForResult(context: Activity, maxSelect: Int, initialSelect: ArrayList<Media>) {
            val intent = Intent(context, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT,initialSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }

        fun startForResult(fragment: Fragment, maxSelect: Int, initialSelect: ArrayList<Media>){
            val intent = Intent(fragment.activity, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT,initialSelect)
            fragment.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColorWhite(this)
        setContentView(R.layout.activity_common_picker)
        recyclerView.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerView.addItemDecoration(GridItemDecoration.Builder(this)
                .size(ScreenUtils.dp2px(this,5)).color(R.color.white)
                .margin(0,0).isExistHead(false).build())
        baseView()
        tvLeft.setOnClickListener {
            finish()
        }
        btnPickOk.setOnClickListener {
         onPickerOk()
        }
        initView(savedInstanceState)
        var selctedCount = imageProvider!!.selectedMedias.size
        btnPickOk.isEnabled = selctedCount > 0
        btnPickOk.text = "完成 ("+selctedCount+"/" + intent.getIntExtra(PickerSettings.MAX_SELECT, 1) + ")"
    }

    private fun baseView() {
        btnCenter = tvCenter
        llEmptyView = emptyView
        btnReload = tvReload
        tvText = tvHint
    }

    override fun update(o: Observable?, arg: Any?) {
        val provider = SelectMediaProvider.instance
        btnPickOk.isEnabled = provider.size > 0
        btnPickOk.text = "完成 (" + provider.size + "/" + provider.maxSelect + ")"
    }

    override fun initData() {
        val selectedFolder = FolderProvider.instance.selectedFolder
        adapter = CommonImageAdapter(this, selectedFolder!!.medias, true)
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

    override fun onPickerOk() {
        intent.putExtra(PickerSettings.RESULT, SelectMediaProvider.instance.selectedMedias)
        setResult(RESULT_OK, intent)
        finish()
    }
}
