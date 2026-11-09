package com.leplay.app

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.Equalizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

/**
 * Applies an [Equalizer] to the player's audio session using our own
 * band-gain presets (device-independent — some firmware reports zero built-in
 * presets, so we synthesize curves and drive [Equalizer.setBandLevel]).
 *
 * [presetIndex] 0 = off; 1..N map to [PRESETS].
 */
object AudioEffects {
    private const val PREFS = "audio_effects"
    private const val KEY_PRESET = "eq_preset"

    /** 5-band gain curves in dB (bass → treble). */
    private val PRESETS = listOf(
        "Bass Boost" to floatArrayOf(9f, 6f, 3f, 0f, 0f),
        "Treble Boost" to floatArrayOf(0f, 0f, 0f, 4f, 8f),
        "Vocal" to floatArrayOf(-3f, 0f, 4f, 3f, -1f),
        "Rock" to floatArrayOf(6f, 3f, 0f, 3f, 5f),
        "Pop" to floatArrayOf(-2f, 3f, 5f, 3f, -1f),
        "Jazz" to floatArrayOf(5f, 3f, 0f, 3f, 5f),
        "Classical" to floatArrayOf(4f, 2f, -1f, 2f, 4f),
        "Electronic" to floatArrayOf(5f, 2f, 0f, 3f, 6f),
    )

    var presetIndex by mutableStateOf(0)
        private set

    private var equalizer: Equalizer? = null
    private var sessionId = 0

    private fun prefs(): SharedPreferences =
        LePlayApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load() {
        presetIndex = prefs().getInt(KEY_PRESET, 0)
    }

    fun presetNames(): List<String> = PRESETS.map { it.first }

    fun attach(audioSessionId: Int) {
        sessionId = audioSessionId
        if (audioSessionId <= 0 || presetIndex <= 0) return
        runCatching {
            val eq = Equalizer(0, audioSessionId)
            apply(eq, presetIndex - 1)
            eq.enabled = true
            equalizer?.release()
            equalizer = eq
        }
    }

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
            apply(eq, index - 1)
            eq.enabled = true
        }
    }

    /** Maps the 5-band curve onto the device's actual band count. */
    private fun apply(eq: Equalizer, presetPos: Int) {
        val curve = PRESETS.getOrNull(presetPos)?.second ?: return
        val bands = eq.numberOfBands.toInt()
        if (bands <= 0) return
        val range = eq.bandLevelRange // [min, max] in milliBels
        val min = range[0].toInt()
        val max = range[1].toInt()
        for (b in 0 until bands) {
            val t = if (bands == 1) 0f else b.toFloat() / (bands - 1)
            val f = t * (curve.size - 1)
            val i0 = f.toInt().coerceIn(0, curve.size - 1)
            val i1 = (i0 + 1).coerceAtMost(curve.size - 1)
            val frac = f - i0
            val db = curve[i0] * (1f - frac) + curve[i1] * frac
            // 1 dB = 100 milliBels, clamped to the device's range.
            val level = (db * 100f).roundToInt().coerceIn(min, max).toShort()
            eq.setBandLevel(b.toShort(), level)
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        equalizer = null
    }
}