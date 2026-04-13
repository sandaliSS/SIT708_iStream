package com.example.istream.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist")
public class PlaylistItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String url;

    public PlaylistItem(int userId, String url) {
        this.userId = userId;
        this.url = url;
    }
}
