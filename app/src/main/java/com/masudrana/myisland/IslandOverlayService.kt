package com.masudrana.myisland
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/** Explicitly started demo overlay. Does not intercept touches or cover status controls. */
class IslandOverlayService : Service() {
 private var wm:WindowManager?=null; private var pill:TextView?=null
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
  if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY}
  if(pill==null) { wm=getSystemService(WINDOW_SERVICE) as WindowManager
   pill=TextView(this).apply { text="●  MY ISLAND";setTextColor(Color.WHITE);textSize=11f;typeface=Typeface.DEFAULT_BOLD;setPadding(34,18,34,18);background=GradientDrawable().apply {setColor(Color.BLACK);cornerRadius=80f} }
   val type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
   val p=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT).apply {gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL;y=16}
   try {wm?.addView(pill,p)} catch(_:Exception){stopSelf()}
  };return START_NOT_STICKY
 }
 override fun onDestroy(){try{pill?.let{wm?.removeView(it)}}catch(_:Exception){};pill=null;wm=null;super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
}
