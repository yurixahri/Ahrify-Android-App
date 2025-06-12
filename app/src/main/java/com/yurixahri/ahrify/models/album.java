package com.yurixahri.ahrify.models;

import android.graphics.Bitmap;

public class album {
    public int drawable;
    public Bitmap cover;
    public String text;

    public boolean is_file;

    public album(){
    }

    public album(int drawable, Bitmap cover, String text, boolean is_file) {
        this.drawable = drawable;
        this.cover = cover;
        this.is_file = is_file;
        this.text = text;
    }
}
