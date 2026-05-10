# ✨ Lyra Aura

**A modernized educational fork of Discord Rich Presence Android**

> Fork by [lyraEz](https://github.com/lyraEz)  
> Original by [JasonBenfrin](https://github.com/JasonBenfrin/Discord-Rich-Presence-Android)  
> Inspired by [Kizzy (Vaibhav)](https://www.youtube.com/channel/UCh-zsCv66gwHCIbMKLMJmaw)

---

## What is Lyra Aura?

Lyra Aura lets you set a custom **Discord Rich Presence** (activity status) from your Android phone, complete with:

- Custom activity name, details, state, images, buttons, party, timestamps
- Status control (Online / Idle / Do Not Disturb / Invisible)
- Save/load presets and quick templates
- A beautiful lavender dark liquid glass UI

> ⚠️ **Educational project.** Using user tokens may violate Discord's Terms of Service. Use at your own risk.

---

## Features

### All original features preserved
- Discord Gateway WebSocket (v10) with op codes 0-11
- Heartbeat, resume, reconnect
- All Rich Presence fields (name, type, details, state, timestamps, assets, buttons, party, secrets, emoji, flags, instance)
- Status customization
- Login via WebView

### 17 new features added
| # | Feature | Description |
|---|---------|-------------|
| 1 | **Live Presence Preview** | Discord-style card showing your activity live |
| 2 | **8 Quick Templates** | Gaming, Coding, Music, Watching, Studying, AFK, Invisible, Streaming |
| 3 | **Presence History** | Last 20 used presences with one-tap restore |
| 4 | **Named Presets** | Save unlimited custom presets |
| 5 | **Timestamp Presets** | "Now", "30m ago", "1h ago" buttons |
| 6 | **Random Join Secret** | Generate a random party join secret |
| 7 | **Connection Latency** | Real-time heartbeat latency display |
| 8 | **Custom Notification** | Edit foreground service notification title/body |
| 9 | **Scheduled Disconnect** | Auto-disconnect timer (5m–8h) |
| 10 | **Auto-clear Presence** | Clear activity when disconnecting |
| 11 | **Vibration Feedback** | Haptic on connect/disconnect |
| 12 | **Developer Mode** | Raw JSON payload preview |
| 13 | **Character Counter** | Real-time char count on all text fields |
| 14 | **Multi-Theme** | Lavender Dark, AMOLED Black, Light, System |
| 15 | **6 Activity Types** | Playing / Streaming / Listening / Watching / Custom / Competing |
| 16 | **Advanced Fields UI** | Party, secrets, instance, flags — all in one place |
| 17 | **Gateway Log Viewer** | Color-coded WebSocket activity log |

---

## Stack

| Component | Library | Version |
|-----------|---------|---------|
| Language | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| DI | Hilt | 2.54 |
| Async | Kotlin Coroutines + Flow | 1.9.0 |
| Network | OkHttp 4 (WebSocket) | 4.12.0 |
| Storage | DataStore Preferences | 1.1.2 |
| Serialization | kotlinx.serialization | 1.7.3 |
| Image loading | Coil | 2.7.0 |
| Navigation | Navigation Compose | 2.8.5 |
| Build | AGP | 8.7.2 |
| Min SDK | Android 8.0 | API 26 |
| Target SDK | Android 15 | API 35 |

---

## Getting Started

### Requirements
- **Android Studio Hedgehog** or newer (2023.1.1+)
- **JDK 17** (bundled with Android Studio)
- Internet connection (for Gradle dependency download)

### Setup
```bash
# 1. Clone or unzip the project
git clone https://github.com/lyraEz/lyra-aura.git
# or unzip lyra-aura.zip

# 2. Open in Android Studio
File → Open → select the lyra-aura/ folder

# 3. Wait for Gradle sync to complete

# 4. Connect a device or start an emulator (API 26+)

# 5. Run → Run 'app'
```

### First run
1. The app will show a **Terms of Service warning** — read it carefully
2. Tap **Login** and sign into your Discord account in the WebView
3. Go back to **Home** and tap **Connect to Discord**
4. Configure your Rich Presence in the **Presence** tab

---

## Project Structure

```
app/src/main/java/com/lyra/aura/
├── LyraAuraApp.kt              Application + Hilt entry point
├── MainActivity.kt             Single activity, nav host, theme
├── di/
│   └── AppModule.kt            Hilt DI module
├── model/
│   └── Models.kt               All data models, sealed classes, enums
├── data/
│   ├── PreferencesDataStore.kt DataStore for settings + user
│   └── PresetsRepository.kt    File-based preset + history storage
├── service/
│   ├── DiscordGateway.kt       WebSocket Gateway (OkHttp 4 + Coroutines)
│   └── PresenceService.kt      Foreground service
├── viewmodel/
│   ├── MainViewModel.kt        Connection, auth, presets, settings
│   └── PresenceViewModel.kt    All RPC fields + push logic
└── ui/
    ├── theme/
    │   ├── Color.kt            Full lavender dark palette
    │   ├── Theme.kt            Multi-theme system + LyraColors
    │   └── Type.kt             Typography scale
    ├── components/
    │   ├── GlassComponents.kt  GlassCard, LyraTextField, Chips, Banners
    │   └── PresencePreviewCard.kt Discord-style activity preview
    ├── navigation/
    │   └── NavGraph.kt         Navigation compose graph + routes
    └── screen/
        ├── HomeScreen.kt       Dashboard + connect
        ├── ConfigureScreen.kt  Status + Rich Presence + Advanced tabs
        ├── PresetsScreen.kt    Templates + saved presets + history
        ├── SettingsScreen.kt   All settings
        ├── LoginScreen.kt      WebView login flow
        ├── HistoryScreen.kt    Presence history
        ├── LogScreen.kt        Gateway log viewer
        └── AboutScreen.kt      Credits + feature list
```

---

## Theme: Lavender Dark Liquid Glass

The UI is built around a **liquid glass** aesthetic inspired by iOS 26:

- **Background**: `#0B0718` deep dark purple
- **Glass cards**: semi-transparent lavender with gradient borders and top shimmer
- **Primary**: `#A78BFA` (lavender 400) — saturated, vivid
- **All corners rounded** at 18–28dp
- **Bottom Navigation** with subtle tint
- Status colors matching Discord's palette (green/yellow/red/gray)

---

## Architecture

- **Single Activity** + Navigation Compose
- **MVVM**: ViewModel → StateFlow → Composable
- **Hilt** for dependency injection (singleton Gateway, DataStore, Repository)
- **DataStore** for settings and user cache
- **File-based JSON** for presets/history (no Room DB needed)
- **SharedFlow** for Gateway events (reconnect, latency, ready)

---

## Credits

```
Lyra Aura
  Fork author   → lyraEz (https://github.com/lyraEz)
  Original app  → JasonBenfrin (https://github.com/JasonBenfrin)
  Inspiration   → Kizzy by Vaibhav
```

---

## License

This project is for **educational purposes only**.  
The original project was released without a license; this fork follows the same spirit.  
Discord's trademarks, API, and ToS belong to Discord Inc.

---

> Made with 💜 by lyraEz
