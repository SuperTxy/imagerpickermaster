package com.example.apple.glidetest.bean

import android.content.Context
import android.widget.Toast
import com.example.apple.glidetest.R
import com.orhanobut.logger.Logger
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/7/31.
 * 懒汉式加载
 */
class SelectImageProvider private constructor() : Observable() {
    //    伴生对象，用来在class内部生成一个对象，lazy默认情况下是线程安全的
    companion object {
        val instance: SelectImageProvider by lazy { SelectImageProvider() }
    }

    var maxSelect: Int = 1
        get() = field
        set(value) {
            field = if (value > 0) value else 1
        }
    var selectedImgs = ArrayList<String>()
        get() = field
        set(value) {
            field = value
        }
    var size: Int = 0
        get() = selectedImgs.size

    fun remove(path: String) {
        if (selectedImgs.contains(path)) {
            selectedImgs.remove(path)
            setChanged()
            notifyObservers()
            Logger.d("remove->" + selectedImgs.size + "-->" + path)
        } else {
            Logger.e("此图片不存在，无法移除")
        }
    }

    fun add(path: String) {
        if (!selectedImgs.contains(path)) {
            selectedImgs.add(path)
            setChanged()
            notifyObservers()
            Logger.d("add->" + selectedImgs.size + "-->" + path)
        } else {
            Logger.e("该图片已存在，无法添加")
        }
    }

    fun isPathExist(path: String): Boolean {
        return selectedImgs.contains(path)
    }

    fun clear() {
        selectedImgs.clear()
//        setChanged()
//        notifyObservers()
    }


    fun maxSelectToast(context: Context, isSelected: Boolean, needSuffix: Boolean = false): Boolean {
        if (!isSelected && selectedImgs.size == maxSelect) {
            var toast = "最多只能选择" + maxSelect + "张图片"
            toast = if (needSuffix) toast + context.getString(R.string.max_toast_suffix) else toast
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }
}