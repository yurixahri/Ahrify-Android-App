package com.yurixahri.ahrify.models;

public class defaultListItem {
    public int drawable;
    public String thumbnail;
    public String text;
    public final String url_thumbnail = "https://ahrify.yurixahri.net/~/files/thumbnails/";
    public int index;
    public defaultListItem() {
    }

    public defaultListItem(String text, int drawable, String thumbnail) {
        this.text = text;
        this.drawable = drawable;
        this.thumbnail = thumbnail.startsWith("https") ||  thumbnail.startsWith("http") ? thumbnail : url_thumbnail + thumbnail;
    }

    public defaultListItem(String text, int drawable, String thumbnail,  int index) {
        this.drawable = drawable;
        this.thumbnail = thumbnail.startsWith("https") ||  thumbnail.startsWith("http") ? thumbnail : url_thumbnail + thumbnail;
        this.text = text;
        this.index = index;
    }
}
