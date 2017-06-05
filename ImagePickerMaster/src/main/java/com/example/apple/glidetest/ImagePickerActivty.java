package com.example.apple.glidetest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.apple.glidetest.adapter.ImageAllAdapter;
import com.example.apple.glidetest.adapter.ImageSelectedAdapter;
import com.example.apple.glidetest.bean.Folder;
import com.example.apple.glidetest.bean.FolderProvider;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.FileUtils;
import com.example.apple.glidetest.utils.ListUtils;
import com.example.apple.glidetest.utils.OsUtils;
import com.example.apple.glidetest.utils.PickerSettings;
import com.example.apple.glidetest.view.SpaceItemDecoration;

import java.io.File;
import java.io.IOException;

public class ImagePickerActivty extends Activity {

    RecyclerView recyclerImageAll;
    RecyclerView recyclerImagePicked;
    ViewPager pagerBigImage;
    TextView tvShowFolder;
    TextView tvPickImage;
    Button btnPickOk;
    CheckBox cbBigCheck;
    LinearLayout pickerAll;
    LinearLayout bigImage;

    private static final String FILE_PROVIDER = "com.example.apple.glidetest.fileprovider";
    private final int PERMISSION_READ_STORAGE_CODE = 001;
    private final int PERMISSION_CAREMA_CODE = 002;
    private final int CAREMA_REQUEST_CODE = 003;
    private final int HORIZONTAL_COUNT = 3;  //  Recycler每行显示的图片个数

    private FolderProvider folderProvider;
    private SelectImageProvider imageProvider;

    private ImageAllAdapter imageAllAdapter;
    private ImageSelectedAdapter imageSelectedAdapter;

