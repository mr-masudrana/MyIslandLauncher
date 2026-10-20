package com.masudrana.myisland

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Reads notification title/text locally to show a brief floating preview.
 * No notification content is logged, stored persistently, or sent over the network.
 */
class NotificationCaptureService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName == packageName) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = (
            extras.getCharSequence("android.text")
                ?: extras.getCharSequence("android.bigText")
            )?.toString().orEmpty()

        // Ignore empty events, such as some silent/system updates.
        if (title.isBlank() && text.isBlank()) return

        sendBroadcast(Intent(ACTION_NOTIFICATION).apply {
            setPackage(packageName)
            putExtra("title", title)
            putExtra("text", text)
        })
    }

    companion object {
        const val ACTION_NOTIFICATION = "com.masudrana.myisland.LOCAL_NOTIFICATION"
    }
}
