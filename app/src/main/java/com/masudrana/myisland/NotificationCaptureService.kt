package com.masudrana.myisland

import android.app.Notification
import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager

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

        val category = notification.category.orEmpty()
        val isCall = category == Notification.CATEGORY_CALL ||
            category == Notification.CATEGORY_MISSED_CALL ||
            category == Notification.CATEGORY_MESSAGE && (pkg.contains("phone", true) || pkg.contains("dialer", true))
        val isMedia = category == Notification.CATEGORY_TRANSPORT ||
            extras.containsKey("android.mediaSession") ||
            pkg.contains("spotify", true) || pkg.contains("music", true) ||
            pkg.contains("youtube", true) || pkg.contains("deezer", true)
        val ongoing = sbn.isOngoing || isCall || isMedia

        if (isMedia && !IslandPrefs.musicEnabled(this)) return

        val event = Intent(ACTION_NOTIFICATION).setPackage(packageName).apply {
            putExtra("title", title.ifBlank { if (isCall) "Call" else "Notification" })
            putExtra("text", text)
            putExtra("sourcePackage", pkg)
            putExtra("isMedia", isMedia)
            putExtra("isCall", isCall)
            putExtra("ongoing", ongoing)
            putExtra("notificationKey", sbn.key)
            notification.contentIntent?.let { putExtra("contentIntent", it) }
        }
        sendBroadcast(event)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        sendBroadcast(Intent(ACTION_NOTIFICATION_REMOVED).setPackage(packageName).apply {
            putExtra("notificationKey", sbn.key)
            putExtra("sourcePackage", sbn.packageName)
        })
    }

    companion object {
        const val ACTION_NOTIFICATION = "com.masudrana.myisland.NOTIFICATION_EVENT"
        const val ACTION_NOTIFICATION_REMOVED = "com.masudrana.myisland.NOTIFICATION_REMOVED"
    }
}
