package com.masudrana.myisland

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF101116)
private val Accent = Color(0xFFB7F36B)
private val Card = Color(0xFF202431)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyIslandSetup() }
    }
}

@Composable
private fun MyIslandSetup() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var overlayAllowed by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var listenerAllowed by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var running by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Card)) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            Column(
                Modifier.fillMaxSize().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(30.dp))
                Text("MYISLAND", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Spacer(Modifier.height(12.dp))
                Text("Dynamic Island", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("A small island. Useful moments.", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(38.dp))
                Box(
                    Modifier.background(Color.Black, RoundedCornerShape(40.dp)).padding(horizontal = 28.dp, vertical = 17.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).background(Accent, RoundedCornerShape(50)))
                        Spacer(Modifier.width(12.dp))
                        Text("MY ISLAND", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Preview • Tap Start to show the floating pill", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(34.dp))
                PermissionCard("Display over other apps", "Required to show the floating island", overlayAllowed, Icons.Default.Tune) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
                Spacer(Modifier.height(12.dp))
                PermissionCard("Notification access", "Optional: detect notification events", listenerAllowed, Icons.Default.Notifications) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        overlayAllowed = Settings.canDrawOverlays(context)
                        listenerAllowed = isNotificationListenerEnabled(context)
                        if (overlayAllowed) {
                            context.startService(Intent(context, IslandOverlayService::class.java))
                            running = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = overlayAllowed
                ) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Dynamic Island")
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        context.stopService(Intent(context, IslandOverlayService::class.java))
                        running = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop Island")
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (running) "Island started. It may be affected by HyperOS battery management."
                    else "No root • No internet permission • Notification content is not uploaded",
                    color = Color.Gray, fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(title: String, description: String, enabled: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(if (enabled) "Enabled" else description, color = if (enabled) Accent else Color.LightGray, fontSize = 12.sp)
        }
        TextButton(onClick = onClick) { Text(if (enabled) "Review" else "Allow") }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    return flat.split(":").any { it.contains(context.packageName) }
}
