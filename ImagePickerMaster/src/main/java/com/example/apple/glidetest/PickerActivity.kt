package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.adapter.ImageSelectedAdapter
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.utils.*
import com.example.apple.glidetest.view.GridItemDecoration
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class PickerActivity : PickerBaseActivity() {

    companion object {
        private val CLASSNAME: String = "className"
        fun startForResult(context: Activity, maxSelect: Int, initialSelect: ArrayList<String>,
                           isModified: Boolean, isAddImg: Boolean? = false) {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra("isModified", isModified)
            intent.putExtra("isAddImg", isAddImg)
            intent.putExtra(PickerSettings.INITIAL_SELECT, initialSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }

        fun start(context: Activity, maxSelect: Int, bundle: Bundle, className: String) {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.BUNDLE, bundle)
            intent.putExtra(CLASSNAME, className)
            context.startActivity(intent)
        }
    }

    private var selectedAdapter: ImageSelectedAdapter? = null
    private var imageSelector = SelectImageProvider.instance
    private var bundle: Bundle? = null
    private var className: String? = null
    private var isModified: Boolean? = null
    private var isAddImg: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColorWhite(this)
        setContentView(R.layout.activity_picker)
        isModified = intent.getBooleanExtra("isModified", false)
        isAddImg = intent.getBooleanExtra("isAddImg", false)
        if (adapter != null) {
            Logger.e(adapter.toString())
        }
        recyclerViewAll.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT) as RecyclerView.LayoutManager?
        recyclerViewAll.addItemDecoration(GridItemDecoration.Builder(this).size(dp2px(5.0f)).color(R.color.white)
                .margin(0, 0).isExistHead(false).build())
        recyclerViewSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSelected.isFocusable = false
        ivCamera.setOnClickListener {
            if (imageSelector.maxSelectToast(this, false))
            else launchCamera()
        }
        btnCenter = tvCenter
        btnLeft = tvLeft
        bundle = intent.getBundleExtra(PickerSettings.BUNDLE)
        className = intent.getStringExtra(CLASSNAME)
        initListener()
        initView(savedInstanceState)
        btnPickOk.text = if (imageSelector.size > 0) "完成" else "跳过"
        imageSelector.needSuffix = imageSelector.maxSelect == 6
    }

    private fun initListener() {
        tvRight.setOnClickListener {
            if (isAddImg == false && (isModified == true || imageSelector.size > 0)) {
                showAlertDialog(getString(R.string.confirm_to_exit), "退出", "取消", object : OnClickListener {
                    override fun onClick(v: View) {
                        finish()
                    }
                }, null)
            } else {
                finish()
                setResult(RESULT_CANCELED, intent)
            }
        }
        btnPickOk.setOnClickListener {
            onPickerOk()
        }
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
        selectedAdapter?.setOnUpdateMoveListener(object : ImageSelectedAdapter.OnUpdateMoveListener {
            override fun onUpdateMove() {
                var itemCount = selectedAdapter!!.itemCount
                if (itemCount > 0) recyclerViewSelected.scrollToPosition(itemCount - 1)
            }
        })
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change) {
            btnPickOk.text = if (imageSelector.size > 0) "完成" else "跳过"
        }
    }

    override fun onPickerOk() {
        if (initialSelect != null) {
            intent.putStringArrayListExtra(PickerSettings.RESULT, SelectImageProvider.instance.selectedImgs)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            val intent = Intent(this, Class.forName(className))
            bundle!!.putSerializable(PickerSettings.RESULT, SelectImageProvider.instance.selectedImgs)
            intent.putExtra(PickerSettings.BUNDLE, bundle)
            startActivity(intent)
            finish()
        }
    }
}
