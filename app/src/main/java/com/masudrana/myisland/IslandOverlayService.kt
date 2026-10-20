package com.masudrana.myisland

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class IslandOverlayService : Service() {
    private var wm: WindowManager? = null
    private var pill: TextView? = null
    private var expanded = false
    private var lastTitle = "MY ISLAND"
    private var lastText = "Ready"

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != NotificationCaptureService.ACTION_NOTIFICATION) return
            lastTitle = intent.getStringExtra("title").orEmpty().ifBlank { "Notification" }.take(48)
            lastText = intent.getStringExtra("text").orEmpty().take(100)
            expanded = true
            updatePill()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (lastTitle == "MY ISLAND" || lastTitle == "Battery" || lastTitle.startsWith("⚡")) {
                lastTitle = if (charging) "⚡ Charging" else "Battery"
                lastText = if (level >= 0) "$level%" else "Battery status"
                updatePill()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        ContextCompat.registerReceiver(this, eventReceiver, IntentFilter(NotificationCaptureService.ACTION_NOTIFICATION), ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(this, batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (pill == null) showPill()
        return START_STICKY
    }

    private fun showPill() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        pill = TextView(this).apply {
            text = "●  MY ISLAND"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(36, 19, 36, 19)
            background = GradientDrawable().apply { setColor(Color.BLACK); cornerRadius = 100f; setStroke(1, 0xFF262626.toInt()) }
            setOnClickListener {
                expanded = !expanded
                if (!expanded) { lastTitle = "MY ISLAND"; lastText = "Ready" }
                updatePill()
            }
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 14 }
        try { wm?.addView(pill, params); updatePill() } catch (_: Exception) { isRunning = false; stopSelf() }
    }

    private fun updatePill() {
        pill?.post {
            pill?.text = if (expanded) {
                if (lastText.isBlank()) lastTitle else "$lastTitle\n$lastText"
            } else when {
                lastTitle.startsWith("⚡") -> "⚡ $lastText"
                lastTitle == "Battery" -> "●  $lastText"
                else -> "●  ${lastTitle.take(22)}"
            }
            pill?.setPadding(if (expanded) 28 else 36, if (expanded) 16 else 19, if (expanded) 28 else 36, if (expanded) 16 else 19)
        }
    }

    override fun onDestroy() {
        isRunning = false
        try { unregisterReceiver(eventReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        try { pill?.let { wm?.removeView(it) } } catch (_: Exception) {}
        pill = null; wm = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.masudrana.myisland.START_ISLAND"
        @Volatile var isRunning: Boolean = false
            private set
    }
}
