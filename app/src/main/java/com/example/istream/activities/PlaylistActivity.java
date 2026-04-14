package com.example.istream.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istream.R;
import com.example.istream.database.AppDatabase;
import com.example.istream.database.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    private ListView listView;
    private AppDatabase db;
    private int userId;
    private List<PlaylistItem> playlistItems;
    private ArrayAdapter<String> adapter;
    private List<String> urlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        setTitle("My Playlist");

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("istream_prefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        listView = findViewById(R.id.listView);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnBack = findViewById(R.id.btnBack);

        urlList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.item_playlist, R.id.tvUrl, urlList);
        listView.setAdapter(adapter);

        loadPlaylist();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String url = urlList.get(position);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("play_url", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadPlaylist() {
        playlistItems = db.playlistDao().getPlaylistForUser(userId);
        urlList.clear();
        for (PlaylistItem item : playlistItems) {
            urlList.add(item.url);
        }
        adapter.notifyDataSetChanged();

        if (urlList.isEmpty()) {
            Toast.makeText(this, "Your playlist is empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylist();
    }
}
