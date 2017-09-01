package com.example.apple.glidetest

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.item_folder.view.*
import kotlinx.android.synthetic.main.popup_folder_popup.view.*
import kotlinx.android.synthetic.main.title_bar.view.*
import java.io.File

/**
 * Created by Apple on 17/8/23.
 */

class FolderPopup(private val context: Context) {

    private val popupWindow: PopupWindow
    private val folders = FolderProvider.instance.folders
    private var listener: OnFolderSelectedListener? = null
    private var contentView: View? = null

    init {
        contentView = View.inflate(context, R.layout.popup_folder_popup, null)
//        var height = context.getScreenHeight() - context.getStatusBarHeight() - context.dp2px(50f)
        popupWindow = PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffffff")))
        contentView!!.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        contentView!!.recyclerView.adapter = FolderAdapter()
        popupWindow.animationStyle = R.style.popup_folder_anim
        contentView!!.tvCenter.isSelected = true
        contentView!!.tvCenter.setOnClickListener {
            popupWindow.dismiss()
        }
    }


    fun show(parent: View, listener: OnFolderSelectedListener) {
        this.listener = listener
        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, 0, 0)
    }

    inner class FolderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (holder is FolderHolder) {
                if (folders.get(position).firstImagePath != null)
                    loadImage(File(folders.get(position).firstImagePath), holder.itemView.ivFolder)
                holder.itemView.tvFolder.text = folders.get(position).name
                holder.itemView.tvCount.text = "(" + folders.get(position).count.toString() + ")"
                holder.itemView.setOnClickListener {
                    FolderProvider.instance.selectedFolder = folders.get(position)
                    contentView!!.tvCenter.text = folders.get(position).name
                    popupWindow.dismiss()
                    listener?.OnFolderSelected()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return FolderHolder(context.getView(R.layout.item_folder, parent))
        }

        override fun getItemCount(): Int {
            return folders.size
        }
    }

    fun isShowing(): Boolean {
        return popupWindow.isShowing
    }

    class FolderHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnFolderSelectedListener {
        fun OnFolderSelected()
    }
}
