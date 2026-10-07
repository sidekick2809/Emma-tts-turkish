package com.emma.tts.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("emma_preferences", Context.MODE_PRIVATE)

    var autoPlayOnShare: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PLAY, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_PLAY, value).apply()

    var speechSpeed: Float
        get() = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_SPEED, value).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_PITCH, value).apply()

    var selectedVoiceName: String?
        get() = prefs.getString(KEY_SELECTED_VOICE, null)
        set(value) = prefs.edit().putString(KEY_SELECTED_VOICE, value).apply()

    fun getHistory(): List<String> {
        val raw = prefs.getString(KEY_HISTORY, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(HISTORY_SEPARATOR)
    }

    fun addToHistory(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val current = getHistory().toMutableList()
        current.remove(clean) // Avoid duplicates
        current.add(0, clean) // Add to top
        val trimmed = current.take(30) // Keep last 30
        prefs.edit().putString(KEY_HISTORY, trimmed.joinToString(HISTORY_SEPARATOR)).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val KEY_AUTO_PLAY = "key_auto_play"
        private const val KEY_SPEECH_SPEED = "key_speech_speed"
        private const val KEY_SPEECH_PITCH = "key_speech_pitch"
        private const val KEY_SELECTED_VOICE = "key_selected_voice"
        private const val KEY_HISTORY = "key_history"
        private const val HISTORY_SEPARATOR = "«§EMMA_SEP§»"
    }
}
