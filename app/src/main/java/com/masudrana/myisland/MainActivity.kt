package com.masudrana.myisland

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

private val Bg = Color(0xFF10121A); private val Panel = Color(0xFF202635); private val Lime = Color(0xFFB6F36B)
private val appList = listOf("Phone" to "com.google.android.dialer", "Messages" to "com.google.android.apps.messaging", "Camera" to "com.android.camera", "Settings" to "com.android.settings", "Chrome" to "com.android.chrome", "Photos" to "com.google.android.apps.photos", "YouTube" to "com.google.android.youtube", "Play Store" to "com.android.vending", "Gmail" to "com.google.android.gm", "Clock" to "com.android.deskclock", "Calendar" to "com.google.android.calendar", "Files" to "com.google.android.documentsui")

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { Home() } } }

@Composable private fun Home() {
 val ctx = androidx.compose.ui.platform.LocalContext.current; val pm = ctx.packageManager
 var search by remember { mutableStateOf("") }; var settings by remember { mutableStateOf(false) }; var refresh by remember { mutableIntStateOf(0) }
 val enabled = remember(refresh) { Settings.canDrawOverlays(ctx) }
 val notificationEnabled = remember(refresh) { (Settings.Secure.getString(ctx.contentResolver,"enabled_notification_listeners") ?: "").contains(ctx.packageName) }
 val shown = remember(search) { appList.filter { it.first.contains(search,true) } }
 MaterialTheme(colorScheme=darkColorScheme(primary=Lime, background=Bg, surface=Panel, onPrimary=Bg, onBackground=Color.White, onSurface=Color.White)) {
  Surface(Modifier.fillMaxSize(), color=Bg) { Column(Modifier.fillMaxSize().padding(18.dp)) {
   Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) { Column { Text("MYISLAND", color=Lime, fontWeight=FontWeight.Black, letterSpacing=3.sp); Text("Your space, your style", color=Color.LightGray, fontSize=12.sp) }; IconButton(onClick={settings=!settings}) { Icon(Icons.Default.Tune,"Settings") } }
   Spacer(Modifier.height(22.dp)); IslandPreview(); Spacer(Modifier.height(22.dp))
   if(settings) {
    Text("Quick setup",fontSize=22.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(10.dp))
    Setup("Notification access",notificationEnabled) { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    Setup("Display over other apps",enabled) { ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:${ctx.packageName}"))) }
    Button(onClick={refresh++; if(Settings.canDrawOverlays(ctx)) ctx.startService(Intent(ctx,IslandOverlayService::class.java))}) { Text("Refresh & start Island") }
    Text("Permissions are optional. Notification content is not stored or uploaded.",fontSize=12.sp,color=Color.LightGray)
   } else {
    Text("Your apps",fontSize=22.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(8.dp))
    OutlinedTextField(search,{search=it},Modifier.fillMaxWidth(),placeholder={Text("Search apps")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,shape=RoundedCornerShape(18.dp))
    Spacer(Modifier.height(12.dp))
    LazyVerticalGrid(columns=GridCells.Fixed(4),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(15.dp)) {
     items(shown) { (label,pkg) ->
      val icon = remember(pkg) { try { pm.getApplicationIcon(pkg).toBitmap() } catch (_:Exception) { null } }
      Column(Modifier.clickable { pm.getLaunchIntentForPackage(pkg)?.let { ctx.startActivity(it) } }.padding(vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally) {
       Box(Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(Panel),contentAlignment=Alignment.Center) { if(icon!=null) Image(icon.asImageBitmap(),label,Modifier.fillMaxSize().padding(7.dp)) else Icon(Icons.Default.Apps,null,tint=Lime) }
       Spacer(Modifier.height(5.dp)); Text(label,fontSize=10.sp,maxLines=1,textAlign=TextAlign.Center)
      }
     }
    }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Panel).padding(15.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
     Icon(Icons.Default.Call,"Phone",tint=Lime,modifier=Modifier.clickable { open(ctx,"com.google.android.dialer") }); Icon(Icons.Default.Message,"Messages",tint=Lime,modifier=Modifier.clickable { open(ctx,"com.google.android.apps.messaging") }); Icon(Icons.Default.CameraAlt,"Camera",tint=Lime,modifier=Modifier.clickable { open(ctx,"com.android.camera") }); Icon(Icons.Default.MusicNote,"Music",tint=Lime,modifier=Modifier.clickable { open(ctx,"com.google.android.music") })
    }
   }
  } }
 }
}
@Composable private fun IslandPreview() { var expanded by remember { mutableStateOf(false) }; Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) { Row(Modifier.clip(RoundedCornerShape(40.dp)).background(Color.Black).clickable { expanded=!expanded }.padding(horizontal=22.dp,vertical=if(expanded) 15.dp else 10.dp),verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(8.dp).clip(CircleShape).background(Lime)); Spacer(Modifier.width(9.dp)); Text(if(expanded) "MY ISLAND • READY" else "MY ISLAND",fontSize=11.sp,fontWeight=FontWeight.Bold); if(expanded) { Spacer(Modifier.width(10.dp)); Icon(Icons.Default.GraphicEq,null,tint=Lime) } }; Spacer(Modifier.height(7.dp)); Text("Tap pill to preview",color=Color.Gray,fontSize=11.sp) } }
@Composable private fun Setup(title:String,ok:Boolean,click:()->Unit) { Row(Modifier.fillMaxWidth().padding(vertical=5.dp).clip(RoundedCornerShape(14.dp)).background(Panel).clickable(onClick=click).padding(14.dp),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title); Text(if(ok) "Enabled" else "Tap to open settings",fontSize=12.sp,color=if(ok)Lime else Color.LightGray) }; Icon(if(ok) Icons.Default.CheckCircle else Icons.Default.ChevronRight,null,tint=Lime) } }
private fun open(ctx:Context,pkg:String) { ctx.packageManager.getLaunchIntentForPackage(pkg)?.let { ctx.startActivity(it) } }
private fun Drawable.toBitmap(): Bitmap { val b=Bitmap.createBitmap(intrinsicWidth.coerceAtLeast(1),intrinsicHeight.coerceAtLeast(1),Bitmap.Config.ARGB_8888); val c=Canvas(b); setBounds(0,0,c.width,c.height); draw(c); return b }
