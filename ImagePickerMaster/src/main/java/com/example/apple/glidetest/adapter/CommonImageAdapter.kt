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
            val cbSelected = holder.itemView.cbSelected
            context.loadImage(File(images.get(pos)), holder.itemView.ivImage)
            val isSelected = selectImageProvider.isPathExist(images.get(pos))
            cbSelected.isSelected = isSelected
            holder.itemView.viewMask.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            cbSelected.setOnClickListener {
                if (selectImageProvider.maxSelectToast(context, cbSelected.isSelected)) return@setOnClickListener
                cbSelected.isSelected = !cbSelected.isSelected
                holder.itemView.viewMask.visibility = if (cbSelected.isSelected) View.VISIBLE else View.INVISIBLE
                if (cbSelected.isSelected) {
                    selectImageProvider.add(images.get(pos))
                } else {
                    selectImageProvider.remove(images.get(pos))
                }
            }
            holder.itemView.setOnClickListener {
                itemClickListener?.onItemClick(pos, cbSelected.isSelected)
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

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change) {
            var index = OsUtils.getIndexInList(images, arg.path)
            if (index != -1) {
                index = if (showCamera) index + 1 else index
                notifyItemChanged(index)
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