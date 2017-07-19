package com.example.apple.glidetest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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

public class ImageAllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer {

    private Context context;
    private List<String> imgs = new ArrayList<>();
    private OnImageAllListener listener;
    private final int CAMERA = 1;
    private final int IMAGE = 2;

    public ImageAllAdapter(Context context) {
        this.context = context;
        SelectImageProvider.getInstance().addObserver(this);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? CAMERA : IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CAMERA) {
            return new CameraHolder(new ImageView(context));
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.image_all_item, null);
            return new ImageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageHolder) {
            ((ImageHolder) holder).setData(position - 1);
        } else {
            ((CameraHolder) holder).setData();
        }
    }

    @Override
    public int getItemCount() {
        return imgs == null ? 0 : imgs.size() + 1;
    }

    public void refresh(List<String> list) {
        imgs.clear();
        imgs.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Change) {
            Change change = (Change) arg;
            int index = ListUtils.getIndexInList(imgs, change.path);
            notifyItemChanged(index+1);
        }
    }

    private class CameraHolder extends RecyclerView.ViewHolder {

        private ImageView iv;

        CameraHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView;
        }

        void setData() {
            iv.setImageResource(R.drawable.ic_photo_camera_white_48dp);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onCameraClick();
                    }
                }
            });
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        View viewMask;
        ImageView cbSelected;

        private ImageHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
            cbSelected = (ImageView) itemView.findViewById(R.id.cb_selected);
            viewMask = itemView.findViewById(R.id.view_mask);
        }

        public void setData(final int pos) {
            boolean isSelected = SelectImageProvider.getInstance().isPathExist(imgs.get(pos));
            cbSelected.setSelected(isSelected);
            viewMask.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            GlideUtils.loadImage(new File(imgs.get(pos)), context, ivImage);
            cbSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SelectImageProvider.getInstance().isSelectedMax() && !cbSelected.isSelected()) {
                        Toast.makeText(context, "你已选择" + SelectImageProvider.getInstance().maxSelect + "张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cbSelected.setSelected(!cbSelected.isSelected());
                    viewMask.setVisibility(cbSelected.isSelected() ? View.VISIBLE : View.GONE);
                    if (cbSelected.isSelected())
                        SelectImageProvider.getInstance().add(imgs.get(pos));
                    else SelectImageProvider.getInstance().remove(imgs.get(pos));
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(pos, cbSelected.isSelected());
                    }
                }
            });
        }
    }

    public void setOnImageAllListener(OnImageAllListener listener) {
        this.listener = listener;
    }

    public interface OnImageAllListener {

        void onItemClick(int position, boolean checked);

        void onCameraClick();
    }
}
