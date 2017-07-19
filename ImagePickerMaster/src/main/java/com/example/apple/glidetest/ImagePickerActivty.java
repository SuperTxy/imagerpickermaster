package com.example.apple.glidetest;

import android.Manifest;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apple.glidetest.adapter.ImageAllAdapter;
import com.example.apple.glidetest.adapter.ImageSelectedAdapter;
import com.example.apple.glidetest.bean.Change;
import com.example.apple.glidetest.bean.Folder;
import com.example.apple.glidetest.bean.FolderProvider;
import com.example.apple.glidetest.bean.SelectImageProvider;
import com.example.apple.glidetest.utils.FileUtils;
import com.example.apple.glidetest.utils.OsUtils;
import com.example.apple.glidetest.utils.PickerSettings;
import com.example.apple.glidetest.view.SpaceItemDecoration;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class ImagePickerActivty extends FragmentActivity implements Observer {

    RecyclerView recyclerImageAll;
    RecyclerView recyclerImagePicked;
    TextView tvShowFolder;
    TextView tvPickImage;
    Button btnPickOk;
    LinearLayout pickerAll;

    private static final String FILE_PROVIDER = "com.example.apple.glidetest.fileprovider";
    private final int PERMISSION_READ_STORAGE_CODE = 001;
    private final int PERMISSION_CAREMA_CODE = 002;
    private final int CAREMA_REQUEST_CODE = 003;
    private final int HORIZONTAL_COUNT = 3;  //  Recycler每行显示的图片个数

    private FolderProvider folderProvider;
    private SelectImageProvider imageProvider;

    private ImageAllAdapter imageAllAdapter;
    private ImageSelectedAdapter imageSelectedAdapter;

    private FolderPopup folderPopup;  //查看大图的popup
    private File tmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker_activty);
        int maxSelect = getIntent().getIntExtra(PickerSettings.MAX_SELECT, -1);
        initView();
        imageProvider = SelectImageProvider.getInstance();
        imageProvider.setMaxSelect(maxSelect);
        folderProvider = FolderProvider.getInstance();
        imageProvider.clear();
        imageProvider.addObserver(this);
        initRecycler();
        checkReadStoragePermission();
    }

    private void initView() {
        recyclerImageAll = (RecyclerView) findViewById(R.id.recycler_image_all);
        recyclerImagePicked = (RecyclerView) findViewById(R.id.recycler_image_picked);
        tvShowFolder = (TextView) findViewById(R.id.tv_show_folder);
        tvPickImage = (TextView) findViewById(R.id.tv_pick_hint);
        btnPickOk = (Button) findViewById(R.id.btn_pick_ok);
        pickerAll = (LinearLayout) findViewById(R.id.picker_all);
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
        imageSelectedAdapter = new ImageSelectedAdapter(this,imageProvider.getSelectedImgs());
        recyclerImagePicked.setAdapter(imageSelectedAdapter);
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
        tvShowFolder.setText(selectedFolder.name);
        folderPopup = new FolderPopup(this, new FolderPopup.OnFolderSelectListener() {
            @Override
            public void onFolderSelected() {
                tvShowFolder.setText(folderProvider.getSelectedFolder().name);
                imageAllAdapter.refresh(folderProvider.getSelectedFolder().imgs);
            }

            @Override
            public void onDismiss() {
                tvShowFolder.setSelected(false);
            }
        });
    }

    private ImageAllAdapter.OnImageAllListener onImageAllListener = new ImageAllAdapter.OnImageAllListener() {
        @Override
        public void onItemClick(int pos, boolean checked) {
            new PictureShowPopup(ImagePickerActivty.this).setDataAndPosition(folderProvider.getSelectedFolder().imgs,pos).show(recyclerImageAll);
        }

        @Override
        public void onCameraClick() {
            if(imageProvider.isSelectedMax())
                Toast.makeText(ImagePickerActivty.this, "你已选择" + SelectImageProvider.getInstance().maxSelect + "张图片", Toast.LENGTH_SHORT).show();
            else requestCameraPermissions();
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
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
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
            if (resultCode == RESULT_OK) {
                if (tmpFile != null) {
//                    notify system
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)));
                    imageProvider.add(tmpFile.getAbsolutePath());
                    imageSelectedAdapter.notifyItemInserted(imageProvider.getCount());
                }
            } else {
//               if user click cancel ,delete the temp file
                if (tmpFile != null && tmpFile.exists()) {
                    boolean success = tmpFile.delete();
                    if (success) {
                        tmpFile = null;
                    }
                }
            }
        }
    }

    public void close(View view) {
        finish();
    }

    @Override
    public void update(Observable o, Object arg) {
        btnPickOk.setEnabled(imageProvider.getCount() > 0);
        btnPickOk.setText("确认\n" + imageProvider.getSelectedImgs().size() + "/" + imageProvider.maxSelect);
        boolean imageEmpty = imageProvider.getSelectedImgs().size() == 0;
        tvPickImage.setVisibility(imageEmpty ? View.VISIBLE : View.GONE);
        recyclerImagePicked.setVisibility(imageEmpty ? View.GONE : View.VISIBLE);
        if (arg instanceof Change) {
            Change change = (Change) arg;
            if (change.isAdd()) {
                recyclerImagePicked.scrollToPosition(imageProvider.getLastIndex());
            }
        }
    }
}
