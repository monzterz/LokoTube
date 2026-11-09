package com.lokotube.app

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

/**
 * Applies an [Equalizer] and a [LoudnessEnhancer] (volume boost) to the player's
 * audio session using our own band-gain presets (device-independent — some
 * firmware reports zero built-in presets, so we synthesize curves and drive
 * [Equalizer.setBandLevel]).
 *
 * [presetIndex] 0 = off; 1..N map to [PRESETS]. [boostPercent] 0..100 = volume
 * boost amount (0 = off).
 */
object AudioEffects {
    private const val PREFS = "audio_effects"
    private const val KEY_PRESET = "eq_preset"
    private const val KEY_BOOST = "volume_boost"
    private const val MAX_BOOST_MB = 2000 // 100 mB = 1 dB; 2000 mB = +20 dB
    private const val TAG = "AudioFX"

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

    /** 0..100; 0 = off (no boost). */
    var boostPercent by mutableStateOf(0)
        private set

    private var equalizer: Equalizer? = null
    private var booster: LoudnessEnhancer? = null
    private var sessionId = 0

    private fun prefs(): SharedPreferences =
        LokoTubeApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load() {
        presetIndex = prefs().getInt(KEY_PRESET, 0)
        boostPercent = prefs().getInt(KEY_BOOST, 0).coerceIn(0, 100)
    }

    fun presetNames(): List<String> = PRESETS.map { it.first }

    fun attach(audioSessionId: Int) {
        Log.d(TAG, "attach session=$audioSessionId")
        sessionId = audioSessionId
        attachEqualizer()
        attachBooster()
    }

    private fun attachEqualizer() {
        if (sessionId <= 0 || presetIndex <= 0) return
        runCatching {
            val eq = Equalizer(0, sessionId)
            apply(eq, presetIndex - 1)
            eq.enabled = true
            equalizer?.release()
            equalizer = eq
            Log.d(TAG, "equalizer aktif bands=${eq.numberOfBands}")
        }.onFailure { Log.w(TAG, "attachEqualizer gagal", it) }
    }

    fun setPreset(index: Int) {
        presetIndex = index
        prefs().edit().putInt(KEY_PRESET, index).apply()
        if (index <= 0) {
            releaseEqualizer()
            return
        }
        val eq = equalizer
        if (eq == null) {
            if (sessionId > 0) attachEqualizer()
            return
        }
        runCatching {
            apply(eq, index - 1)
            eq.enabled = true
        }.onFailure { Log.w(TAG, "setPreset gagal", it) }
    }

    fun setBoost(pct: Int) {
        boostPercent = pct.coerceIn(0, 100)
        prefs().edit().putInt(KEY_BOOST, boostPercent).apply()
        Log.d(TAG, "setBoost $boostPercent% (session=$sessionId)")
        if (boostPercent <= 0) {
            releaseBooster()
            return
        }
        val b = booster
        if (b == null) {
            if (sessionId > 0) attachBooster()
            return
        }
        runCatching {
            b.setTargetGain(boostGainMB(boostPercent))
            b.enabled = true
            Log.d(TAG, "setBoost ok targetGain=${b.targetGain} mB")
        }.onFailure { Log.w(TAG, "setBoost gagal", it) }
    }

    private fun attachBooster() {
        if (sessionId <= 0 || boostPercent <= 0) {
            Log.d(TAG, "attachBooster skip (session=$sessionId boost=$boostPercent)")
            releaseBooster()
            return
        }
        runCatching {
            val le = LoudnessEnhancer(sessionId)
            le.setTargetGain(boostGainMB(boostPercent))
            le.enabled = true
            booster?.release()
            booster = le
            Log.d(TAG, "booster aktif gain=${le.targetGain} mB")
        }.onFailure { Log.w(TAG, "attachBooster gagal", it) }
    }

    private fun boostGainMB(pct: Int): Int =
        (MAX_BOOST_MB.toLong() * pct.coerceIn(0, 100) / 100L).toInt()

    private fun releaseEqualizer() {
        runCatching { equalizer?.release() }
        equalizer = null
    }

    private fun releaseBooster() {
        runCatching { booster?.release() }
        booster = null
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
        releaseEqualizer()
        releaseBooster()
    }
}