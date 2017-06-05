package com.example.apple.glidetest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.apple.glidetest.R;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.GlideUtils;
import com.example.apple.glidetest.utils.ListUtils;
import java.io.File;
import java.util.List;

/**
 * Created by Apple on 17/5/27.
 */

public class ImageAllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> imgs;
    private OnImageAllListener listener;
    private int checkChangedIndex = -1;
    private final int CAMERA = 1;
    private final int IMAGE = 2;

    public ImageAllAdapter(Context context) {
        this.context = context;
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
            ((ImageHolder) holder).setData(imgs.get(position - 1));
        } else {
            ((CameraHolder) holder).setData();
        }
    }

    @Override
    public int getItemCount() {
        return imgs == null ? 0 : imgs.size() + 1;
    }

    public void refresh(List<String> imgs) {
        this.imgs = imgs;
        notifyDataSetChanged();
    }

    public void changeChecked(String path) {
        checkChangedIndex = ListUtils.getIndexInList(imgs, path);
        notifyItemChanged(checkChangedIndex);
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        private ImageView iv;

        public CameraHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView;
        }

        public void setData() {
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

    class ImageHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        View viewMask;
        ImageView cbSelected;

        public ImageHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
            cbSelected = (ImageView) itemView.findViewById(R.id.cb_selected);
            viewMask = itemView.findViewById(R.id.view_mask);
        }

        public void setData(final String path) {
            boolean isSelected = SelectImageProvider.getInstance().getSelectedImgs().contains(path);
            cbSelected.setSelected(isSelected);
            viewMask.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            GlideUtils.loadImage(new File(path), context, ivImage);
            cbSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SelectImageProvider.getInstance().isSelectedMax() && !cbSelected.isSelected()) {
                        Toast.makeText(context, "你已选择" + SelectImageProvider.getInstance().maxSelect + "张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cbSelected.setSelected(!cbSelected.isSelected());
                    if (listener != null) {
                        viewMask.setVisibility(cbSelected.isSelected() ? View.VISIBLE : View.GONE);
                        listener.onCheckChanged(cbSelected.isSelected(), path);
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(path, cbSelected.isSelected());
                    }
                }
            });
        }
    }

    public void setOnImageAllListener(OnImageAllListener listener) {
        this.listener = listener;
    }

    public interface OnImageAllListener {
        void onCheckChanged(boolean isChecked, String path);

        void onItemClick(String path, boolean checked);

        void onCameraClick();
    }
}
