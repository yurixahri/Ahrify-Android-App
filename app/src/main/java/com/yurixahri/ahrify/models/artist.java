package com.yurixahri.ahrify.models;

public class artist {
    public int drawable;
    public String thumbnail;
    public String text;
    public final String url_thumbnail = "https://ahrify.yurixahri.net/~/files/thumbnails/";
    public boolean is_file;

    public artist(){}

    public artist(int drawable, String thumbnail, String text, boolean is_file) {
        this.drawable = drawable;
        this.thumbnail = url_thumbnail + thumbnail;
        this.is_file = is_file;
        this.text = text;
    }
}
