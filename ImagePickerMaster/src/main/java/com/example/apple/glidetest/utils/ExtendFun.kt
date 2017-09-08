package com.example.apple.glidetest.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.SelectImageProvider
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.alert_dialog.view.*
import java.io.File
import java.lang.Exception

/**
 * Created by Apple on 17/7/31.
 */
//    扩展函数
fun Activity.showAlertDialog(message: String, leftStr: String, rightStr: String, leftListener: OnClickListener?, rightListener: OnClickListener?) {
    val dialog = Dialog(this, R.style.mydialogstyle)
    val view = inflate(this, R.layout.alert_dialog, null)
    dialog.setContentView(view)
    view.tv_alert_negative.text = leftStr
    view.tv_alert_positive.text = rightStr
    view.tv_alert_message.text = message
    view.tv_alert_negative.setOnClickListener { v ->
        leftListener?.onClick(v)
        dialog.dismiss()
    }
    view.tv_alert_positive.setOnClickListener { v ->
        rightListener?.onClick(v)
        dialog.dismiss()
    }
    dialog.show()
}

fun Activity.showPermissionDialog(message: String) {
    showAlertDialog(message, "取消", "去开启", null, object : OnClickListener {
        override fun onClick(v: View) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:" + packageName))
            startActivity(intent)
        }
    })
}

fun Context.dp2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5).toInt()
}

fun Context.getScreenHeight(): Int {
    val displayMetrics = resources.displayMetrics
    return displayMetrics.heightPixels
}

fun Context.getStatusBarHeight(): Int {
    var statusBarHeight = -1
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

fun loadImage(file: File, imageView: ImageView) {
    Glide.with(imageView.context).load(file).listener(object : RequestListener<File, GlideDrawable> {
        override fun onException(e: Exception?, model: File?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
            Logger.e(model?.absolutePath)
            Logger.e(e?.message)
            if ("setDataSource failed: status = 0x80000000".equals(e?.message)){
                SelectImageProvider.instance.damageImgs.add(file.absolutePath)
            }
            return false
        }

        override fun onResourceReady(resource: GlideDrawable?, model: File?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            return false
        }
    }).error(R.drawable.default_image).centerCrop().into(imageView)
}

fun loadImage(path:String,imageView: ImageView){
    if(!File(path).exists()) {
        Logger.e("此图片文件不存在！")
        return
    }
    if (path.endsWith(".gif"))
        loadGifImage(File(path),imageView)
    else loadImage(File(path),imageView)
}

fun loadGifImage(file: File, imageView: ImageView) {
    Glide.with(imageView.context).load(file).asBitmap().listener(object :  RequestListener<File, Bitmap> {
        override fun onException(e: Exception?, model: File?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
            Logger.e(model?.absolutePath)
            Logger.e(e?.message)
            SelectImageProvider.instance.damageImgs.add(file.absolutePath)
            return false
        }

        override fun onResourceReady(resource: Bitmap?, model: File?, target: Target<Bitmap>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            return false
        }

    }).error(R.drawable.default_image).centerCrop().into(imageView)
}

fun Context.getView(layoutId: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(layoutId, parent, false)
}

fun Context.toastStrId(resId: Int) {
    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
}

fun Context.toastStr(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}

interface OnClickListener {
    fun onClick(v: View)
}
