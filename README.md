# MyIsland v0.4 — App Icon & Splash Screen

A root-free Android floating pill demo for HyperOS. This is a source project; APK is built with GitHub Actions.

## Features
- Custom MyIsland adaptive app icon.
- Branded dark splash screen with a lime Dynamic Island mark.
- Floating overlay pill.
- Shows notification title/text briefly in the pill when Android sends notification events.
- Basic battery/charging indicator.
- Tap pill to expand/collapse.
- Notification text is handled locally only: not logged, saved persistently, or uploaded.
- No Internet permission is declared.

## Build APK
1. Upload all files to a GitHub repository, preserving `.github/workflows/build.yml`.
2. Push to `main` or go to **Actions → Build MyIsland APK → Run workflow**.
3. Download `MyIsland-v0.3-debug-apk` artifact from a successful run.
4. Extract it and install `app-debug.apk`.

## Setup on HyperOS
1. Open MyIsland and grant **Display over other apps**.
2. Grant **Notification access** from Android settings. This is sensitive: it allows the app to read notification content.
3. Return to MyIsland and tap **Start Dynamic Island**.
4. Send a test notification from another app.
5. If the overlay stops, reopen MyIsland and start it again; HyperOS battery management may restrict background services.

## Limitations
- This is an initial notification integration; notifications are not yet filtered by app and may include sensitive content on-screen.
- The notification listener reads title/text only to display them locally. No network permission is present.
- No quick reply, media controls, or production-grade animation yet.
- Do not commit signing keys. Debug APK is for testing only.

## v0.4 icon and splash changes
- Launcher icon uses Android adaptive icon resources, suitable for modern Android launchers.
- Splash screen uses AndroidX Core SplashScreen and automatically transitions to the app theme.
- To see the new icon, install/update the APK. If HyperOS caches the old icon, clear launcher cache or reboot; do not clear MyIsland app data unless necessary.


## v0.4.1 crash fix
- Dynamic broadcast receivers now use explicit `RECEIVER_NOT_EXPORTED` flags for modern Android versions.
- Start-service exceptions are caught and surfaced as status text.
- If the app still closes, capture Logcat from the crash to identify device-specific issues.


## v0.4.2 live UI state
- Permission labels refresh automatically when returning from Android Settings (`ON_RESUME`).
- Start/Stop buttons update immediately and reflect the service state.
- Start button is disabled while running; Stop button is disabled while stopped.
- This source has not been built on a device here; run GitHub Actions and share logs if it fails.
