package com.example.apple.glidetest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.apple.glidetest.R;
import com.example.apple.glidetest.bean.Change;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.GlideUtils;
import com.example.apple.glidetest.utils.ListUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Apple on 17/5/27.
 */

public class ImageSelectedAdapter extends RecyclerView.Adapter<ImageSelectedAdapter.ViewHolder> implements Observer {

    private Context context;
    private List<String> imgs = new ArrayList<>();

    public ImageSelectedAdapter(Context context, List<String> list) {
        this.context = context;
        imgs.addAll(list);
        SelectImageProvider.getInstance().addObserver(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_seleted_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData();
    }

    @Override
    public int getItemCount() {
        return imgs == null ? 0 : imgs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDel;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
            ivDel = (ImageView) itemView.findViewById(R.id.iv_del);
        }

        public void setData() {
            GlideUtils.loadImage(new File(imgs.get(getAdapterPosition())), context, ivImage);
            ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectImageProvider.getInstance().remove(imgs.get(getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Change) {
            Change change = (Change) arg;
            if (change.isAdd()) {
                imgs.add(change.path);
                notifyItemInserted(imgs.size() - 1);
            } else {
                int index = ListUtils.getIndexInList(imgs, change.path);
                imgs.remove(change.path);
                notifyItemRemoved(index);
            }
        }
    }
}
