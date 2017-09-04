package com.example.apple.glidetest.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageState;

/**
 * Created by Apple on 17/6/5.
 */

public class FileUtils {
    /**
     * /storage/emulated/0/DCIM/Camera
     * /storage/emulated/0/DCIM
     * /storage/emulated/0/DCIM/Screenshots
     */
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String WRITE_EXTERNAL_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static File createTmpFile(Context context) throws IOException {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        String JPEG_FILE_PREFIX = "IMG_" + date+"_";
        File dir;
        if (TextUtils.equals(getExternalStorageState(), MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM + "/Camera");
            if (!dir.exists()) {
                dir = getCacheDirectory(context, true);
            }
        } else {
            dir = getCacheDirectory(context, true);
        }
//        ENAMETOOLONG (File name too long)
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir);
    }

    private static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) {
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) {
            externalStorageState = "";
        }
        if (preferExternal && TextUtils.equals(externalStorageState, MEDIA_MOUNTED) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = context.getFilesDir().getPath() + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appCacheDir;

    }

    private static boolean hasExternalStoragePermission(Context context) {
        return PERMISSION_GRANTED == context.checkCallingOrSelfPermission(WRITE_EXTERNAL_PERMISSION);
    }
}
