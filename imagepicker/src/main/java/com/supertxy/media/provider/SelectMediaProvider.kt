package com.supertxy.media.provider

import android.content.Context
import android.widget.Toast
import com.supertxy.media.bean.Change
import com.supertxy.media.bean.Media
import com.orhanobut.logger.Logger
import com.supertxy.media.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by Apple on 17/7/31.
 * 懒汉式加载
 */
class SelectMediaProvider private constructor() : Observable() {
    //    伴生对象，用来在class内部生成一个对象，lazy默认情况下是线程安全的
    companion object {
        val instance: SelectMediaProvider by lazy { SelectMediaProvider() }
    }

    var needSuffix: Boolean = false
        set(value) {
            field = value
        }

    var maxSelect: Int = 0
        get() = field
        set(value) {
            field = if (value > 0) value else 0
        }
    var selectedMedias = ArrayList<Media>()
    var selectedMap = HashMap<String, Media>()
    var damageMedias = ArrayList<Media>()

    var size: Int = 0
        get() = selectedMedias.size

    fun remove(media: Media) {
        if (selectedMap.containsKey(media.path)) {
            selectedMedias.remove(selectedMap.get(media.path)!!)
            selectedMap.remove(media.path)
            setChanged()
            notifyObservers(Change(Change.REMOVE, media))
            Logger.d("remove->" + selectedMedias.size + "-->" + media)
        } else {
            Logger.e("此图片不存在，无法移除")
        }
    }

    fun add(media: Media) {
        if (!selectedMap.containsKey(media.path)) {
            selectedMedias.add(media)
            selectedMap.put(media.path, media)
            setChanged()
            notifyObservers(Change(Change.ADD, media))
            Logger.d("add->" + selectedMedias.size + "-->" + media)
        } else {
            Logger.e("该图片已存在，无法添加")
        }
    }

    fun orderOfMedia(media: Media): String {
        if (selectedMap.containsKey(media.path)) {
            val index: Int = selectedMedias.indexOf(selectedMap.get(media.path)!!)
            return (index + 1).toString()
        }
        return ""
    }

    fun isMediaExist(media: Media): Boolean {
        return selectedMap.contains(media.path)
    }

    fun setSelect(medias: ArrayList<Media>?) {
        clear()
        needSuffix = false
        if (medias != null && medias.size > 0) {
            selectedMedias.addAll(medias)
            for (it in medias)
                selectedMap.put(it.path, it)
        }
    }

    fun clear() {
        selectedMedias.clear()
        damageMedias.clear()
        selectedMap.clear()
    }

    fun maxSelectToast(context: Context, isSelected: Boolean): Boolean {
        if (!isSelected && selectedMedias.size == maxSelect) {
            var toast = "最多只能选择" + maxSelect + "张图片"
            toast = if (needSuffix) toast + context.getString(R.string.max_toast_suffix) else toast
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }
}