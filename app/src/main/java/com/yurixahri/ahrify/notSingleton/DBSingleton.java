package com.yurixahri.ahrify.notSingleton;

import android.content.Context;

import com.yurixahri.ahrify.R;
import com.yurixahri.ahrify.adapters.PlaylistListView;
import com.yurixahri.ahrify.adapters.PlaylistSongListView;
import com.yurixahri.ahrify.models.playlist;
import com.yurixahri.ahrify.models.playlistSong;

import java.util.ArrayList;
import java.util.List;

public class DBSingleton {
    private static DBSingleton instance;
    private Context context;
    public List<playlist> playlists = new ArrayList<>();
    public List<playlistSong> playlistSongs = new ArrayList<>();
    public PlaylistListView adapter;
    public PlaylistListView adapter_add_song;
    public PlaylistSongListView adapter_playlist_song;

    private DBSingleton() {}

    public static DBSingleton getInstance() {
        if (instance == null) {
            synchronized (DBSingleton.class) {
                if (instance == null) {
                    instance = new DBSingleton();
                }
            }
        }
        return instance;
    }

    // Set context and initialize adapter
    public void init(Context context) {
        this.context = context.getApplicationContext(); // Safe long-lived context
        adapter = new PlaylistListView(this.context, R.layout.default_listview_item, playlists);
        adapter_add_song = new PlaylistListView(this.context, R.layout.default_listview_item, playlists);
        adapter_playlist_song = new PlaylistSongListView(this.context, R.layout.default_listview_item, playlistSongs);
    }

    public void updatePlaylists(List<playlist> newPlaylists) {
        playlists.clear();
        playlists.addAll(newPlaylists);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter_add_song.notifyDataSetChanged();
        }
    }

    public void updatePlaylistSongs(List<playlistSong> newPlaylistSongs) {
        playlistSongs.clear();
        playlistSongs.addAll(newPlaylistSongs);
        if (adapter_playlist_song != null) {
            adapter_playlist_song.notifyDataSetChanged();
        }
    }
}
