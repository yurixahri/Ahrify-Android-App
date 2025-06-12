package com.yurixahri.ahrify.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yurixahri.ahrify.models.playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    private SQLiteDatabase db;

    public PlaylistDAO(Context context) {
        DBHelper helper = new DBHelper(context);
        this.db = helper.getWritableDatabase();
    }

    public long insert(playlist playlist){
        ContentValues values = new ContentValues();
        values.put("title", playlist.title);
        return db.insert("playlist", null, values);
    }
    public long delete(int ID){
        return db.delete("playlist","id=?", new String[] {String.valueOf(ID)});
    }

    public List<playlist> get(String sql, String ... selectArgs){
        List<playlist> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql,selectArgs);
        while(cursor.moveToNext()){
            playlist playlist = new playlist();
            playlist.id = cursor.getInt(0);
            playlist.title = cursor.getString(1);
            list.add(playlist);
        }
        return list;
    }

    public List<playlist> getAll(){
        String sql = "SELECT * FROM playlist";
        return get(sql);
    }
}
