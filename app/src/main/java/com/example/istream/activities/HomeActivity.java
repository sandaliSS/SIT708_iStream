package com.example.istream.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.istream.R;
import com.example.istream.database.AppDatabase;
import com.example.istream.database.PlaylistItem;

public class HomeActivity extends AppCompatActivity {

    private EditText etUrl;
    private WebView webView;
    private AppDatabase db;
    private int userId;

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
        webView = findViewById(R.id.webView);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
        Button btnMyPlaylist = findViewById(R.id.btnMyPlaylist);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Setup WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        btnPlay.setOnClickListener(v -> playVideo(etUrl.getText().toString().trim()));

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



        btnLogout.setOnClickListener(v -> logout());

        // Check if launched from playlist
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
            Toast.makeText(this, "Invalid YouTube URL. Please use a valid youtube.com or youtu.be link.", Toast.LENGTH_LONG).show();
            return;
        }

        String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#000;'>" +
                "<iframe width='100%' height='100%' " +
                "src='https://www.youtube.com/embed/" + videoId + "?autoplay=1' " +
                "frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>" +
                "</body></html>";

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null);
    }

    private String extractVideoId(String url) {
        if (url == null || url.isEmpty()) return null;

        // youtu.be/VIDEO_ID
        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1].split("[?&]")[0];
                if (!id.isEmpty()) return id;
            }
        }

        // youtube.com/watch?v=VIDEO_ID
        if (url.contains("youtube.com/watch")) {
            if (url.contains("v=")) {
                String[] parts = url.split("v=");
                if (parts.length > 1) {
                    String id = parts[1].split("[?&]")[0];
                    if (!id.isEmpty()) return id;
                }
            }
        }

        // youtube.com/embed/VIDEO_ID
        if (url.contains("youtube.com/embed/")) {
            String[] parts = url.split("youtube.com/embed/");
            if (parts.length > 1) {
                String id = parts[1].split("[?&]")[0];
                if (!id.isEmpty()) return id;
            }
        }

        return null;
    }

    private boolean isValidYouTubeUrl(String url) {
        return extractVideoId(url) != null;
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("istream_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
