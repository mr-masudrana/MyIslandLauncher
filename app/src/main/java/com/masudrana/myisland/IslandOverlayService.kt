package com.masudrana.myisland

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class IslandOverlayService : Service() {
    private var wm: WindowManager? = null
    private var root: LinearLayout? = null
    private var appIcon: ImageView? = null
    private var titleView: TextView? = null
    private var messageView: TextView? = null
    private var controls: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null
    private var attached = false
    private var currentAnimator: ValueAnimator? = null
    private var mediaController: android.media.session.MediaController? = null
    private var currentPackage = ""
    private var currentKey = ""
    private var currentContentIntent: PendingIntent? = null
    private var currentIsOngoing = false
    private var currentIsMedia = false
    private var currentIsCall = false
    private var charging = false
    private var lastBatteryLevel = -1
    private var lastBatteryStatus = -1

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                NotificationCaptureService.ACTION_NOTIFICATION -> {
                    val pending = if (Build.VERSION.SDK_INT >= 33)
                        intent.getParcelableExtra("contentIntent", PendingIntent::class.java)
                    else @Suppress("DEPRECATION") intent.getParcelableExtra("contentIntent")
                    showEvent(
                        intent.getStringExtra("title").orEmpty().ifBlank { "Notification" },
                        intent.getStringExtra("text").orEmpty(),
                        intent.getStringExtra("sourcePackage").orEmpty(),
                        intent.getBooleanExtra("isMedia", false),
                        intent.getBooleanExtra("isCall", false),
                        intent.getBooleanExtra("ongoing", false),
                        intent.getStringExtra("notificationKey").orEmpty(),
                        pending,
                        if (intent.getBooleanExtra("ongoing", false)) 0L else 5000L
                    )
                }
                NotificationCaptureService.ACTION_NOTIFICATION_REMOVED -> {
                    val key = intent.getStringExtra("notificationKey").orEmpty()
                    if (key.isNotBlank() && key == currentKey && currentIsOngoing) {
                        scheduleHide(1800L)
                    }
                }
            }
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_BATTERY_CHANGED || !IslandPrefs.chargingEnabled(this@IslandOverlayService)) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val nowCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val changed = nowCharging != charging || level != lastBatteryLevel || status != lastBatteryStatus
            lastBatteryLevel = level
            lastBatteryStatus = status
            if (!changed) return
            // Battery broadcasts are frequent. Only show on plug/unplug transition, not every percentage tick.
            if (nowCharging != charging) {
                charging = nowCharging
                if (nowCharging) {
                    showEvent("⚡ Charging", if (level >= 0) "Battery $level%" else "Charging", "", false, false, false, "myisland:charging", null, 3500L)
                } else if (currentKey == "myisland:charging") {
                    animateClose()
                }
            } else if (nowCharging && currentKey == "myisland:charging") {
                messageView?.text = if (level >= 0) "Battery $level%" else "Charging"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        ContextCompat.registerReceiver(this, eventReceiver, IntentFilter().apply {
            addAction(NotificationCaptureService.ACTION_NOTIFICATION)
            addAction(NotificationCaptureService.ACTION_NOTIFICATION_REMOVED)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(this, batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        return START_STICKY
    }

    private fun showEvent(
        title: String, message: String, packageName: String,
        media: Boolean, call: Boolean, ongoing: Boolean, key: String,
        contentIntent: PendingIntent?, duration: Long
    ) {
        handler.post {
            if (!Settings.canDrawOverlays(this)) return@post
            ensureView()
            currentPackage = packageName
            currentKey = key
            currentContentIntent = contentIntent
            currentIsOngoing = ongoing
            currentIsMedia = media
            currentIsCall = call

            titleView?.text = title.take(70)
            messageView?.text = message.take(160)
            messageView?.visibility = if (message.isBlank()) View.GONE else View.VISIBLE
            val icon = if (packageName.isNotBlank()) try { packageManager.getApplicationIcon(packageName) } catch (_: Exception) { null } else null
            appIcon?.setImageDrawable(icon)
            appIcon?.visibility = if (icon != null) View.VISIBLE else View.GONE
            controls?.visibility = if (media && IslandPrefs.musicEnabled(this)) View.VISIBLE else View.GONE
            if (media) refreshMediaController()
            applyAppearance()
            hideRunnable?.let(handler::removeCallbacks)
            currentAnimator?.cancel()
            root?.alpha = 0f; root?.scaleX = .75f; root?.scaleY = .75f
            attachIfNeeded()
            animateOpen()
            if (duration > 0L) scheduleHide(duration)
        }
    }

    private fun scheduleHide(delay: Long) {
        hideRunnable?.let(handler::removeCallbacks)
        hideRunnable = Runnable { animateClose() }
        handler.postDelayed(hideRunnable!!, delay)
    }

    private fun ensureView() {
        if (root != null) return
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        appIcon = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply { rightMargin = dp(10) }
            background = GradientDrawable().apply { setColor(0xFF282828.toInt()); cornerRadius = dp(9).toFloat() }
            clipToOutline = true
        }
        titleView = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 13f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END
        }
        messageView = TextView(this).apply {
            setTextColor(0xFFE5E5E5.toInt()); textSize = 12f; maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleView, LinearLayout.LayoutParams(-1, -2))
            addView(messageView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        }
        controls = LinearLayout(this).apply {
            gravity = Gravity.CENTER; visibility = View.GONE
            addView(mediaButton("⏮") { mediaController?.transportControls?.skipToPrevious() })
            addView(mediaButton("▶ / ❚❚") {
                val state = mediaController?.playbackState?.state
                if (state == android.media.session.PlaybackState.STATE_PLAYING) mediaController?.transportControls?.pause()
                else mediaController?.transportControls?.play()
            })
            addView(mediaButton("⏭") { mediaController?.transportControls?.skipToNext() })
        }
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            addView(appIcon)
            addView(textColumn, LinearLayout.LayoutParams(0, -2, 1f))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(topRow, LinearLayout.LayoutParams(-1, -2))
            addView(controls, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = GradientDrawable().apply { cornerRadius = dp(32).toFloat(); setStroke(dp(1), 0xFF292929.toInt()) }
            elevation = dp(8).toFloat(); alpha = 0f; scaleX = .75f; scaleY = .75f
            addView(content, LinearLayout.LayoutParams(-1, -2))
            // Clicking a notification opens its originating PendingIntent when available.
            setOnClickListener {
                try {
                    currentContentIntent?.send()
                } catch (_: PendingIntent.CanceledException) {
                    if (currentPackage.isNotBlank()) {
                        val launch = packageManager.getLaunchIntentForPackage(currentPackage)
                        if (launch != null) {
                            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(launch)
                        }
                    }
                } catch (_: Exception) {
                    if (currentPackage.isNotBlank()) {
                        val launch = packageManager.getLaunchIntentForPackage(currentPackage)
                        if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch) }
                    }
                }
            }
            isClickable = true
        }
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        params = WindowManager.LayoutParams(
            dp(IslandPrefs.pillWidth(this)), WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = dp(IslandPrefs.topOffset(this)) }
    }

    private fun mediaButton(label: String, action: () -> Unit) = TextView(this).apply {
        text = label; setTextColor(0xFFB7F36B.toInt()); textSize = 12f; gravity = Gravity.CENTER
        setPadding(dp(12), dp(6), dp(12), dp(6)); isClickable = true
        setOnClickListener { action() }
    }

    private fun refreshMediaController() {
        try {
            val manager = getSystemService(Context.MEDIA_SESSION_SERVICE) as android.media.session.MediaSessionManager
            mediaController = manager.getActiveSessions(ComponentName(this, NotificationCaptureService::class.java)).firstOrNull()
        } catch (_: SecurityException) { mediaController = null } catch (_: Exception) { mediaController = null }
    }

    private fun applyAppearance() {
        root?.background = GradientDrawable().apply {
            setColor(IslandPrefs.pillColor(this@IslandOverlayService))
            cornerRadius = dp(32).toFloat()
            setStroke(dp(1), 0xFF292929.toInt())
        }
        params?.width = dp(IslandPrefs.pillWidth(this))
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.y = dp(IslandPrefs.topOffset(this))
        root?.minimumHeight = dp(IslandPrefs.pillHeight(this))
    }

    private fun attachIfNeeded() {
        val view = root ?: return
        val p = params ?: return
        try {
            if (!attached) { wm?.addView(view, p); attached = true }
            else wm?.updateViewLayout(view, p)
        } catch (_: Exception) { attached = false; stopSelf() }
    }

    private fun animateOpen() {
        val view = root ?: return
        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 280; interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                val v = a.animatedValue as Float
                view.alpha = v; view.scaleX = .75f + .25f * v; view.scaleY = .75f + .25f * v
            }; start()
        }
    }

    private fun animateClose() {
        if (!attached) return
        hideRunnable?.let(handler::removeCallbacks); hideRunnable = null
        currentAnimator?.cancel()
        val view = root ?: return
        currentAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 220; interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                val v = a.animatedValue as Float
                view.alpha = v; view.scaleX = .75f + .25f * v; view.scaleY = .75f + .25f * v
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { detachView() }
            }); start()
        }
    }

    private fun detachView() {
        try { if (attached) root?.let { wm?.removeView(it) } } catch (_: Exception) {} finally { attached = false }
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        isRunning = false
        hideRunnable?.let(handler::removeCallbacks); currentAnimator?.cancel()
        try { unregisterReceiver(eventReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        detachView(); root = null; wm = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        @Volatile var isRunning: Boolean = false
            private set
        const val ACTION_START = "com.masudrana.myisland.START_ISLAND"
    }
}
