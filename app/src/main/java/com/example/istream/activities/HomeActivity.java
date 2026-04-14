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

        etUrl           = findViewById(R.id.etUrl);
        webView         = findViewById(R.id.webView);
        Button btnPlay          = findViewById(R.id.btnPlay);
        Button btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
        Button btnMyPlaylist    = findViewById(R.id.btnMyPlaylist);
        Button btnLogout        = findViewById(R.id.btnLogout);

        // Setup WebView properly
        setupWebView();

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
            // Fix issue 5 — DB off main thread
            new Thread(() -> {
                db.playlistDao().insert(new PlaylistItem(userId, url));
                runOnUiThread(() ->
                        Toast.makeText(this, "Added to playlist!", Toast.LENGTH_SHORT).show()
                );
            }).start();
        });

        btnMyPlaylist.setOnClickListener(v ->
                startActivity(new Intent(this, PlaylistActivity.class))
        );

        btnLogout.setOnClickListener(v -> logout());

        // Check if launched from playlist
        String playUrl = getIntent().getStringExtra("play_url");
        if (playUrl != null && !playUrl.isEmpty()) {
            etUrl.setText(playUrl);
            playVideo(playUrl);
        }
    }

    // Extracted into its own method
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
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

        String html =
                "<!DOCTYPE html><html>" +
                        "<head>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                        "<style>" +
                        "  * { margin:0; padding:0; }" +
                        "  body { background:#000; }" +
                        "  #player { width:100%; height:100%; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div id='player'></div>" +
                        "<script src='https://www.youtube.com/iframe_api'></script>" +
                        "<script>" +
                        "  function onYouTubeIframeAPIReady() {" +
                        "    new YT.Player('player', {" +
                        "      width: '100%'," +
                        "      height: '100%'," +
                        "      videoId: '" + videoId + "'," +
                        "      playerVars: { playsinline: 1, controls: 1, rel: 0 }" +
                        "    });" +
                        "  }" +
                        "</script>" +
                        "</body></html>";

        webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                html,
                "text/html",
                "utf-8",
                null
        );
    }

    // Fix issue 2 — proper video ID extraction
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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Prevent audio/video leak when activity is destroyed
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }
}