package com.example.apple.glidetest

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.apple.glidetest.bean.FolderProvider
import com.example.apple.glidetest.utils.StatusBarUtil
import com.example.apple.glidetest.utils.getView
import com.example.apple.glidetest.utils.loadImage
import kotlinx.android.synthetic.main.activity_folder_select.*
import kotlinx.android.synthetic.main.item_folder.view.*
import kotlinx.android.synthetic.main.title_bar.*
import java.io.File

class FolderSelectActivity : Activity() {
    private val folders = FolderProvider.instance.folders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarColorWhite(this)
        setContentView(R.layout.activity_folder_select)
        tvRight.setOnClickListener {
            setResult(RESULT_CANCELED,intent)
            finish()
        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = FolderAdapter()
    }

    inner class FolderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (holder is FolderHolder) {
                loadImage(File(folders.get(position).firstImagePath), holder.itemView.ivFolder)
                holder.itemView.tvFolder.text = folders.get(position).name
                holder.itemView.tvCount.text = "("+folders.get(position).count.toString()+")"
                holder.itemView.setOnClickListener {
                    FolderProvider.instance.selectedFolder = folders.get(position)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return FolderHolder(this@FolderSelectActivity.getView(R.layout.item_folder,parent))
        }

        override fun getItemCount(): Int {
            return folders.size
        }
    }

    class FolderHolder(view: View) : RecyclerView.ViewHolder(view)
}
