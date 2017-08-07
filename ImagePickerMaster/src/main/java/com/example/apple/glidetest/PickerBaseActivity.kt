package com.example.apple.glidetest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.FileProvider
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
import java.io.File
import java.util.*

/**
 * Created by Apple on 17/7/31.
 */

abstract class PickerBaseActivity : Activity(), Observer {
    protected var imageProvider: SelectImageProvider = SelectImageProvider.instance
    private var tmpFile: File? = null
    private val FILE_PROVIDER = "com.example.apple.glidetest.fileprovider"
    protected val HORIZONTAL_COUNT: Int = 4
    private var permissionUtils: PermissionUtils? = null
    protected var adapter: CommonImageAdapter? = null
    protected var view: View? = null
    protected var btnCenter: TextView? = null
    protected var btnLeft: ImageView? = null
    protected var initialSelect: ArrayList<String>? = null

    fun initView() {
        permissionUtils = PermissionUtils(this)
        imageProvider.addObserver(this)
        initialSelect = intent.getStringArrayListExtra(PickerSettings.INITIAL_SELECT)
        imageProvider.setSelect(initialSelect)
        imageProvider.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 1)
        permissionUtils?.checkStoragePermission(Runnable { loadFolderAndImages() })
//        TODO("图片读取目录还有问题")
        btnLeft!!.setOnClickListener {
            startActivityForResult(Intent(this, FolderSelectActivity::class.java), PickerSettings.FOLDER_REQUEST_CODE)
        }
    }

    fun loadFolderAndImages() {
        Thread(Runnable {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(contentUri, null, null, null, null)
            val allFolder = FolderProvider.instance.selectedFolder
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                if (path.contains(".gif")) break
                if (allFolder?.firstImagePath == null) {
                    allFolder?.firstImagePath = path
                }
                allFolder?.addImage(path)
                val dir = File(path).parentFile.absolutePath
                if (!FolderProvider.instance.hasFolder(dir)) {
                    val name = dir.substring(dir.lastIndexOf('/') + 1)
                    FolderProvider.instance.addFolder(Folder(dir, name, path))
                }
                FolderProvider.instance.getFolderByDir(dir)?.addImage(path)
            }
            cursor.close()
            Handler(mainLooper).post { initData() }
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

        when (requestCode) {
            PickerSettings.BIG_REQUEST_CODE -> {
                adapter!!.notifyDataSetChanged()
                onBigResult()
            }

            PickerSettings.FOLDER_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val selectedFolder = FolderProvider.instance.selectedFolder
                    btnCenter!!.text = selectedFolder!!.name
                    adapter!!.refresh(selectedFolder.imgs)
                } else finish()
            }

            PickerSettings.CAREMA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    if (tmpFile != null) {
                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)))
                        adapter!!.insertImage(tmpFile!!.absolutePath)
                        imageProvider.add(tmpFile!!.absolutePath)
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

    abstract fun onBigResult()

    abstract fun initData()

}