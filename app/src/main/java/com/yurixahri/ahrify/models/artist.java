package com.yurixahri.ahrify.models;

public class artist {
    public int drawable;
    public String text;
    public boolean is_file;

    public artist(){}

    public artist(int drawable, String text, boolean is_file) {
        this.drawable = drawable;
        this.is_file = is_file;
        this.text = text;
    }
}
