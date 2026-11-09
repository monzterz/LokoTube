package com.leplay.app

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.Equalizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Applies a system [Equalizer] to the player's audio session. Presets only —
 * no band sliders, kept simple and safe. Index 0 is "off" (no effect);
 * indices 1..N map to the device's built-in equalizer presets.
 */
object AudioEffects {
    private const val PREFS = "audio_effects"
    private const val KEY_PRESET = "eq_preset"

    private var equalizer: Equalizer? = null
    private var sessionId = 0

    var presetIndex by mutableStateOf(0)
        private set

    private fun prefs(): SharedPreferences =
        LePlayApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Restores the saved preset (called at app start). */
    fun load() {
        presetIndex = prefs().getInt(KEY_PRESET, 0)
    }

    /** Enumerates the device's equalizer presets (works without a session). */
    fun presetNames(): List<String> = runCatching {
        val eq = Equalizer(0, 0)
        try {
            (0 until eq.numberOfPresets.toInt()).map { eq.getPresetName(it.toShort()).toString() }
        } finally {
            eq.release()
        }
    }.getOrDefault(emptyList())

    /** Attaches (and re-attaches on session change) the equalizer. */
    fun attach(audioSessionId: Int) {
        sessionId = audioSessionId
        if (audioSessionId <= 0 || presetIndex <= 0) return
        runCatching {
            val eq = Equalizer(0, audioSessionId)
            presetNames().getOrNull(presetIndex - 1)?.toShort()?.let { eq.usePreset(it) }
            eq.enabled = true
            equalizer?.release()
            equalizer = eq
        }
    }

    /** Updates the preset and persists it. 0 turns the equalizer off. */
    fun setPreset(index: Int) {
        presetIndex = index
        prefs().edit().putInt(KEY_PRESET, index).apply()
        if (index <= 0) {
            release()
            return
        }
        val eq = equalizer
        if (eq == null) {
            if (sessionId > 0) attach(sessionId)
            return
        }
        runCatching {
            presetNames().getOrNull(index - 1)?.toShort()?.let { eq.usePreset(it) }
            eq.enabled = true
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        equalizer = null
    }
}