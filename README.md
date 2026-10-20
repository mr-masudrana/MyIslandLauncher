# MyIsland — Dynamic Island only

A minimal Android app for a floating iPhone-inspired pill. It does not act as a launcher and does not replace HyperOS system UI.

## Included
- Standalone setup screen.
- Explicit overlay permission and start/stop controls.
- Floating pill overlay.
- Basic charging/battery percentage display.
- Optional notification listener declaration, with no notification text collection.
- GitHub Actions workflow to build a debug APK.

## Build with GitHub Actions
1. Upload these project files to a new GitHub repository (keep `.github/workflows/build.yml` path).
2. Push to `main`, or run **Actions → Build MyIsland APK → Run workflow**.
3. Download the `MyIsland-debug-apk` artifact from the successful workflow run.
4. Extract the artifact ZIP and install `app-debug.apk`.

## On Redmi Note 12 4G / HyperOS
1. Open MyIsland.
2. Tap Allow for **Display over other apps**.
3. Return and tap **Start Dynamic Island**.
4. Notification access is optional and is not yet connected to the pill.
5. To stop the floating pill, open the app and tap Stop Island.

## Privacy & limitations
- No Internet permission is declared.
- No root is required.
- Notification content is not read, saved, logged, or uploaded.
- The overlay is a basic starter; actual notification animations and media controls are future work.
- HyperOS may restrict background services. Keep battery use in mind; do not disable system security protections.
- Debug APK is for testing. For public distribution, configure a release signing key stored in GitHub Actions secrets, never in the repository.
