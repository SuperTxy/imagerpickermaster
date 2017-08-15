package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private val FILE_PROVIDER = "com.example.apple.glidetest.fileprovider"
    protected val HORIZONTAL_COUNT: Int = 4
    private var permissionUtils: PermissionUtils? = null
    protected var adapter: CommonImageAdapter? = null
    protected var view: View? = null
    protected var btnCenter: TextView? = null
    protected var btnLeft: ImageView? = null
    protected var initialSelect: ArrayList<String>? = null

    fun initView(savedInstanceState: Bundle?) {
        imageProvider = SelectImageProvider.instance
        folderProvider = FolderProvider.instance
        permissionUtils = PermissionUtils(this)
        imageProvider!!.addObserver(this)
        initialSelect = intent.getStringArrayListExtra(PickerSettings.INITIAL_SELECT)
        if (savedInstanceState == null) {
            imageProvider!!.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 1)
            imageProvider!!.setSelect(initialSelect)
            permissionUtils?.checkStoragePermission(Runnable {
                loadFolderAndImages()
            })
        } else {
            tmpFile = savedInstanceState.getSerializable("tmpFile") as File?
            initData()
        }
        btnLeft!!.setOnClickListener {
            startActivityForResult(Intent(this, FolderSelectActivity::class.java), PickerSettings.FOLDER_REQUEST_CODE)
        }
    }

    fun loadFolderAndImages() {
        Thread(Runnable {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
            val where = MediaStore.Images.Media.SIZE + " > " + 1000
            val cursor = contentResolver.query(contentUri, null, where, null, sortOrder)
            val allFolder = folderProvider!!.selectedFolder
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                if (path.contains(".gif")) continue
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
        permissionUtils?.checkCameraPermission(Runnable {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                tmpFile = FileUtils.createTmpFile(this)
                if (tmpFile!!.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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

            PickerSettings.FOLDER_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    btnCenter!!.text = selectedFolder!!.name
                    adapter!!.refresh(selectedFolder.imgs)
                } else finish()
            }

            PickerSettings.CAREMA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    if (tmpFile != null) {
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)))
                        val path = tmpFile!!.absolutePath
                        imageProvider!!.add(path)
                        val dir = tmpFile!!.parentFile.absolutePath
                        folderProvider!!.addCameraImage(path)
                        if (TextUtils.equals(selectedFolder!!.dir, dir) || selectedFolder.name.equals(folderProvider!!.folders.get(0).name))
                            adapter!!.refresh(selectedFolder.imgs)
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
        imageProvider = null
        folderProvider = null
    }

    abstract fun initData()
    abstract fun onPickerOk()
}