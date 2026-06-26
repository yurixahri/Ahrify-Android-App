package com.yurixahri.ahrify.models;

import android.graphics.Bitmap;

public class song {
    public int drawable;
    public String thumbnail;
    public String text;

    public final String url_thumbnail = "https://ahrify.yurixahri.net/~/files/thumbnails/";

    public song() {
    }

    public song(int drawable, String thumbnail, String text) {
        this.drawable = drawable;
        this.thumbnail = url_thumbnail + thumbnail;
        this.text = text;
    }
}
