# iStream

A simple Android video streaming app built with Java.

---

## Features
- User registration and login with local Room database
- Each user has a separate playlist
- Play YouTube videos using the YouTube IFrame Player API
- Save YouTube URLs to your personal playlist
- Tap any playlist item to load it in the player
- Logout support on Home and Playlist screens
- Basic URL validation with error messages

---

## Project Structure

```
app/src/main/
│
├── java/com/example/istream/
│   ├── activities/
│   │   ├── LoginActivity.java
│   │   ├── SignUpActivity.java
│   │   ├── HomeActivity.java
│   │   └── PlaylistActivity.java
│   │
│   └── database/
│       ├── AppDatabase.java
│       ├── User.java
│       ├── UserDao.java
│       ├── PlaylistItem.java
│       └── PlaylistDao.java
│
└── res/
    ├── layout/
    │   ├── activity_login.xml
    │   ├── activity_sign_up.xml
    │   ├── activity_home.xml
    │   ├── activity_playlist.xml
    │   └── item_playlist.xml
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── styles.xml
```

---

## Dependencies
- AndroidX AppCompat
- Material Components
- Room Database (local storage)
- Android WebView (YouTube IFrame Player API)
