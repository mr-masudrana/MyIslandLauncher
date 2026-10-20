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


## v0.4.3 notification-only behavior
- Overlay is not displayed permanently after Start; it appears only when a non-system app posts a notification.
- Notification pill auto-hides after five seconds but the service remains running.
- Overlay is non-touchable, so tapping near the top cannot dismiss it.
- Filters MyIsland and common Android/MIUI system package notifications and group-summary notifications.
- Build/test on a real HyperOS device is still required; some OEM package names may differ.


## v0.4.4 animated notifications and app icons
- Notification pill opens with a short scale/fade animation and closes with a fade/scale animation.
- Shows the source app's launcher icon when Android permits resolving that package.
- The overlay remains non-touchable and auto-hides after five seconds.
- Real-device build and HyperOS verification are still required.


## v0.5.0 Notification Control
- Dynamic Island pill appearance settings: top offset, width, height, and selectable colors.
- Optional selected-app notification filter. With filtering enabled, only checked packages are displayed.
- Music notification detection and playback controls for an active media session (previous/play-pause/next), when Android exposes an active session.
- Charging state and battery percentage pop-up, controlled by a setting.
- Notification text is processed locally; no network permission is added.
- Requires overlay and notification access. HyperOS may restrict background activity; enable Autostart/background activity if needed.
- This source update has not been built/tested on a physical device. Verify GitHub Actions logs and test media controls on device.

- Overlay accepts touch so media buttons can be used; keep controls small and test interactions over other apps on HyperOS.


## v0.5.1 persistent call/media and notification tap
- Ongoing notifications (including detected call/media notifications) do not auto-hide on the normal 5-second timer.
- Notification removal events can close the matching persistent pill after a short delay.
- Battery broadcasts only trigger the charging pill on a charging-state transition, avoiding repeated flicker; battery percentage updates while the charging pill is visible.
- The pill stores the notification content PendingIntent and sends it when tapped, falling back to launching the source app.
- Note: phone-call notification categorization varies by dialer/OEM; ongoing call display must be verified on HyperOS.
- Source has not been built or tested on a physical device.


## v0.5.2 - Settings and app selector
- Home page has a settings icon in the top bar.
- Settings page separates Appearance and Activities.
- Home page shows a Select apps button under the two permission cards.
- App selector supports individual app selection, Select all, and clear all by toggling Select all off.
- App choices persist locally. The notification filter is only restrictive when "Only selected apps" is enabled; otherwise notifications from all apps may show.
- Build with GitHub Actions workflow and download `MyIsland-v0.5.2-debug-apk`.
