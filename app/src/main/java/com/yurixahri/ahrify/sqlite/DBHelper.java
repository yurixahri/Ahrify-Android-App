package com.yurixahri.ahrify.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_Name = "Ahrify";
    private static final int DB_Version = 3;
    public DBHelper(@Nullable Context context) {
        super(context, DB_Name, null, DB_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String playlist_table  = "CREATE TABLE playlist(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " title text not null)";

        String playlist_song_table = "CREATE TABLE playlist_song("+
                "file text not null,"+
                "folder text not null,"+
                "title text not null,"+
                "cover blob,"+
                "playlist_id INTEGER not null,"+
                "PRIMARY KEY (file, folder, playlist_id),"+
                "FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE)";

        db.execSQL(playlist_table);
        db.execSQL(playlist_song_table);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String playlist_table = "DROP TABLE IF EXISTS playlist";
        String playlist_song_table = "DROP TABLE IF EXISTS playlist_song";
        db.execSQL(playlist_song_table);
        db.execSQL(playlist_table);
        onCreate(db);
    }
}
