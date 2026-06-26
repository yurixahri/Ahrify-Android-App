package com.yurixahri.ahrify.models;

public class playlistSong {
    public String file;
    public String folder;
    public String title;
    public String artist;
    public String album;
    public  String thumbnail;
    public  String cover;
    public final String url_thumbnail = "https://ahrify.yurixahri.net/~/files/thumbnails/";
    public final String url_cover = "https://ahrify.yurixahri.net/~/files/covers/";
    public int playlist_id;

    public playlistSong() {
    }

    public playlistSong(String file, String folder, String title, String artist, String album, String thumbnail, String cover, int playlist_id) {
        this.file = file;
        this.folder = folder;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.thumbnail = thumbnail;
        this.cover = cover;
        this.playlist_id = playlist_id;
    }
}
