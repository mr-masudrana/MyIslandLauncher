package com.masudrana.myisland

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private val Background = Color(0xFF101116)
private val Accent = Color(0xFFB7F36B)
private val Card = Color(0xFF202431)

data class AppChoice(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SetupScreen() }
    }
}

@Composable
private fun SetupScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var overlayAllowed by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var listenerAllowed by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var running by remember { mutableStateOf(isIslandServiceRunning(context)) }
    var status by remember { mutableStateOf(if (running) "Island running" else "Island stopped") }
    var filterEnabled by remember { mutableStateOf(IslandPrefs.hasAppFilter(context)) }
    var musicEnabled by remember { mutableStateOf(IslandPrefs.musicEnabled(context)) }
    var chargingEnabled by remember { mutableStateOf(IslandPrefs.chargingEnabled(context)) }
    var topOffset by remember { mutableFloatStateOf(IslandPrefs.topOffset(context).toFloat()) }
    var width by remember { mutableFloatStateOf(IslandPrefs.pillWidth(context).toFloat()) }
    var height by remember { mutableFloatStateOf(IslandPrefs.pillHeight(context).toFloat()) }
    var colorChoice by remember { mutableIntStateOf(IslandPrefs.pillColor(context)) }
    var selectedApps by remember { mutableStateOf(IslandPrefs.selectedPackages(context)) }
    val apps = remember { loadLaunchableApps(context) }

    fun refreshPermissions() {
        overlayAllowed = Settings.canDrawOverlays(context)
        listenerAllowed = isNotificationListenerEnabled(context)
        running = isIslandServiceRunning(context)
        status = if (running) "Island running" else "Island stopped"
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val overlayLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refreshPermissions() }
    val listenerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refreshPermissions() }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Card)) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
            ) {
                item {
                    Text("MYISLAND", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Notification Control", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("A small island. Useful moments.", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.background(Color(AndroidColor.BLACK), RoundedCornerShape(40.dp)).padding(horizontal = 28.dp, vertical = 15.dp)) {
                        Text("●  MY ISLAND", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("v0.5.0 • notification controls", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(22.dp))
                    PermissionCard("Display over other apps", "Required for the floating pill", overlayAllowed) {
                        overlayLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                    }
                    Spacer(Modifier.height(10.dp))
                    PermissionCard("Notification access", "Needed for notifications and media sessions", listenerAllowed) {
                        listenerLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                    Spacer(Modifier.height(20.dp))
                    SectionTitle("Dynamic Island appearance")
                    SettingSlider("Position from top", topOffset, 0f, 80f, "dp") {
                        topOffset = it; IslandPrefs.setTopOffset(context, it.toInt())
                    }
                    SettingSlider("Pill width", width, 180f, 360f, "dp") {
                        width = it; IslandPrefs.setPillWidth(context, it.toInt())
                    }
                    SettingSlider("Pill height", height, 48f, 100f, "dp") {
                        height = it; IslandPrefs.setPillHeight(context, it.toInt())
                    }
                    Text("Pill color", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(AndroidColor.BLACK, 0xFF161820.toInt(), 0xFF243044.toInt(), 0xFF172817.toInt()).forEach { color ->
                            Surface(
                                onClick = { colorChoice = color; IslandPrefs.setPillColor(context, color) },
                                color = Color(color), shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(if (colorChoice == color) 3.dp else 1.dp, if (colorChoice == color) Accent else Color.Gray),
                                modifier = Modifier.size(42.dp)
                            ) {}
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    SectionTitle("Notification privacy")
                    SettingSwitch("Only selected apps", "Hide notifications from apps not selected below", filterEnabled) {
                        filterEnabled = it; IslandPrefs.setAppFilter(context, it)
                    }
                    Text("Choose apps allowed to appear in the island", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                }
                items(apps, key = { it.packageName }) { app ->
                    Row(
                        Modifier.fillMaxWidth().background(Card, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, color = Color.White, fontSize = 14.sp)
                            Text(app.packageName, color = Color.Gray, fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = selectedApps.contains(app.packageName),
                            onCheckedChange = { checked ->
                                selectedApps = if (checked) selectedApps + app.packageName else selectedApps - app.packageName
                                IslandPrefs.setSelectedPackages(context, selectedApps)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Accent)
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                }
                item {
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    SectionTitle("Live activities")
                    SettingSwitch("Music controls", "Show media sessions and playback buttons when available", musicEnabled) {
                        musicEnabled = it; IslandPrefs.setMusicEnabled(context, it)
                    }
                    SettingSwitch("Charging animation", "Show charging state and battery percentage", chargingEnabled) {
                        chargingEnabled = it; IslandPrefs.setChargingEnabled(context, it)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            refreshPermissions()
                            if (!overlayAllowed) status = "Allow Display over other apps first"
                            else {
                                try {
                                    context.startService(Intent(context, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_START))
                                    running = true; status = "Island running"
                                } catch (e: Exception) {
                                    running = false; status = "Could not start: ${e.javaClass.simpleName}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = overlayAllowed && !running,
                        shape = RoundedCornerShape(16.dp)
                    ) { Text(if (running) "Dynamic Island Running" else "Start Dynamic Island") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            context.stopService(Intent(context, IslandOverlayService::class.java))
                            running = false; status = "Island stopped"
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = running,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, null); Spacer(Modifier.width(8.dp)); Text("Stop Island")
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(status, color = Color.LightGray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Notification content is processed locally. Selected-app filtering is optional.", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = Accent, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
}

@Composable
private fun SettingSlider(title: String, value: Float, min: Float, max: Float, unit: String, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.White, fontSize = 13.sp)
            Text("${value.toInt()} $unit", color = Accent, fontSize = 12.sp)
        }
        Slider(value = value, onValueChange = onChange, valueRange = min..max, colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent))
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.LightGray, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = .35f)))
    }
}

@Composable
private fun PermissionCard(title: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Notifications, null, tint = Accent, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(if (enabled) "Enabled" else description, color = if (enabled) Accent else Color.LightGray, fontSize = 11.sp)
        }
        TextButton(onClick = onClick) { Text(if (enabled) "Review" else "Allow") }
    }
}

@Suppress("DEPRECATION")
private fun loadLaunchableApps(context: Context): List<AppChoice> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        .mapNotNull { info ->
            val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
            if (packageName == context.packageName) return@mapNotNull null
            AppChoice(info.loadLabel(pm)?.toString() ?: packageName, packageName)
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    return flat.split(":").any { it.contains(context.packageName) }
}
private fun isIslandServiceRunning(context: Context): Boolean = IslandOverlayService.isRunning
