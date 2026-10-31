# Contributing to CustomCleanTV

We welcome contributions to make Android TV cleaner, faster, and more customizable!

---

## Areas You Can Help With

1. **New Themes & Visual Effects**:
   - New backdrop animations, particle shaders, or clock styles.
   - Vector logos for region-specific streaming apps in `WhiteLogoDrawable.java`.
2. **Device Profiles & Debloat Lists**:
   - Documenting tested debloat packages for untested TV models in `examples/`.
3. **Bug Fixes & Performance**:
   - Memory optimizations, smooth animations, and Leanback compatibility enhancements.
4. **Documentation**:
   - Improvements to user guides and AI onboarding docs (`LLM_ONBOARDING.md`).

---

## Development Workflow

1. **Fork the repo** and clone your fork:
   ```bash
   git clone https://github.com/Loge28/CustomCleanTV.git
   cd CustomCleanTV
   ```
2. **Build the APK**:
   ```powershell
   .\launcher\gradlew.bat -p launcher assembleRelease
   ```
3. **Test on your TV**:
   ```powershell
   $env:TV_HOST="<YOUR_TV_IP>"
   python tools/tv.py --push "launcher/app/build/outputs/apk/release/app-release.apk" "/data/local/tmp/tvhome.apk"
   python tools/tv.py "pm install -r -d /data/local/tmp/tvhome.apk && am force-stop com.gathin.tvhome && input keyevent KEYCODE_HOME"
   ```
4. **Pull Requests**:
   - Ensure clean commits with descriptive messages.
   - Submit PRs against the `main` branch.
