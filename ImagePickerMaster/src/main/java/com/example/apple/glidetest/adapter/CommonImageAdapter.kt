package com.example.apple.glidetest.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.listener.OnCameraClickListener
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.utils.OsUtils
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadImage
import com.example.apple.glidetest.utils.toastStr
import kotlinx.android.synthetic.main.image_all_item.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/8/1.
 * @param needSuffix 选择图片为最大数的提示是否需要suffix
 * @link SelectImageProvider.maxSelectToast
 */
class CommonImageAdapter(private val context: Context, images: ArrayList<String>,
                         private var showCamera: Boolean = false)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Observer {

    private var images: ArrayList<String> = ArrayList()
    var cameraClickListener: OnCameraClickListener? = null
        set(value) {
            field = value
        }
    var itemClickListener: OnItemClickListener? = null
        set(value) {
            field = value
        }

    init {
        this.images.clear()
        this.images.addAll(images)
        SelectImageProvider.instance.addObserver(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && showCamera) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return CameraHolder(context.getView(R.layout.camera))
        } else {
            return ImageHolder(context.getView(R.layout.image_all_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val selectImageProvider = SelectImageProvider.instance
        if (holder is ImageHolder) {
            val pos: Int = if (showCamera) position - 1 else position
            val path = images.get(pos)
            val cbSelected = holder.itemView.cbSelected
            loadImage(File(path), holder.itemView.ivImage)
            handleSelected(selectImageProvider.isPathExist(path), holder, path)
            holder.itemView.flSelected.setOnClickListener {
                if (selectImageProvider.maxSelectToast(context, cbSelected.isSelected)) return@setOnClickListener
                if (!File(path).exists()){
                    context.toastStr("此图片已被删除")
                    return@setOnClickListener
                }
                handleSelected(!cbSelected.isSelected, holder, path)
                if (cbSelected.isSelected) {
                    selectImageProvider.add(path)
                } else {
                    selectImageProvider.remove(path)
                }
            }
            holder.itemView.setOnClickListener {
                itemClickListener?.onItemClick(pos)
            }
        } else if (holder is CameraHolder) {
            holder.itemView.setOnClickListener {
                if (!selectImageProvider.maxSelectToast(context, false)) {
                    cameraClickListener?.onCameraClick()
                }
            }
        }
    }

    fun handleSelected(isSelected: Boolean, holder: ImageHolder, path: String) {
        val cbSelected = holder.itemView.cbSelected
        cbSelected.isSelected = isSelected
        cbSelected.text = if (isSelected) SelectImageProvider.instance.getPathIndex(path) else ""
        holder.itemView.viewMask.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change) {
            if (arg.isAdd) {
                var index = OsUtils.getIndexInList(images, arg.path)
                if (index != -1) {
                    index = if (showCamera) index + 1 else index
                    notifyItemChanged(index)
                }
            }else{
                notifyDataSetChanged()
            }
        }
    }

    fun refresh(images: ArrayList<String>) {
        this.images.clear()
        this.images.addAll(images)
        notifyDataSetChanged()
    }


    inner class CameraHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ImageHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return if (showCamera) images.size + 1 else images.size
    }

}