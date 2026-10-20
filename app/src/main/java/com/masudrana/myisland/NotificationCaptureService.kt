package com.masudrana.myisland

import android.app.Notification
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Bundle

class NotificationCaptureService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return
        if (pkg == packageName || pkg == "android" || pkg == "com.android.systemui" ||
            pkg == "com.miui.system" || pkg == "com.miui.notification") return

        if (IslandPrefs.hasAppFilter(this) && !IslandPrefs.selectedPackages(this).contains(pkg)) return

        val notification = sbn.notification ?: return
        val extras: Bundle = notification.extras ?: return
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT))?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT ||
            extras.containsKey("android.mediaSession") ||
            pkg.contains("spotify", true) || pkg.contains("music", true) ||
            pkg.contains("youtube", true) || pkg.contains("deezer", true)

        if (isMedia && !IslandPrefs.musicEnabled(this)) return

        sendBroadcast(Intent(ACTION_NOTIFICATION).setPackage(packageName).apply {
            putExtra("title", title.ifBlank { "Notification" })
            putExtra("text", text)
            putExtra("sourcePackage", pkg)
            putExtra("isMedia", isMedia)
        })
    }

    companion object { const val ACTION_NOTIFICATION = "com.masudrana.myisland.NOTIFICATION_EVENT" }
}
