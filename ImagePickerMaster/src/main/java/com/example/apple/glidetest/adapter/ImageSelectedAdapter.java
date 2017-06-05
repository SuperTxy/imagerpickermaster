package com.example.apple.glidetest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.apple.glidetest.R;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.GlideUtils;

import java.io.File;

/**
 * Created by Apple on 17/5/27.
 */

public class ImageSelectedAdapter extends RecyclerView.Adapter<ImageSelectedAdapter.ViewHolder> {

    private Context context;
    private OnImageSelectedListener listener;
    private final SelectImageProvider imageProvider;

    public ImageSelectedAdapter(Context context) {
        this.context = context;
        imageProvider = SelectImageProvider.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_seleted_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(imageProvider.getSelectedImgs().get(position), position);
    }

    @Override
    public int getItemCount() {
        return imageProvider.getSelectedImgs().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivDel;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage= (ImageView) itemView.findViewById(R.id.iv_image);
            ivDel= (ImageView) itemView.findViewById(R.id.iv_del);
        }

        public void setData(final String path, final int position) {
            GlideUtils.loadImage(new File(path), context, ivImage);
            ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        imageProvider.remove(path);
                        notifyItemRemoved(position);
                        listener.onImageDel(path);
                    }
                }
            });
        }
    }

    public interface OnImageSelectedListener {
        void onImageDel(String path);
    }

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.listener = listener;
    }
}
