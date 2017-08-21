package com.example.apple.glidetest.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Created by Apple on 17/8/3.
 */
class PermissionUtils(private val context: Activity) {

    private var mHasPermissionRunnable: Runnable? = null
    private var mNoPermissionRunnable: Runnable? = null
    private var REQUEST_CODE_PERMISSION = 1000

    fun checkStoragePermission(hasPermissionDo: Runnable) {
        var permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        checkPermission(permission, hasPermissionDo, Runnable {
            context.showPermissionDialog("不开启存储权限，无法访问相册哦~")
        })
    }

    fun checkCameraPermission(hasPermissionDo: Runnable) {
        var permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermission(permission, hasPermissionDo, Runnable {
            context.showPermissionDialog("不开启相机权限，无法拍照哦~")
        })
    }

    fun checkPermission(permissions: Array<out String>, hasPermissionDo: Runnable, noPermissionDo: Runnable) {
        mHasPermissionRunnable = null
        mNoPermissionRunnable = null
        if (isPermissionsGranted(permissions)) hasPermissionDo.run()
        else if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissions.get(0))) {
            context.showPermissionDialog("不开启存储权限，无法访问相册哦~")
        } else {
            mHasPermissionRunnable = hasPermissionDo
            mNoPermissionRunnable = noPermissionDo
            ActivityCompat.requestPermissions(context, permissions, REQUEST_CODE_PERMISSION)
        }
    }


     fun isPermissionsGranted(permissions: Array<out String>): Boolean {
        for (it in permissions) {
            if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    private fun isAllGranted(grantResults: IntArray): Boolean {
        for (it in grantResults) {
            if (it != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (isAllGranted(grantResults))
                mHasPermissionRunnable?.run()
            else mNoPermissionRunnable?.run()
        }
    }
}