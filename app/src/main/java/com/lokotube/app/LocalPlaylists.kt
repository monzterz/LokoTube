package com.lokotube.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class LocalSavedVideo(val id: String, val title: String, val channel: String, val thumbnail: String?)

data class LocalPlaylist(val id: String, val title: String, val videos: List<LocalSavedVideo>) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("videos", JSONArray().apply {
            videos.forEach { v ->
                put(JSONObject().apply {
                    put("id", v.id)
                    put("title", v.title)
                    put("channel", v.channel)
                    v.thumbnail?.let { put("thumbnail", it) }
                })
            }
        })
    }

    companion object {
        fun fromJson(o: JSONObject): LocalPlaylist {
            val vids = ArrayList<LocalSavedVideo>()
            val arr = o.optJSONArray("videos")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val v = arr.optJSONObject(i) ?: continue
                    val th = v.optString("thumbnail")
                    vids.add(LocalSavedVideo(v.optString("id"), v.optString("title"), v.optString("channel"), th.ifBlank { null }))
                }
            }
            return LocalPlaylist(o.optString("id"), o.optString("title"), vids)
        }
    }
}

/** Playlist LOKAL (terpisah penuh dari playlist akun YouTube). Disimpan sebagai JSON di SharedPreferences. */
object LocalPlaylists {
    private const val PREFS = "local_playlists"
    private const val KEY = "items"
    private var prefs: SharedPreferences? = null

    var playlists by mutableStateOf<List<LocalPlaylist>>(emptyList())
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        load()
    }

    fun create(title: String): LocalPlaylist {
        val id = "LP" + System.currentTimeMillis().toString(36)
        val pl = LocalPlaylist(id, title.trim().ifBlank { "Playlist" }, emptyList())
        playlists = playlists + pl
        persist()
        return pl
    }

    fun rename(id: String, title: String) {
        playlists = playlists.map { if (it.id == id) it.copy(title = title.trim()) else it }
        persist()
    }

    fun delete(id: String) {
        playlists = playlists.filter { it.id != id }
        persist()
    }

    fun add(id: String, video: LocalSavedVideo): Boolean {
        var changed = false
        playlists = playlists.map { pl ->
            if (pl.id == id && pl.videos.none { it.id == video.id }) {
                changed = true
                pl.copy(videos = pl.videos + video)
            } else pl
        }
        if (changed) persist()
        return changed
    }

    fun remove(id: String, videoId: String) {
        playlists = playlists.map { pl ->
            if (pl.id == id) pl.copy(videos = pl.videos.filter { it.id != videoId }) else pl
        }
        persist()
    }

    fun exportJson(): String = JSONArray().apply { playlists.forEach { put(it.toJson()) } }.toString(2)

    /** Import daftar playlist dari JSON, lewati yang ID-nya sudah ada. Return jumlah baru. */
    fun importJson(text: String): Int {
        val arr = runCatching { JSONArray(text) }.getOrNull() ?: return 0
        val incoming = ArrayList<LocalPlaylist>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            runCatching { LocalPlaylist.fromJson(o) }.getOrNull()?.let { incoming.add(it) }
        }
        val existingIds = playlists.map { it.id }.toSet()
        val newOnes = incoming.filter { it.id !in existingIds }
        if (newOnes.isNotEmpty()) {
            playlists = playlists + newOnes
            persist()
        }
        return newOnes.size
    }

    private fun load() {
        val p = prefs ?: return
        val s = p.getString(KEY, null) ?: return
        val arr = runCatching { JSONArray(s) }.getOrNull() ?: return
        val list = ArrayList<LocalPlaylist>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            runCatching { LocalPlaylist.fromJson(o) }.getOrNull()?.let { list.add(it) }
        }
        playlists = list
    }

    private fun persist() {
        val p = prefs ?: return
        val arr = JSONArray().apply { playlists.forEach { put(it.toJson()) } }
        p.edit().putString(KEY, arr.toString()).apply()
    }
}