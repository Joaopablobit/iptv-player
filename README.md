# IPTV Player - Android TV App

A fully-featured IPTV player for Android TV with M3U playlist support, built with Kotlin, ExoPlayer (Media3), and the Leanback library.

---

## 📺 Features

- **M3U Playlist support** — load any M3U/M3U8 URL (HTTP/HTTPS)
- **HLS & DASH streaming** via ExoPlayer Media3
- **Leanback TV UI** — optimized for D-pad remote navigation
- **Channel grouping** by `group-title` attribute
- **Favorites** — long-press or button to star channels
- **Recently Watched** — quick access to last 20 channels
- **Search** — filter channels by name or group
- **Dark TV theme** — professional dark blue palette
- **Offline cache** — last loaded playlist cached locally

---

## 🛠 Build Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog 2023.1+ |
| JDK | 17+ |
| Android SDK | API 34 |
| Gradle | 8.2 |
| Kotlin | 1.9.0 |

---

## 🚀 How to Build

### Option A — Android Studio (Recommended)

1. Open Android Studio
2. `File → Open` → select the `IPTVPlayer` folder
3. Wait for Gradle sync to complete
4. Connect your Android TV device or start an Android TV emulator
5. Click **Run ▶** or use:
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
6. The APK will be in:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Option B — Command Line

```bash
cd IPTVPlayer

# On macOS/Linux
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Option C — Release APK (signed)

```bash
# Generate keystore (one time)
keytool -genkey -v -keystore release.keystore \
  -alias iptvplayer -keyalg RSA -keysize 2048 -validity 10000

# Build release
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=release.keystore \
  -Pandroid.injected.signing.store.password=YOUR_PASSWORD \
  -Pandroid.injected.signing.key.alias=iptvplayer \
  -Pandroid.injected.signing.key.password=YOUR_PASSWORD
```

---

## 📱 Install on Android TV

```bash
# Enable ADB on your Android TV (Settings → Device Preferences → About → Build (7 times) → ADB Debugging)

# Install via ADB
adb connect YOUR_TV_IP:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Project Structure

```
IPTVPlayer/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/iptvplayer/app/
│   │   ├── data/
│   │   │   ├── model/Channel.kt          # Data models
│   │   │   ├── parser/M3UParser.kt       # M3U parser
│   │   │   └── repository/ChannelRepository.kt
│   │   ├── ui/
│   │   │   ├── main/
│   │   │   │   ├── MainActivity.kt       # Entry point
│   │   │   │   ├── MainFragment.kt       # Leanback browse UI
│   │   │   │   ├── MainViewModel.kt      # State management
│   │   │   │   ├── AddPlaylistDialog.kt  # Add M3U dialog
│   │   │   │   └── SettingsActivity.kt
│   │   │   ├── player/
│   │   │   │   └── PlayerActivity.kt     # ExoPlayer fullscreen
│   │   │   └── channels/
│   │   │       ├── ChannelListActivity.kt # Searchable grid
│   │   │       ├── ChannelGridAdapter.kt
│   │   │       └── ChannelCardPresenter.kt
│   │   └── utils/Constants.kt
│   └── res/                             # Layouts, themes, icons
└── app/build.gradle                     # Dependencies
```

---

## 🎮 Using the App

1. Launch the app on Android TV
2. The **Add M3U Playlist** dialog will appear on first launch
3. Enter your M3U URL (e.g., `http://yourprovider.com/playlist.m3u`)
4. Channels load automatically grouped by category
5. Navigate with D-pad, press **Select/OK** to play

### Remote Controls
| Button | Action |
|--------|--------|
| D-pad | Navigate channels |
| OK/Select | Play channel |
| Back | Return / hide controls |
| Play/Pause | Toggle playback |
| Search | Open search |

---

## ⚙️ M3U Format Support

```
#EXTM3U
#EXTINF:-1 tvg-id="cnn" tvg-name="CNN" tvg-logo="http://..." group-title="News",CNN
http://stream.example.com/cnn

#EXTINF:-1 group-title="Sports",ESPN
http://stream.example.com/espn
```

Supported attributes: `tvg-id`, `tvg-name`, `tvg-logo`, `group-title`, `tvg-language`, `tvg-country`

---

## 🔒 Legal Notice

This app does not include any channels or streams. Users are responsible for ensuring they have legal rights to access any IPTV content they stream. For use only with content you own or have permission to access.
