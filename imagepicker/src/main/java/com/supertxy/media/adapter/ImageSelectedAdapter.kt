package com.supertxy.media.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.supertxy.media.R
import com.supertxy.media.bean.Change
import com.supertxy.media.bean.Media
import com.supertxy.media.provider.SelectMediaProvider
import com.supertxy.media.utils.getView
import com.supertxy.media.utils.loadBitmap
import com.txy.androidutils.TxyListUtils
import kotlinx.android.synthetic.main.image_seleted_item.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/5/27.
 */

class ImageSelectedAdapter(private val context: Context, list: List<Media>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Observer {

    private val medias = ArrayList<Media>()

    init {
        medias.addAll(list)
        SelectMediaProvider.instance.addObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(context.getView(R.layout.image_seleted_item))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == medias.size) {
            holder.itemView.tv_blank.visibility = View.VISIBLE
            holder.itemView.tv_blank.text = (position + 1).toString()
            holder.itemView.ivVideo.visibility = View.GONE
        } else {
            loadBitmap(medias.get(position), holder.itemView.ivImage)
            holder.itemView.tv_blank.visibility = View.GONE
            holder.itemView.ivVideo.visibility = if (medias.get(position).isVideo) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int {
        return if (medias.size < SelectMediaProvider.instance.maxSelect && medias.size != 0)
            medias.size + 1 else medias.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectMediaProvider && arg is Change) {
            if (arg.isAdd) {
                medias.add(arg.media)
                if (medias.size == itemCount)
                    notifyItemRemoved(itemCount - 1)
                notifyItemInserted(medias.size - 1)
                if (medias.size == itemCount - 1)
                    notifyItemChanged(itemCount - 1)
            } else {
                val index = TxyListUtils.indexOfObj(medias, arg.media)
                medias.remove(arg.media)
                notifyItemRemoved(index)
                if (medias.size == SelectMediaProvider.instance.maxSelect - 1)
                    notifyItemInserted(itemCount - 1)
                if (medias.size == 0) notifyItemRemoved(0)
                else notifyItemChanged(itemCount - 1)
            }
            listener?.onUpdateMove()
        }
    }

    private var listener: OnUpdateMoveListener? = null

    fun setOnUpdateMoveListener(listener: OnUpdateMoveListener) {
        this.listener = listener
    }

    interface OnUpdateMoveListener {
        fun onUpdateMove()
    }
}
