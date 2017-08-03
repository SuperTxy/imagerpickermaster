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
import java.io.File
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
        context.loadImage(File(imgs.get(position)), holder.itemView.ivImage)
    }

    override fun getItemCount(): Int {
        return imgs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun update(o: Observable?, arg: Any?) {
        if (o is SelectImageProvider && arg is Change) {
            if (arg.isAdd) {
                imgs.add(arg.path)
                notifyItemInserted(imgs.size - 1)
            } else {
                val index = OsUtils.getIndexInList(imgs, arg.path)
                imgs.remove(arg.path)
                notifyItemRemoved(index)
            }
        }
    }

    fun refresh(selectedImgs: ArrayList<String>) {
        imgs.clear()
        imgs.addAll(selectedImgs)
        notifyDataSetChanged()
    }
}
