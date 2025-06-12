package com.yurixahri.ahrify.models;

public class playlistSong {
    public String file;
    public String folder;
    public String title;
    public byte[] cover;
    public int playlist_id;

    public playlistSong() {
    }

    public playlistSong(String file, String folder, String title, byte[] cover, int playlist_id) {
        this.file = file;
        this.folder = folder;
        this.title = title;
        this.cover = cover;
        this.playlist_id = playlist_id;
    }
}
