package com.miruai.app.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("miru_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "stability_api_key"
        private const val KEY_RECENT_VIDEOS = "recent_videos"
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    fun hasApiKey(): Boolean = apiKey.isNotEmpty()

    fun saveRecentVideo(path: String) {
        val current = getRecentVideos().toMutableList()
        current.add(0, path)
        val trimmed = current.take(10)
        prefs.edit().putString(KEY_RECENT_VIDEOS, trimmed.joinToString("|")).apply()
    }

    fun getRecentVideos(): List<String> {
        val raw = prefs.getString(KEY_RECENT_VIDEOS, "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split("|").filter { it.isNotEmpty() }
    }
}
