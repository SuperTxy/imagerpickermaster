package com.example.apple.glidetest.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.listener.OnCameraClickListener
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.utils.OsUtils
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.image_all_item.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/8/1.
 */
class CommonImageAdapter(private val context: Context, images: ArrayList<String>, private var showCamera: Boolean = false)
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
            return CameraHolder(ImageView(context))
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
            context.loadImage(File(path), holder.itemView.ivImage)
            handleSelected(selectImageProvider.isPathExist(path), holder, path)
            cbSelected.setOnClickListener {
                if (selectImageProvider.maxSelectToast(context, cbSelected.isSelected)) return@setOnClickListener
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
            val cameraView = holder.itemView as ImageView
            cameraView.setImageResource(R.drawable.ic_photo_camera_white_48dp)
            cameraView.setOnClickListener {
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

    fun insertImage(image: String) {
        var index = if (showCamera) 1 else 0
        this.images.add(index, image)
        notifyItemInserted(index)
    }

    inner class CameraHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ImageHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return if (showCamera) images.size + 1 else images.size
    }

}