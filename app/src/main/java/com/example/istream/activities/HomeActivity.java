package com.example.istream.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istream.R;
import com.example.istream.database.AppDatabase;
import com.example.istream.database.PlaylistItem;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class HomeActivity extends AppCompatActivity {

    private EditText etUrl;
    private YouTubePlayerView youtubePlayerView;
    private AppDatabase db;
    private int userId;
    private String pendingVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("istream_prefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        String username = prefs.getString("username", "User");

        setTitle("iStream - " + username);

        etUrl = findViewById(R.id.etUrl);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
        Button btnMyPlaylist = findViewById(R.id.btnMyPlaylist);
        Button btnLogout = findViewById(R.id.btnLogout);

        getLifecycle().addObserver(youtubePlayerView);

        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                if (pendingVideoId != null) {
                    youTubePlayer.loadVideo(pendingVideoId, 0);
                }
            }
        });

        btnPlay.setOnClickListener(v ->
                playVideo(etUrl.getText().toString().trim())
        );

        btnAddToPlaylist.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidYouTubeUrl(url)) {
                Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
                return;
            }

            db.playlistDao().insert(new PlaylistItem(userId, url));
            Toast.makeText(this, "Added to playlist!", Toast.LENGTH_SHORT).show();

        });

        btnMyPlaylist.setOnClickListener(v ->
                startActivity(new Intent(this, PlaylistActivity.class))
        );

        btnLogout.setOnClickListener(v -> logout());

        String playUrl = getIntent().getStringExtra("play_url");
        if (playUrl != null && !playUrl.isEmpty()) {
            etUrl.setText(playUrl);
            playVideo(playUrl);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String playUrl = intent.getStringExtra("play_url");
        if (playUrl != null && !playUrl.isEmpty()) {
            etUrl.setText(playUrl);
            playVideo(playUrl);
        }
    }

    private void playVideo(String url) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a YouTube URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String videoId = extractVideoId(url);
        if (videoId == null) {
            Toast.makeText(this, "Invalid YouTube URL.", Toast.LENGTH_LONG).show();
            return;
        }

        pendingVideoId = videoId;

        youtubePlayerView.getYouTubePlayerWhenReady(youTubePlayer ->
                youTubePlayer.loadVideo(videoId, 0)
        );
    }

    private String extractVideoId(String url) {
        if (url == null || url.isEmpty()) return null;

        if (url.contains("youtube.com/watch?v=")) {
            String id = url.split("v=")[1];
            int ampIndex = id.indexOf("&");
            return ampIndex != -1 ? id.substring(0, ampIndex) : id;
        }

        if (url.contains("youtu.be/")) {
            String id = url.split("youtu.be/")[1];
            int qIndex = id.indexOf("?");
            return qIndex != -1 ? id.substring(0, qIndex) : id;
        }

        if (url.contains("youtube.com/embed/")) {
            String id = url.split("embed/")[1];
            int qIndex = id.indexOf("?");
            return qIndex != -1 ? id.substring(0, qIndex) : id;
        }

        return null;
    }

    private boolean isValidYouTubeUrl(String url) {
        if (url == null || url.isEmpty()) return false;

        return url.contains("youtube.com/watch?v=") ||
                url.contains("youtu.be/") ||
                url.contains("youtube.com/embed/");
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("istream_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}