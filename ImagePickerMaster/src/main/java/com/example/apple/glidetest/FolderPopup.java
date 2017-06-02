package com.example.apple.glidetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.apple.glidetest.bean.Folder;
import com.example.apple.glidetest.bean.FolderProvider;
import com.example.apple.glidetest.utils.GlideUtils;
import com.example.apple.glidetest.utils.OsUtils;
import java.io.File;

/**
 * Created by Apple on 17/5/31.
 */

public class FolderPopup {

    ListView lvFolder;
    TextView tvFolderShow;
    private PopupWindow popupWindow;
    private FolderProvider folderProvider;

    public FolderPopup(Context context, final OnFolderSelectListener listener) {
        View view = View.inflate(context, R.layout.folder_popup, null);
        view.findViewById(R.id.tv_folder_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFolderShow.setSelected(!tvFolderShow.isSelected());
                popupWindow.dismiss();
            }
        });
        lvFolder = (ListView) view.findViewById(R.id.lv_folder);
        tvFolderShow = (TextView) view.findViewById(R.id.tv_folder_show);

        int height = OsUtils.getScreenHeight(context) - OsUtils.dp2px(context, 100) - OsUtils.getStatusBarHeight(context);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, height);
        folderProvider = FolderProvider.getInstance();
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        popupWindow.setAnimationStyle(R.style.popup_anim);
        lvFolder.setAdapter(new MyAdapter());
        tvFolderShow.setText(folderProvider.getSelectedFolder().name);
        tvFolderShow.setSelected(true);
        lvFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                folderProvider.setSelectedFolder(folderProvider.getFolders().get(position));
                tvFolderShow.setText(folderProvider.getSelectedFolder().name);
                if (listener != null) {
                    listener.onFolderSelected();
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (listener != null) {
                    listener.onDismiss();
                }
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return folderProvider.getCount();
        }

        @Override
        public Folder getItem(int position) {
            return folderProvider.getFolders().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(lvFolder.getContext(), R.layout.item_folder_popup, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            GlideUtils.loadImage(new File(getItem(position).firstImagePath), lvFolder.getContext(), holder.ivFolder);
            holder.tvFolder.setText(getItem(position).name + " (" + getItem(position).getCount() + ")");
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView ivFolder ;
        TextView tvFolder;

        ViewHolder(View view) {
            ivFolder= (ImageView) view.findViewById(R.id.iv_folder);
            tvFolder= (TextView) view.findViewById(R.id.tv_folder);
        }
    }

    public void showPopup(View parent) {
        popupWindow.showAtLocation(parent, Gravity.TOP, 0, 0);
    }

    public interface OnFolderSelectListener {
        void onFolderSelected();

        void onDismiss();
    }
}
