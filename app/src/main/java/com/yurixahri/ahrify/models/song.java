package com.yurixahri.ahrify.models;

import android.graphics.Bitmap;

public class song {
    public int drawable;
    public Bitmap cover;
    public String text;

    public song() {
    }

    public song(int drawable, Bitmap cover, String text) {
        this.drawable = drawable;
        this.cover = cover;
        this.text = text;
    }
}
