package com.example.apple.glidetest.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.apple.glidetest.R;

import java.io.File;

/**
 * Created by Apple on 17/5/27.
 */

public class GlideUtils {

    public static void loadImage(File file, Context activity, ImageView imageView) {
        Glide.with(activity).load(file).error(R.drawable.default_image)
                .placeholder(R.drawable.default_image).centerCrop().into(imageView);
    }
}
