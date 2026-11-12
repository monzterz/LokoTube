package com.lokotube.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class BlockedChannel(val id: String, val name: String)

/** Channel yang diblokir agar tak muncul lagi di beranda/rekomendasi.
 *  Disimpan sebagai JSON di SharedPreferences, di-load sekali saat app
 *  start (lihat LokoTubeApplication), dan dibaca sebagai Compose state
 *  sehingga feed langsung merespons blokir/tanpa refresh. */
object BlockedChannels {
    private const val PREFS = "blocked_channels"
    private const val KEY = "channels"

    private val prefs by lazy {
        LokoTubeApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var channels by mutableStateOf<List<BlockedChannel>>(emptyList())
        private set

    fun load() {
        val raw = prefs.getString(KEY, null) ?: return
        runCatching {
            val array = JSONArray(raw)
            val out = ArrayList<BlockedChannel>(array.length())
            for (i in 0 until array.length()) {
                val json = array.optJSONObject(i) ?: continue
                val id = json.optString("id")
                if (id.isBlank()) continue
                out += BlockedChannel(id, json.optString("name"))
            }
            channels = out
        }
    }

    private fun persist() {
        runCatching {
            val array = JSONArray()
            channels.forEach { c ->
                array.put(JSONObject().put("id", c.id).put("name", c.name))
            }
            prefs.edit().putString(KEY, array.toString()).apply()
        }
    }

    /** True bila video milik channel yang diblokir. Aman dipanggil saat
     *  composition (hanya membaca state, tak memuat ulang prefs). */
    fun blocks(video: VideoItem): Boolean {
        val idKey = video.channelId()
        if (idKey != null && channels.any { it.id == idKey }) return true
        val nameKey = "name:${video.channel.trim().lowercase()}"
        return channels.any { it.id == nameKey }
    }

    fun block(video: VideoItem) {
        val key = video.channelId()
            ?: "name:${video.channel.trim().lowercase()}".takeIf { video.channel.isNotBlank() }
            ?: return
        if (channels.any { it.id == key }) return
        channels = channels + BlockedChannel(key, video.channel.ifBlank { "Unknown" })
        persist()
    }

    fun unblock(id: String) {
        channels = channels.filter { it.id != id }
        persist()
    }
}