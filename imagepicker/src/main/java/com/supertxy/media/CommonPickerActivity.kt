package com.supertxy.media

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.widget.Toast
import com.supertxy.media.adapter.CommonImageAdapter
import com.supertxy.media.bean.Media
import com.supertxy.media.listener.OnCameraClickListener
import com.supertxy.media.listener.OnItemClickListener
import com.supertxy.media.provider.FolderProvider
import com.supertxy.media.provider.SelectMediaProvider
import com.supertxy.media.utils.PickerSettings
import com.supertxy.media.utils.StatusBarUtil
import com.supertxy.media.view.GridItemDecoration
import com.orhanobut.logger.Logger
import com.supertxy.media.image.R
import com.txy.androidutils.TxyFileUtils
import com.txy.androidutils.TxyScreenUtils
import kotlinx.android.synthetic.main.activity_common_picker.*
import kotlinx.android.synthetic.main.title_bar.*
import java.util.*

class CommonPickerActivity : PickerBaseActivity() {

    companion object {
        fun startForResult(context: Activity, maxSelect: Int, initialSelect: ArrayList<Media>) {
            val intent = Intent(context, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT, initialSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }

        fun startForResult(fragment: Fragment, maxSelect: Int, initialSelect: ArrayList<Media>) {
            val intent = Intent(fragment.activity, CommonPickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT, initialSelect)
            fragment.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColorWhite(this)
        setContentView(R.layout.activity_common_picker)
        recyclerView.layoutManager = GridLayoutManager(this, HORIZONTAL_COUNT)
        recyclerView.addItemDecoration(GridItemDecoration.Builder(this)
                .size(TxyScreenUtils.dp2px(this, 5)).color(R.color.white)
                .margin(0, 0).isExistHead(false).build())
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
        btnPickOk.text = "完成 (" + selctedCount + "/" + intent.getIntExtra(PickerSettings.MAX_SELECT, 1) + ")"
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

    private fun launchCamera() {
        val FILE_PROVIDER = getString(R.string.provider_name)
        permissionUtils!!.checkCameraPermission(Runnable {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                tmpFile = TxyFileUtils.createIMGFile(this@CommonPickerActivity)
                if (tmpFile!!.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER, tmpFile)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile))
                    }
                    startActivityForResult(intent, PickerSettings.SYSTEM_CAREMA_REQUEST_CODE)
                } else {
                    Toast.makeText(this, "图片错误！", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "无法启动相机！", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val selectedFolder = folderProvider!!.selectedFolder
        when (requestCode) {
            PickerSettings.SYSTEM_CAREMA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    if (tmpFile != null) {
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)))
                        val media = Media(System.currentTimeMillis().toString(), tmpFile!!.absolutePath,
                                tmpFile!!.length().toString(), Media.MediaType.IMG)
                        imageProvider!!.add(media)
                        val dir = tmpFile!!.parentFile.absolutePath
                        folderProvider!!.addNewMedia(media)
                        if (TextUtils.equals(selectedFolder!!.dir, dir) || selectedFolder.name.equals(folderProvider!!.folders.get(0).name)) {
                            adapter!!.refresh(selectedFolder.medias)
                        }
                    } else {
                        Logger.e("Activity重新创建，没保存tmpFile")
                    }
                } else {
                    if (tmpFile != null && tmpFile!!.exists()) {
                        if (tmpFile!!.delete()) {
                            tmpFile = null
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (tmpFile != null)
            outState!!.putSerializable("tmpFile", tmpFile)
    }
}
