package com.masudrana.myisland
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
/** Privacy-first placeholder: does not read, persist, or transmit notification text. */
class NotificationCaptureService:NotificationListenerService(){
 override fun onNotificationPosted(sbn:StatusBarNotification?){ if(sbn!=null) Log.d("MyIsland","Notification event received") }
}
