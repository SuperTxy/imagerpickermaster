package com.example.apple.glidetest.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.Media
import com.example.apple.glidetest.listener.OnCameraClickListener
import com.example.apple.glidetest.listener.OnItemClickListener
import com.example.apple.glidetest.provider.SelectMediaProvider
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadBitmap
import com.txy.androidutils.TxyListUtils
import com.txy.androidutils.TxyToastUtils
import kotlinx.android.synthetic.main.image_all_item.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/8/1.
 * @param needSuffix 选择图片为最大数的提示是否需要suffix
 * @link SelectImageProvider.maxSelectToast
 */
class CommonImageAdapter(private val context: Context, images: ArrayList<Media>,
                         private var showCamera: Boolean = false)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Observer {

    private var medias: ArrayList<Media> = ArrayList()
    private var toastUtils: TxyToastUtils? = null
    var cameraClickListener: OnCameraClickListener? = null
        set(value) {
            field = value
        }
    var itemClickListener: OnItemClickListener? = null
        set(value) {
            field = value
        }

    init {
        toastUtils = TxyToastUtils(context)
        this.medias.clear()
        this.medias.addAll(images)
        SelectMediaProvider.instance.addObserver(this)
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
        val selectImageProvider = SelectMediaProvider.instance
        if (holder is ImageHolder) {
            val pos: Int = if (showCamera) position - 1 else position
            val media = medias.get(pos)
            val tvDuration = holder.itemView.tvDuration
            val cbSelected = holder.itemView.cbSelected
            loadBitmap(media, holder.itemView.ivImage)
            holder.itemView.ivVideo.visibility = if (media.isVideo) View.VISIBLE else View.GONE
            if (media.isVideo) {
                tvDuration.visibility = View.VISIBLE
                tvDuration.text = media.durationStr
            } else tvDuration.visibility = View.INVISIBLE
            tvDuration.visibility = if (media.isVideo) View.VISIBLE else View.INVISIBLE
            tvDuration.text
            handleSelected(selectImageProvider.isMediaExist(media), holder, media)
            holder.itemView.flSelected.setOnClickListener {
                if (selectImageProvider.maxSelectToast(context, cbSelected.isSelected)) return@setOnClickListener
                if (media.dir.isNullOrEmpty() && !cbSelected.isSelected) {
                    toastUtils?.toast("此图片已被删除！")
                    return@setOnClickListener
                }
                if (selectImageProvider.damageMedias.contains(media) && !cbSelected.isSelected) {
                    toastUtils?.toast("此图片文件已损坏！")
                    return@setOnClickListener
                }
                if (media.isVideo && media.isDurationlarge12 ){
                    toastUtils?.toast("视频限定时长12秒！")
                    return@setOnClickListener
                }
                if (media.isVideo && media.isSizeLarge10M){
                    toastUtils?.toast("视频大小超过限制！")
                    return@setOnClickListener
                }
                handleSelected(!cbSelected.isSelected, holder, media)
                if (cbSelected.isSelected) {
                    selectImageProvider.add(media)
                } else {
                    selectImageProvider.remove(media)
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

    fun handleSelected(isSelected: Boolean, holder: ImageHolder, media: Media) {
        val cbSelected = holder.itemView.cbSelected
        cbSelected.isSelected = isSelected
        cbSelected.text = if (isSelected) SelectMediaProvider.instance.orderOfMedia(media) else ""
        holder.itemView.viewMask.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectMediaProvider && arg is Change) {
            if (arg.isAdd) {
                var index = TxyListUtils.indexOfObj(medias, arg.media)
                if (index != -1) {
                    index = if (showCamera) index + 1 else index
                    notifyItemChanged(index)
                }
            } else {
                notifyDataSetChanged()
            }
        }
    }

    fun refresh(medias: ArrayList<Media>) {
        this.medias.clear()
        this.medias.addAll(medias)
        notifyDataSetChanged()
    }


    inner class CameraHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ImageHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return if (showCamera) medias.size + 1 else medias.size
    }

    fun destroy() {
        toastUtils?.destroy()
    }

}