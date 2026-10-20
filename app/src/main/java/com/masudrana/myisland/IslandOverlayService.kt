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

class IslandOverlayService : Service() {
    private var wm: WindowManager? = null
    private var pill: TextView? = null
    private var expanded = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            pill?.text = if (expanded) {
                (if (charging) "⚡ Charging" else "MY ISLAND") + if (level >= 0) " • $level%" else ""
            } else {
                (if (charging) "⚡" else "●") + if (level >= 0) "  $level%" else "  MY ISLAND"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
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
            background = GradientDrawable().apply {
                setColor(Color.BLACK)
                cornerRadius = 100f
                setStroke(1, 0xFF262626.toInt())
            }
            setOnClickListener {
                expanded = !expanded
                text = if (expanded) "MY ISLAND  •  Tap to close" else "●  MY ISLAND"
                setPadding(if (expanded) 48 else 36, 19, if (expanded) 48 else 36, 19)
            }
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 14
        }
        try { wm?.addView(pill, params) } catch (_: Exception) { stopSelf() }
    }

    override fun onDestroy() {
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        try { pill?.let { wm?.removeView(it) } } catch (_: Exception) {}
        pill = null
        wm = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
