# CustomCleanTV 📺✨

An enhanced, high-performance Android TV launcher tailored for modern visuals, dynamic theming, and distraction-free viewing. Built on top of CleanYourTV with custom UI engines, vector logo recoloring, and media wallpapers.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform: Android TV](https://img.shields.io/badge/Platform-Android%20TV%209%2B-blue.svg)](https://developer.android.com/tv)
[![Onboarding Guide](https://img.shields.io/badge/AI%20Onboarding-LLM_ONBOARDING.md-green.svg)](LLM_ONBOARDING.md)

---

## 🌟 What's New & Custom Features

### 🌓 1. Dynamic Dark & White UI Theming
- **Instant Theme Toggle**: Switch between **White Theme** (default) and **Dark Theme** right next to the Rain Effect in the Theme popup menu.
- **Adaptive Vector Logos**: Brand vector logos (YouTube, Netflix, Prime Video, Spotify, Hotstar, ZEE5, Crunchyroll, etc.) dynamically recolor to deep charcoal (`#1C1D1F`) with soft drop-shadows in Dark Theme, ensuring crystal-clear legibility over light, bright wallpapers (e.g. Royal Enfield or daylight scenes).
- **Adaptive Clock & Top Pills**: Rolling digit clock, greeting, date, and pill buttons (Theme, Inputs, Wi-Fi, Settings) seamlessly adapt their typography and contrast.

### 🖼️ 2. Custom Video & Image Wallpaper Engine
- **Seamless 15-Second Video Loop**: Select your own video wallpapers (`.mp4`, `.mkv`, `.webm`) with automatic 15-second loop timing and optional audio toggle.
- **Wallpaper Scaling Options**: Toggle between **Fill Screen** (`CENTER_CROP`) and **Fit to Screen** (`FIT_CENTER`) for portrait and landscape wallpapers.
- **Integrated Storage Scanner**: Automatic media discovery across internal storage, `/sdcard/Movies`, `/sdcard/Download`, and `/sdcard/Pictures`.

### 🛡️ 3. Leanback Settings & App Info Overlay Fix
- **No More Black Screens**: On Android TV, system Settings (`MainSettings`) and App Info (`AppManagementActivity`) use `windowShowWallpaper = true`, which normally hides the launcher and shows a pitch-black screen.
- **Hardware-Safe PixelCopy Mirroring**: The launcher continuously mirrors its screen buffer directly to Android's system `WallpaperManager` via `PixelCopy`. When Settings or App Info opens as a sidebar on the right, the complete home screen (wallpaper, clock, and apps) stays fully visible on the left.

### 👁️ 4. Anime & Particle Effects
- **Sharingan Startup Animation**: Smooth rotation and pupil expansion sequence on launcher boot.
- **Interactive Pupil Tracking**: Procedural eye tracking looking toward the active D-pad focus.
- **Hidden Rain Effect**: Subtle ambient rain particles falling in the background (toggleable ON/OFF).

---

## 🚀 Quick Start & Building

### Prerequisites
- JDK 17
- Android SDK (API 28+ / Android 9.0 Pie or higher)
- Python 3 with `adb-shell`

### Build Release APK
```powershell
# From the project root:
.\launcher\gradlew.bat -p launcher assembleRelease
```
The compiled APK will be at:
`launcher/app/build/outputs/apk/release/app-release.apk`

### Install to TV over Wi-Fi
```powershell
# Set your TV's local IP address:
$env:TV_HOST="<YOUR_TV_IP>"

# Push APK to device:
python tools/tv.py --push "launcher/app/build/outputs/apk/release/app-release.apk" "/data/local/tmp/tvhome.apk"

# Install and make default home:
python tools/tv.py "pm install -r -d /data/local/tmp/tvhome.apk && am force-stop com.gathin.tvhome && input keyevent KEYCODE_HOME"
```

---

## 🤖 AI / LLM Development & Onboarding

If you are using an AI coding assistant (Claude, ChatGPT, Gemini, Copilot, Antigravity):
- Read **[`LLM_ONBOARDING.md`](LLM_ONBOARDING.md)** for architecture deep dives, view hierarchies, and Android TV lifecycle constraints.
- Copy and paste the prompt in **[`ONBOARDING_PROMPT.txt`](ONBOARDING_PROMPT.txt)** into your LLM chat to bring it up to speed immediately.

---

## 📄 License
This project is open-source under the [MIT License](LICENSE).
Outfit font is licensed under the [SIL Open Font License](launcher/FONT-LICENSE-OFL.txt).
Based on the CleanYourTV project.
