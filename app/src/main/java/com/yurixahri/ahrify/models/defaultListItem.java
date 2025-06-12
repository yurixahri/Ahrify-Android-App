package com.yurixahri.ahrify.models;

import android.graphics.Bitmap;

public class defaultListItem {
    public int drawable;
    public Bitmap cover;
    public String text;

    public defaultListItem() {
    }

    public defaultListItem(String text, int drawable, Bitmap cover) {
        this.text = text;
        this.drawable = drawable;
        this.cover = cover;
    }
}
