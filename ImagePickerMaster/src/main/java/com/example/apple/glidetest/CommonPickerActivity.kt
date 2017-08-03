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
        fun start(context: Activity, maxSelect:Int){
            val intent = Intent(context, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT,maxSelect)
            context.startActivityForResult(intent,PickerSettings.COMMON_PICKER_REQUEST_CODE)
        }
    }
    private var adapter: CommonImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_picker)
        recyclerView.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerView.addItemDecoration(SpaceItemDecoration(dp2px(2.0f), HORIZONTAL_COUNT))
        btnPickOk.setOnClickListener {
            intent.putStringArrayListExtra(PickerSettings.RESULT, SelectImageProvider.instance.selectedImgs)
            setResult(RESULT_OK, intent)
            finish()
        }
        ivLeft.setOnClickListener {
            startActivityForResult(Intent(this, FolderSelectActivity::class.java), PickerSettings.FOLDER_SELECT_CODE)
        }
        tvRight.setOnClickListener {
            setResult(RESULT_CANCELED,intent)
            finish() }
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
            override fun onItemClick(position: Int, isChecked: Boolean) {
                BigImageActivity.start(this@CommonPickerActivity,position)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PickerSettings.FOLDER_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                val selectedFolder = FolderProvider.instance.selectedFolder
                tvCenter.text = selectedFolder!!.name
                adapter!!.refresh(selectedFolder.imgs)
            } else {
                finish()
            }
        }else if (requestCode == PickerSettings.BIG_REQUEST_CODE && resultCode == RESULT_OK){
            adapter!!.refresh(SelectImageProvider.instance.selectedImgs)
            update(null,null)
        }
    }
}
