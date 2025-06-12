package com.yurixahri.ahrify.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yurixahri.ahrify.models.playlistSong;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongDAO {
    private SQLiteDatabase db;

    public PlaylistSongDAO(Context context) {
        DBHelper helper = new DBHelper(context);
        this.db = helper.getWritableDatabase();
    }

    public long insert(playlistSong playlistSong){
        ContentValues values = new ContentValues();

        values.put("file", playlistSong.file);
        values.put("folder", playlistSong.folder);
        values.put("title", playlistSong.title);
        values.put("cover", playlistSong.cover);
        values.put("playlist_id", playlistSong.playlist_id);
        return db.insert("playlist_song", null, values);
    }
    public long delete(playlistSong song){
        return db.delete("playlist_song","file=? AND folder = ? and playlist_id = ?", new String[] {song.file, song.folder, String.valueOf(song.playlist_id)});
    }

    public List<playlistSong> get(String sql, String ... selectArgs){
        List<playlistSong> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql,selectArgs);
        while(cursor.moveToNext()){
            playlistSong playlistSong = new playlistSong();
            playlistSong.file = cursor.getString(0);
            playlistSong.folder = cursor.getString(1);
            playlistSong.title = cursor.getString(2);
            playlistSong.cover = cursor.getBlob(3);
            playlistSong.playlist_id = cursor.getInt(4);
            list.add(playlistSong);
        }
        return list;
    }

    public List<playlistSong> getAll(int id){
        String sql = "SELECT * FROM playlist_song WHERE playlist_id = ?";
        return get(sql, new String[]{String.valueOf(id)});
    }
}
