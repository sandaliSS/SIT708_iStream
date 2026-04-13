package com.example.istream.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    void insert(PlaylistItem item);

    @Query("SELECT * FROM playlist WHERE userId = :userId")
    List<PlaylistItem> getPlaylistForUser(int userId);

    @Query("DELETE FROM playlist WHERE id = :id")
    void delete(int id);
}
