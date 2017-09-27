package com.example.apple.glidetest

import android.Manifest
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.apple.glidetest.adapter.CommonImageAdapter
import com.example.apple.glidetest.bean.Folder
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.provider.FolderProvider
import com.example.apple.glidetest.provider.SelectMediaProvider
import com.example.apple.glidetest.utils.PickerSettings
import com.example.apple.glidetest.utils.isGif
import com.orhanobut.logger.Logger
import com.txy.androidutils.TxyPermissionUtils
import java.io.File
import java.util.*

/**
 * Created by Apple on 17/7/31.
 */
abstract class PickerBaseActivity : Activity(), Observer {

    protected var tmpFile: File? = null
    protected var imageProvider: SelectMediaProvider? = null
    protected var folderProvider: FolderProvider? = null
    protected val HORIZONTAL_COUNT: Int = 4
    protected var permissionUtils: TxyPermissionUtils? = null
    protected var adapter: CommonImageAdapter? = null
    protected var view: View? = null
    protected var btnCenter: TextView? = null
    protected var initialSelect: ArrayList<Media>? = null
    protected var llEmptyView: View? = null
    protected var btnReload: TextView? = null
    protected var tvText: TextView? = null
    protected var folderPopup: FolderPopup? = null
    private var READ_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onResume() {
        super.onResume()
        if (llEmptyView!!.visibility == View.VISIBLE && permissionUtils!!.isPermissionGranted(READ_PERMISSION)) {
            loadMedias()
        }
        llEmptyView!!.visibility = if (permissionUtils!!.isPermissionGranted(READ_PERMISSION)) View.GONE else View.VISIBLE
    }

    fun initView(savedInstanceState: Bundle?) {
        imageProvider = SelectMediaProvider.instance
        folderProvider = FolderProvider.instance
        permissionUtils = TxyPermissionUtils(this)
        imageProvider!!.addObserver(this)
        folderProvider!!.clear()
        initialSelect = intent.getSerializableExtra(PickerSettings.INITIAL_SELECT) as ArrayList<Media>?
        if (savedInstanceState == null) {
            imageProvider!!.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 0)
            imageProvider!!.setSelect(initialSelect)
        } else {
            if (imageProvider!!.maxSelect == 0) {
                imageProvider!!.maxSelect = intent.getIntExtra(PickerSettings.MAX_SELECT, 0)
                imageProvider!!.setSelect(initialSelect)
            }
            tmpFile = savedInstanceState.getSerializable("tmpFile") as File?
        }
        loadMedias()
        folderPopup = FolderPopup(this)

        btnReload!!.setOnClickListener {
            loadMedias()
        }
        btnCenter!!.setOnClickListener {
            if (!folderPopup!!.isShowing()) {
                folderPopup!!.show(btnCenter!!, object : FolderPopup.OnFolderSelectedListener {
                    override fun OnFolderSelected() {
                        btnCenter!!.text = folderProvider!!.selectedFolder!!.name
                        adapter!!.refresh(folderProvider!!.selectedFolder!!.medias)
                    }
                })
            }
        }
    }

    fun loadMedias() {
        permissionUtils!!.checkStoragePermission(Runnable {
            Thread(Runnable {
                if (this@PickerBaseActivity is PickerActivity) {
                    loadVideos()
                }
                loadImages()
                folderProvider!!.allFolder?.sort()
                Handler(mainLooper).post {
                    initData()
                }

            }).start()
        })
    }

    private fun loadVideos() {
        val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"
        val where = MediaStore.Video.Media.SIZE + ">" + 2000
        val cursor = contentResolver.query(contentUri, null, where, null, sortOrder)
        val allFolder = folderProvider!!.selectedFolder
        var videoFolder: Folder? = null
        while (cursor.moveToNext()) {
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            val date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
            val size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
            val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
            val width = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH))
            val height = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT))
            if (!File(path).exists()) continue
            val media = Media(date, path, size, Media.MediaType.VID, duration.toLong())
            media.width = width
            media.height = height
            if (allFolder?.firstMedia == null) {
                allFolder?.firstMedia = media
            }
            allFolder?.addMedia(media)
            if (videoFolder == null) {
                videoFolder = Folder("video", "所有视频", media)
                folderProvider!!.addFolder(videoFolder)
            }
            videoFolder.addMedia(media)
        }
        cursor.close()
    }

    private fun loadImages() {
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val where = MediaStore.Images.Media.SIZE + " > " + 2000
        val cursor = contentResolver.query(contentUri, null, where, null, sortOrder)
        val allFolder = folderProvider!!.selectedFolder
        while (cursor.moveToNext()) {
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
            val size = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
            if (!File(path).exists()) continue
            if (isGif(path)) continue
            val media = Media(date, path, size, Media.MediaType.IMG)
            if (allFolder?.firstMedia == null) {
                allFolder?.firstMedia = media
            }
            allFolder?.addMedia(media)
            if (!folderProvider!!.hasFolder(media.dir)) {
                val name = media.dir.substring(media.dir.lastIndexOf('/') + 1)
                folderProvider!!.addFolder(Folder(media.dir, name, media))
            }
            folderProvider!!.getFolderByDir(media.dir)?.addMedia(media)
        }
        cursor.close()
    }

    fun launchMediaRecord(isCamera: Boolean) {
        if (adapter == null) return
        if (!isCamera) {
            permissionUtils!!.checkRecordVideoPermission {
                RecordMediaActivity.startForResult(this, isCamera)
            }
        } else permissionUtils!!.checkCameraPermission {
            RecordMediaActivity.startForResult(this, isCamera)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val selectedFolder = folderProvider!!.selectedFolder
        when (requestCode) {
            PickerSettings.BIG_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) onPickerOk()
            }
            PickerSettings.CAREMA_REQUEST_CODE, PickerSettings.RECORD_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data?.getSerializableExtra(PickerSettings.RESULT) != null) {
                    val media = data.getSerializableExtra(PickerSettings.RESULT) as Media
                    imageProvider!!.add(media)
                    folderProvider!!.addNewMedia(media)
                    if (TextUtils.equals(selectedFolder!!.dir, media.dir) || selectedFolder.name == folderProvider!!.folders.get(0).name) {
                        adapter!!.refresh(selectedFolder.medias)
                    }
                    Logger.e(media.toString())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageProvider!!.deleteObservers()
        adapter?.destroy()
        permissionUtils?.destroy()
    }

    abstract fun initData()
    abstract fun onPickerOk()

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (tmpFile != null)
            outState!!.putSerializable("tmpFile", tmpFile)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            Glide.with(this).onTrimMemory(level)
        }
    }
}