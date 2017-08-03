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
            context.startActivityForResult(intent, PickerSettings.COMMON_PICKER_REQUEST_CODE)
        }
    }

    private var allAdapter: CommonImageAdapter? = null
    private var selectedAdapter: ImageSelectedAdapter? = null
    private var imageSelector = SelectImageProvider.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        recyclerViewAll.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerViewAll.addItemDecoration(SpaceItemDecoration(dp2px(2.0f), HORIZONTAL_COUNT))
        recyclerViewSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tvCamera.setOnClickListener { launchCamera() }
        btnPickOk.setOnClickListener {
            intent.putStringArrayListExtra(PickerSettings.RESULT, SelectImageProvider.instance.selectedImgs)
            setResult(RESULT_OK, intent)
            finish()
        }
        ivLeft.setOnClickListener {
            startActivityForResult(Intent(this, FolderSelectActivity::class.java), PickerSettings.FOLDER_SELECT_CODE)
        }
        tvRight.setOnClickListener { finish() }
    }

    override fun initData() {
        val selectedFolder = FolderProvider.instance.selectedFolder
        allAdapter = CommonImageAdapter(this, selectedFolder!!.imgs)
        recyclerViewAll.adapter = allAdapter
        selectedAdapter = ImageSelectedAdapter(this, imageSelector.selectedImgs)
        recyclerViewSelected.adapter = selectedAdapter
        allAdapter!!.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int, isChecked: Boolean) {
                BigImageActivity.start(this@PickerActivity, position)
            }
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change && arg.isAdd) {
            recyclerViewSelected.scrollToPosition(imageSelector.size)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PickerSettings.FOLDER_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                val selectedFolder = FolderProvider.instance.selectedFolder
                tvCenter.text = selectedFolder!!.name
                allAdapter!!.refresh(selectedFolder.imgs)
            } else {
                finish()
            }
        } else if (requestCode == PickerSettings.BIG_REQUEST_CODE && resultCode == RESULT_OK) {
            allAdapter!!.refresh(imageSelector.selectedImgs)
            selectedAdapter!!.refresh(imageSelector.selectedImgs)
        }
    }
}
