package com.masudrana.myisland

import android.content.Context
import android.graphics.Color

object IslandPrefs {
    private const val FILE = "myisland_preferences"
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun selectedPackages(context: Context): Set<String> =
        prefs(context).getStringSet("selected_packages", emptySet())?.toSet() ?: emptySet()

    fun setSelectedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet("selected_packages", packages.toSet()).apply()
    }

    fun hasAppFilter(context: Context): Boolean = prefs(context).getBoolean("filter_enabled", false)
    fun setAppFilter(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("filter_enabled", enabled).apply()
    }

    fun topOffset(context: Context) = prefs(context).getInt("top_offset", 4)
    fun setTopOffset(context: Context, value: Int) { prefs(context).edit().putInt("top_offset", value.coerceIn(0, 80)).apply() }

    fun pillWidth(context: Context) = prefs(context).getInt("pill_width", 300)
    fun setPillWidth(context: Context, value: Int) { prefs(context).edit().putInt("pill_width", value.coerceIn(180, 360)).apply() }

    fun pillHeight(context: Context) = prefs(context).getInt("pill_height", 64)
    fun setPillHeight(context: Context, value: Int) { prefs(context).edit().putInt("pill_height", value.coerceIn(48, 100)).apply() }

    fun pillColor(context: Context) = prefs(context).getInt("pill_color", Color.BLACK)
    fun setPillColor(context: Context, value: Int) { prefs(context).edit().putInt("pill_color", value).apply() }

    fun musicEnabled(context: Context) = prefs(context).getBoolean("music_enabled", true)
    fun setMusicEnabled(context: Context, value: Boolean) { prefs(context).edit().putBoolean("music_enabled", value).apply() }

    fun chargingEnabled(context: Context) = prefs(context).getBoolean("charging_enabled", true)
    fun setChargingEnabled(context: Context, value: Boolean) { prefs(context).edit().putBoolean("charging_enabled", value).apply() }
}