    private MyPagerAdapter myPagerAdapter;
    private FolderPopup folderPopup;  //查看大图的popup
    private File tmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker_activty);
        initView();
        imageProvider = SelectImageProvider.getInstance();
        folderProvider = FolderProvider.getInstance();
        imageProvider.clear();
        initRecycler();
        checkReadStoragePermission();
        imageProvider.setOnSelectChangedListener(new SelectImageProvider.OnSelectChangedListener() {
            @Override
            public void onSelectChanged() {
                btnPickOk.setEnabled(imageProvider.getCount() > 0);
                btnPickOk.setText("确认\n" + imageProvider.getSelectedImgs().size() + "/" + imageProvider.maxSelect);
            }
        });
        cbBigCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageProvider.isSelectedMax() && !cbBigCheck.isSelected()) {
                    Toast.makeText(ImagePickerActivty.this, "你已选择" + SelectImageProvider.getInstance().maxSelect + "张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                cbBigCheck.setSelected(!cbBigCheck.isSelected());
                String path = folderProvider.getSelectedFolder().imgs.get(pagerBigImage.getCurrentItem());
                checkChanged(cbBigCheck.isSelected(), path);
                imageAllAdapter.notifyItemChanged(pagerBigImage.getCurrentItem());
            }
        });
    }

    private void initView() {
        recyclerImageAll = (RecyclerView) findViewById(R.id.recycler_image_all);
        recyclerImagePicked = (RecyclerView) findViewById(R.id.recycler_image_picked);
        pagerBigImage = (ViewPager) findViewById(R.id.pager_big_image);
        tvShowFolder = (TextView) findViewById(R.id.tv_show_folder);
        tvPickImage = (TextView) findViewById(R.id.tv_pick_hint);
        btnPickOk = (Button) findViewById(R.id.btn_pick_ok);
        cbBigCheck = (CheckBox) findViewById(R.id.cb_big_check);
        pickerAll = (LinearLayout) findViewById(R.id.picker_all);
        bigImage = (LinearLayout) findViewById(R.id.big_image);
        findViewById(R.id.iv_big_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerAll.setVisibility(View.VISIBLE);
                bigImage.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.tv_show_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderPopup.showPopup(tvShowFolder);
                tvShowFolder.setSelected(true);
            }
        });
        findViewById(R.id.btn_pick_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putStringArrayListExtra(PickerSettings.RESULT, imageProvider.getSelectedImgs());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initRecycler() {
        //        中间图片展示Recycler
        recyclerImageAll.setLayoutManager(new GridLayoutManager(this, HORIZONTAL_COUNT));
        recyclerImageAll.addItemDecoration(new SpaceItemDecoration(OsUtils.dp2px(this, 2), HORIZONTAL_COUNT));
        imageAllAdapter = new ImageAllAdapter(this);
        recyclerImageAll.setAdapter(imageAllAdapter);
        imageAllAdapter.setOnImageAllListener(onImageAllListener);
//      底部选中图片Recycler
        recyclerImagePicked.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageSelectedAdapter = new ImageSelectedAdapter(this);
        recyclerImagePicked.setAdapter(imageSelectedAdapter);
        imageSelectedAdapter.setOnImageSelectedListener(onImageSelectedListener);
//      查看大图的pager
        myPagerAdapter = new MyPagerAdapter();
        pagerBigImage.setAdapter(myPagerAdapter);
        pagerBigImage.addOnPageChangeListener(onPageChangeListener);
    }

    private void loadFolderAndImages() {
        new Thread(new Runnable() {
            public void run() {
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
                Folder allFolder = folderProvider.getSelectedFolder();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if (allFolder.firstImagePath == null) {
                        allFolder.firstImagePath = path;
                    }
                    allFolder.addImage(path);
                    String dir = new File(path).getParentFile().getAbsolutePath();
                    if (!folderProvider.hasFolder(dir)) {
                        String name = dir.substring(dir.lastIndexOf("/") + 1);
                        folderProvider.addFolder(new Folder(dir, name, path));
                    }
                    folderProvider.getFolderByDir(dir).addImage(path);
                }
                cursor.close();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                });
            }
        }).start();
    }

    private void initData() {
        final Folder selectedFolder = folderProvider.getSelectedFolder();
        imageAllAdapter.refresh(selectedFolder.imgs);
        myPagerAdapter.notifyDataSetChanged();
        tvShowFolder.setText(selectedFolder.name);
        folderPopup = new FolderPopup(this, new FolderPopup.OnFolderSelectListener() {
            @Override
            public void onFolderSelected() {
                tvShowFolder.setText(folderProvider.getSelectedFolder().name);
                imageAllAdapter.refresh(folderProvider.getSelectedFolder().imgs);
                myPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDismiss() {
                tvShowFolder.setSelected(false);
            }
        });
    }

    private ImageAllAdapter.OnImageAllListener onImageAllListener = new ImageAllAdapter.OnImageAllListener() {
        @Override
        public void onCheckChanged(boolean isChecked, String path) {
            checkChanged(isChecked, path);
        }

        @Override
        public void onItemClick(String path, boolean checked) {
            pickerAll.setVisibility(View.GONE);
            bigImage.setVisibility(View.VISIBLE);
            int currentItem = ListUtils.getIndexInList(folderProvider.getSelectedFolder().imgs, path);
            pagerBigImage.setCurrentItem(currentItem);
        }

        @Override
        public void onCameraClick() {
            requestCameraPermissions();
        }
    };

    private void requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAREMA_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                tmpFile = FileUtils.createTmpFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tmpFile != null && tmpFile.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER, tmpFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                } else {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
                }
                startActivityForResult(intent, CAREMA_REQUEST_CODE);
            } else {
                Toast.makeText(ImagePickerActivty.this, getString(R.string.image_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ImagePickerActivty.this, getString(R.string.can_not_launch_camera), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkChanged(boolean isChecked, String path) {
        if (isChecked) {
            imageProvider.add(path);
            imageSelectedAdapter.notifyItemInserted(imageProvider.getLastIndex());
            recyclerImagePicked.scrollToPosition(imageProvider.getLastIndex());
        } else {
            int index = ListUtils.getIndexInList(imageProvider.getSelectedImgs(), path);
            imageProvider.remove(path);
            imageSelectedAdapter.notifyItemRemoved(index);
        }
        boolean imageEmpty = imageProvider.getSelectedImgs().size() == 0;
        tvPickImage.setVisibility(imageEmpty ? View.VISIBLE : View.GONE);
        recyclerImagePicked.setVisibility(imageEmpty ? View.GONE : View.VISIBLE);
    }

    private ImageSelectedAdapter.OnImageSelectedListener onImageSelectedListener = new ImageSelectedAdapter.OnImageSelectedListener() {
        @Override
        public void onImageDel(String path) {
            String currPath = folderProvider.getSelectedFolder().imgs.get(pagerBigImage.getCurrentItem());
            if (OsUtils.isVisible(bigImage) && currPath.equals(path)) {
                cbBigCheck.setSelected(false);
            }
            imageAllAdapter.changeChecked(path);
        }
    };


    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            String path = folderProvider.getSelectedFolder().imgs.get(position);
            cbBigCheck.setSelected(imageProvider.getSelectedImgs().contains(path));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void checkReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_STORAGE_CODE);
        } else {
            loadFolderAndImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_STORAGE_CODE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFolderAndImages();
                } else {
                    Toast.makeText(ImagePickerActivty.this, getString(R.string.pemission_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_CAREMA_CODE:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(ImagePickerActivty.this, getString(R.string.pemission_error), Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAREMA_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                if(tmpFile != null) {
//                    notify system
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(tmpFile)));
                    imageProvider.add(tmpFile.getAbsolutePath());
                    imageSelectedAdapter.notifyItemInserted(imageProvider.getCount());
                }
            }else{
//               if user click cancel ,delete the temp file
                if(tmpFile != null && tmpFile.exists()) {
                    boolean success = tmpFile.delete();
                    if(success) {
                        tmpFile = null;
                    }
                }
            }
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            Folder selectedFolder = folderProvider.getSelectedFolder();
            return selectedFolder == null ? 0 : selectedFolder.imgs.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(ImagePickerActivty.this);
            File file = new File(folderProvider.getSelectedFolder().imgs.get(position));
            Glide.with(ImagePickerActivty.this).load(file).into(imageView);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
