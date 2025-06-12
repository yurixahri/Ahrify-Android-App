package com.yurixahri.ahrify.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GlideHelper {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Future<Bitmap> loadBitmapSync(Context context, String url) {
        return executor.submit((Callable<Bitmap>) () -> Glide.with(context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .submit()
                .get());
    }
}
