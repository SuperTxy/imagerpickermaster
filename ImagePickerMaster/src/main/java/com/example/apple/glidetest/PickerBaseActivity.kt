package com.example.apple.glidetest

import android.Manifest
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.bean.Folder
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.utils.FileUtils
import com.example.apple.glidetest.utils.PermissionUtils
import com.example.apple.glidetest.utils.PickerSettings
import com.orhanobut.logger.Logger
import java.io.File
import java.util.*

/**
 * Created by Apple on 17/7/31.
 */
abstract class PickerBaseActivity : Activity(), Observer {
    protected var imageProvider: SelectImageProvider? = null
    protected var folderProvider: FolderProvider? = null
    private var tmpFile: File? = null
    private var FILE_PROVIDER:String ?=null
    protected val HORIZONTAL_COUNT: Int = 4
    private var permissionUtils: PermissionUtils? = null
    protected var adapter: CommonImageAdapter? = null
    protected var view: View? = null
    protected var btnCenter: TextView? = null
    protected var initialSelect: ArrayList<String>? = null
    protected var llEmptyView: View? = null
    protected var btnReload: TextView? = null
    protected var tvText: TextView? = null
    protected var folderPopup: FolderPopup? = null

    override fun onResume() {
        super.onResume()
        if (llEmptyView!!.visibility == View.VISIBLE && isReadPermissionGranted()) {
            loadFolderAndImages()
        }
        llEmptyView!!.visibility = if (isReadPermissionGranted()) View.GONE else View.VISIBLE
    }

    fun initView(savedInstanceState: Bundle?) {
        FILE_PROVIDER = getString(R.string.provider_name)
        imageProvider = SelectImageProvider.instance
        folderProvider = FolderProvider.instance
        permissionUtils = PermissionUtils(this)
        imageProvider!!.addObserver(this)
        folderProvider!!.clear()
        initialSelect = intent.getStringArrayListExtra(PickerSettings.INITIAL_SELECT)
        if (savedInstanceState == null) {
            imageProvider!!.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 0)
            imageProvider!!.setSelect(initialSelect)
            permissionUtils?.checkStoragePermission(Runnable {
                loadFolderAndImages()
            })
        } else {
            if (imageProvider!!.maxSelect == 0) {
                imageProvider!!.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 0)
                imageProvider!!.setSelect(initialSelect)
            }
            tmpFile = savedInstanceState.getSerializable("tmpFile") as File?
            initData()
        }
        folderPopup = FolderPopup(this)

        btnReload!!.setOnClickListener {
            if (isReadPermissionGranted()) {
                loadFolderAndImages()
            } else {
                permissionUtils?.checkStoragePermission(Runnable {
                    loadFolderAndImages()
                })
            }
        }
        btnCenter!!.setOnClickListener {
            if (!folderPopup!!.isShowing()) {
                folderPopup!!.show(btnCenter!!, object : FolderPopup.OnFolderSelectedListener {
                    override fun OnFolderSelected() {
                        btnCenter!!.text = folderProvider!!.selectedFolder!!.name
                        adapter!!.refresh(folderProvider!!.selectedFolder!!.imgs)
                    }
                })
            }
        }
    }

    fun isReadPermissionGranted(): Boolean {
        return permissionUtils!!.isPermissionsGranted(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    fun loadFolderAndImages() {
        Thread(Runnable {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
            val where = MediaStore.Images.Media.SIZE + " > " + 2000
            val cursor = contentResolver.query(contentUri, null, where, null, sortOrder)
            val allFolder = folderProvider!!.selectedFolder
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
//                if (path.contains(".gif"))
//                    Logger.e(path)
                if(!File(path).exists()) continue
                if (allFolder?.firstImagePath == null) {
                    allFolder?.firstImagePath = path
                }
                allFolder?.addImage(path)
                val dir = File(path).parentFile.absolutePath
                if (!folderProvider!!.hasFolder(dir)) {
                    val name = dir.substring(dir.lastIndexOf('/') + 1)
                    folderProvider!!.addFolder(Folder(dir, name, path))
                }
                folderProvider!!.getFolderByDir(dir)?.addImage(path)
            }
            cursor.close()
            Handler(mainLooper).post {
                initData()
            }
        }).start()
    }

    fun launchCamera() {
        if (adapter == null) return
        permissionUtils?.checkCameraPermission(Runnable {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                tmpFile = FileUtils.createTmpFile(this)
                if (tmpFile!!.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Logger.e(FILE_PROVIDER)
                        val photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER, tmpFile)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile))
                    }
                    startActivityForResult(intent, PickerSettings.CAREMA_REQUEST_CODE)
                } else {
                    Toast.makeText(this, "图片错误！", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "无法启动相机！", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val selectedFolder = folderProvider!!.selectedFolder
        when (requestCode) {
            PickerSettings.BIG_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) onPickerOk()
            }
            PickerSettings.CAREMA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    if (tmpFile != null) {
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)))
                        val path = tmpFile!!.absolutePath
                        imageProvider!!.add(path)
                        val dir = tmpFile!!.parentFile.absolutePath
                        folderProvider!!.addCameraImage(path)
                        if (TextUtils.equals(selectedFolder!!.dir, dir) || selectedFolder.name.equals(folderProvider!!.folders.get(0).name)) {
                            adapter!!.refresh(selectedFolder.imgs)
                        }
                    } else {
                        Logger.e("Activity重新创建，没保存tmpFile")
                    }
                } else {
//               user click cancel
                    if (tmpFile != null && tmpFile!!.exists()) {
                        if (tmpFile!!.delete()) {
                            tmpFile = null
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (tmpFile != null)
            outState!!.putSerializable("tmpFile", tmpFile)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageProvider!!.deleteObservers()
    }

    abstract fun initData()
    abstract fun onPickerOk()

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN){
            Glide.with(this).onTrimMemory(level)
        }
    }
}