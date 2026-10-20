package com.masudrana.myisland

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Optional notification event listener.
 * This starter deliberately does not read notification title/text, store them,
 * log them, or transmit them. Future integration should use an app allowlist.
 */
class NotificationCaptureService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Notification content intentionally not accessed.
    }
}
