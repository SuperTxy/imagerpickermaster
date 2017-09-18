package com.example.apple.glidetest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.provider.SelectMediaProvider
import com.orhanobut.logger.Logger
import java.io.File

/**
 * Created by Apple on 17/7/31.
 */
//TODO("Glide 4.1.1")
fun loadImage(media: Media, imageView: ImageView) {
    val options = RequestOptions()
            .error(R.drawable.default_image)
    if (media.dir.isNullOrEmpty()) {
        Logger.e("此文件不存在！")
    } else if (media.isVideo)
        loadBitmap(media, imageView)
    else if (media.isGif)
        Glide.with(imageView.context).load(File(media.path)).apply(options).into(imageView)
    else Glide.with(imageView.context).load(File(media.path)).apply(options).into(imageView)
}

fun loadBitmap(media: Media, imageView: ImageView) {
    if (media.dir.isNullOrEmpty()) {
        Logger.e("此文件不存在！")
    } else {
        val options = RequestOptions()
                .centerCrop()
                .error(R.drawable.default_image)

        Glide.with(imageView.context).asBitmap().apply(options).load(File(media.path)).listener(object : RequestListener<Bitmap> {
            override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                return false

            }

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                Logger.e(e?.message)
                SelectMediaProvider.instance.damageMedias.add(media)
                return false
            }
        }).into(imageView)
    }
}

fun Context.getView(layoutId: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(this).inflate(layoutId, parent, false)
}

fun isGif(path: String): Boolean {
    return TextUtils.equals(getType(path), "gif")
}

fun getType(path: String): String {
    val options = BitmapFactory.Options()
    //        让图片不加载到内存中
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    val type = options.outMimeType
    //        ”image/png”、”image/jpeg”、”image/gif”
    if (TextUtils.isEmpty(type))
        return "未能识别的图片"
    else
        return type.substring(6, type.length)
}

fun unitFormat(i: Long): String {
    return if (i >= 0 && i < 10) "0" + i else i.toString() + ""
}

fun mills2Duration(mills: Long): String {
    var sec = mills.toLong() / 1000
    if (sec < 0) return "00:00"
    var min = sec / 60
    if (min < 60) {
        sec = sec % 60
        return unitFormat(min) + ":" + unitFormat(sec)
    } else {
        val hour = min / 60
        min = min % 60
        return unitFormat(hour) + ":" + unitFormat(min) + ":" + unitFormat(sec)
    }
}




