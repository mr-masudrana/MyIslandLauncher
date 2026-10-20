# MyIsland Launcher — starter

Kotlin + Jetpack Compose Android launcher starter for Redmi Note 12 4G / HyperOS. Root-free.

## Included
- Selectable Android HOME activity.
- App grid, search, installed app icons and dock.
- Branded Dynamic Island preview in the launcher.
- Optional overlay demo pill (user must grant overlay permission and tap Start).
- Notification listener placeholder that does not read, store or transmit notification text.
- GitHub Actions debug APK workflow.

## Not yet implemented
This starter is not a finished launcher. Real AppWidgetHost picker/placement, live notification-to-island bridge, media controls, charging events, icon-pack support, folders and gestures are follow-up milestones. Ordinary apps cannot fully replace Android's system notification shade or Quick Settings.

## Build locally
Install JDK 17 and Gradle 8.9+ then run `gradle assembleDebug`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Build on GitHub
1. Create a repository and upload all files.
2. Push to `main` or choose Actions → Build MyIsland APK → Run workflow.
3. Download `MyIsland-debug-apk` artifact from the successful run and extract the APK.

## First run on HyperOS
1. Install APK and open MyIsland Launcher.
2. Set it as default Home app if prompted.
3. Settings button opens permission setup.
4. Overlay permission is optional and only needed for the floating demo pill.
5. Notification access is optional; current listener intentionally ignores notification contents.

## Privacy and safety
No Internet permission is declared. Notification content is not logged or uploaded. Overlay and notification access are sensitive; only enable after reviewing source. HyperOS may stop background processes. This debug APK is not a signed production release.
