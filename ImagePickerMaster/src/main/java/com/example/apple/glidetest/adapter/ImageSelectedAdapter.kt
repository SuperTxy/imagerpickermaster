package com.example.apple.glidetest.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.apple.glidetest.R
import com.example.apple.glidetest.bean.Change
import com.example.apple.glidetest.bean.SelectImageProvider
import com.example.apple.glidetest.utils.OsUtils
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.image_seleted_item.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Apple on 17/5/27.
 */

class ImageSelectedAdapter(private val context: Context, list: List<String>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Observer {

    private val imgs = ArrayList<String>()

    init {
        imgs.addAll(list)
        SelectImageProvider.instance.addObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(context.getView(R.layout.image_seleted_item))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == imgs.size) {
            holder.itemView.tv_blank.visibility = View.VISIBLE
            holder.itemView.tv_blank.text = (position + 1).toString()
        } else {
            loadImage(imgs.get(position), holder.itemView.ivImage)
            holder.itemView.tv_blank.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return if (imgs.size < SelectImageProvider.instance.maxSelect && imgs.size != 0)
            imgs.size + 1 else imgs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change) {
            if (arg.isAdd) {
                imgs.add(arg.path)
                if (imgs.size == itemCount)
                    notifyItemRemoved(itemCount - 1)
                notifyItemInserted(imgs.size - 1)
                if (imgs.size == itemCount - 1)
                    notifyItemChanged(itemCount - 1)
            } else {
                val index = OsUtils.getIndexInList(imgs, arg.path)
                imgs.remove(arg.path)
                notifyItemRemoved(index)
                if (imgs.size == SelectImageProvider.instance.maxSelect - 1)
                    notifyItemInserted(itemCount - 1)
                if(imgs.size == 0) notifyItemRemoved(0)
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
