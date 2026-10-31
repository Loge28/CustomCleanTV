# LLM Onboarding Guide: CustomCleanTV

This document is designed for AI models (Claude, ChatGPT, Gemini, Copilot) and agentic coding tools to instantly understand the architecture, design patterns, customizations, build toolchain, and operational constraints of **CustomCleanTV**.

---

## 1. Project Overview

- **Application ID**: `com.gathin.tvhome`
- **Platform**: Android TV (API 28+ / Android 9.0 Pie and higher)
- **Primary Target Hardware**: Mi TV / Xiaomi Android TV (1280x720 physical framebuffer, 1080p output scale, 213 dpi)
- **Language & Stack**: Java (Android SDK), Gradle 8+, Leanback Support Libraries

CustomCleanTV is an ultra-minimalist, performance-oriented Android TV launcher tailored for speed, custom visual aesthetics, and distraction-free viewing.

---

## 2. Key Architecture & Customizations

### A. Core Activity & View Hierarchy
- **`MainActivity.java`**: Central hub managing the app lifecycle, D-Pad navigation, remote intent dispatch, wallpaper state, and popup menus.
- **`activity_main.xml`**: Root `FrameLayout` layering:
  1. `SkyView` (ambient time-of-day sky tint)
  2. `WallpaperPlayer` (theme wallpaper engine)
  3. `customWallpaper` (`ImageView` for custom image/GIF wallpapers)
  4. `videoWallpaper` (`TextureView` for hardware-accelerated 15s video loops)
  5. `itachiEyes` (`ItachiEyesView` pupil-tracking canvas)
  6. `rainView` (`HiddenRainView` procedural rain animation)
  7. `grid` (`RecyclerView` containing app tiles powered by `AppAdapter.java`)
  8. `clockBlock` (`RollingClockView` dynamic rolling second digits)

### B. Dark & White UI Theme Engine
- Controlled via `darkUiTheme` boolean persisted in `SharedPreferences` (`"dark_ui_theme"`).
- **Clock**: `RollingClockView.setDarkTheme(boolean dark)` updates primary text paint (`#1C1D1F` in dark, `#FFFFFF` in white) and drops contrasting shadow layers.
- **Top Bar Pills**: Dynamically selects between `pill_bg.xml` / `pill_bg_dark.xml` and tints text via `pill_text_dark.xml`.
- **Vector Logos**: `WhiteLogoDrawable.java` intercepts brand logos (YouTube, Netflix, Prime, Spotify, Hotstar, Zee5, etc.) and recolors them to deep charcoal `#1C1D1F` in Dark Mode with a subtle light drop shadow for high contrast on bright wallpapers (e.g., Royal Enfield image).
- **Tile Labels**: `AppAdapter.java` tints `h.name` and `h.label` accordingly.

### C. System Wallpaper & Leanback Overlay Integration
- **Problem Solved**: Android TV system Settings (`com.android.tv.settings.MainSettings`) and App Info (`com.android.tv.settings.device.apps.AppManagementActivity`) declare `windowShowWallpaper = true`. When active, WindowManager hides the launcher surface and exposes `com.android.systemui.ImageWallpaper`.
- **Solution (`syncWallpaperSnapshot()`)**:
  - Uses `PixelCopy.request(window, ...)` to capture the exact rendered home screen (wallpaper + clock + tiles).
  - Explicitly invokes `wm.suggestDesiredDimensions(w, h)` to prevent `ImageWallpaper` from miscalculating aspect ratio or creating letterbox bars.
  - Updates `WallpaperManager.getInstance(context).setBitmap(snapshot)`.
  - Debounced with `isSyncingWallpaper` to eliminate race conditions and avoid hardware bitmap crashes on software canvases.

### D. Custom Media Engine
- **Custom Image Wallpaper**: Loaded using `BitmapFactory` with `Bitmap.Config.ARGB_8888` and hardware-bitmap copying guards to ensure software-canvas compatibility. Supports **Fill Screen** (`CENTER_CROP`) vs **Fit to Screen** (`FIT_CENTER`).
- **Custom Video Wallpaper**: Plays via `MediaPlayer` onto `TextureView` surface with 15-second looping timer (`videoLoopCheck`) and audio toggle (`video_audio_enabled`).
- **Default Media Files**: Automatically picks up media placed in `/sdcard/Movies`, `/sdcard/Download`, or internal app storage.

---

## 3. Build & Deployment Toolchain

### Prerequisites
- JDK 17
- Android SDK (API 28+)
- Python 3 with `adb-shell` (configured in `tools/tv.py`)

### Build Commands (Windows PowerShell)
```powershell
# Clean and assemble release APK
cd cleanyourtv
.\launcher\gradlew.bat -p launcher assembleRelease
```
Output artifact: `launcher/app/build/outputs/apk/release/app-release.apk`

### Deploy to TV via `tools/tv.py`
```powershell
# Set TV IP address
$env:TV_HOST="<YOUR_TV_IP>"

# Push APK to device
python tools/tv.py --push "launcher/app/build/outputs/apk/release/app-release.apk" "/data/local/tmp/tvhome.apk"

# Install and restart launcher
python tools/tv.py "pm install -r -d /data/local/tmp/tvhome.apk && am force-stop com.gathin.tvhome && input keyevent KEYCODE_HOME"
```

### Remote Inspection & Screenshots
```powershell
# Capture TV screenshot
python tools/tv.py --screenshot "screenshot.png"

# Inspect launcher logs
python tools/tv.py "logcat -d -s TvHome:V"
```

---

## 4. Coding Standards & Guidelines for AI Agents

1. **Software Rendering Safety**:
   - Never decode bitmaps directly into `Bitmap.Config.HARDWARE` if they might be drawn on a software `Canvas`.
   - In `displayCustomImage`, always use `BitmapFactory.Options.inPreferredConfig = Bitmap.Config.ARGB_8888`.
2. **Leanback Activity Safety**:
   - Leanback side panels run in independent tasks. Do not call `syncWallpaperSnapshot()` inside `open(Intent i)` right before `startActivity()`; instead, sync during `onResume()`, `postDelayed()`, or theme switches.
3. **Remote Control (D-Pad) First**:
   - Every interactive element must have `focusable="true"` and define explicit `nextFocusUp`, `nextFocusDown`, `nextFocusLeft`, and `nextFocusRight` where automatic focus traversal fails.
4. **Git Discipline**:
   - Keep TV backup dumps (`backup_*/`), device serials, adb keys, and scratch scripts out of commits (enforced in `.gitignore`).
