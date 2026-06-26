package com.yurixahri.ahrify.models;

import android.graphics.Bitmap;

public class album {
    public int drawable;
    public String thumbnail;
    public String name;
    public final String url_thumbnail = "https://ahrify.yurixahri.net/~/files/thumbnails/";

    public boolean is_file;
    public album(){
    }
    public album(int drawable, String thumbnail, String name, boolean is_file) {
        this.drawable = drawable;
        this.thumbnail = url_thumbnail + thumbnail;
        this.is_file = is_file;
        this.name = name;
    }
}
