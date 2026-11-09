package com.leplay.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Ekspor/impor daftar langganan (akun YouTube) sebagai subscriptions.csv. */
object SubscriptionTransfer {

    private val CHANNEL_ID = Regex("UC[A-Za-z0-9_-]{22}")

    /** Format kompatibel Google Takeout: "Channel Id,Channel Url,Channel title". */
    fun exportCsv(channels: List<ChannelRef>): String {
        val sb = StringBuilder("Channel Id,Channel Url,Channel title\n")
        channels.forEach { ch ->
            val id = ch.id.trim()
            if (!CHANNEL_ID.matches(id)) return@forEach
            sb.append(id)
                .append(",https://www.youtube.com/channel/").append(id)
                .append(',').append(csvField(ch.name)).append('\n')
        }
        return sb.toString()
    }

    /** Ambil semua channel ID unik dari teks (Takeout CSV, OPML longgar, atau 1 ID/URL per baris). */
    fun parseChannelIds(text: String): List<String> {
        val out = LinkedHashSet<String>()
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("Channel Id") || line.startsWith("channel id")) continue
            val m = CHANNEL_ID.find(line) ?: continue
            out.add(m.value)
        }
        return out.toList()
    }

    data class ImportResult(val added: Int, val failed: Int)

    /** Subscribe massal ke akun, lewati yang sudah dilanggani, jeda antar panggilan. */
    suspend fun import(
        ids: List<String>,
        existing: Set<String>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): ImportResult = withContext(Dispatchers.IO) {
        val todo = ids.filter { it.isNotBlank() && it !in existing }.distinct()
        var added = 0
        var failed = 0
        todo.forEachIndexed { i, id ->
            if (runCatching { InnerTube.setSubscribed(id, true) }.isSuccess) added++ else failed++
            onProgress(i + 1, todo.size)
            if (i < todo.lastIndex) delay(300L)
        }
        ImportResult(added, failed)
    }

    private fun csvField(s: String): String {
        val t = s.replace("\"", "'").trim()
        return if (t.contains(',') || t.contains('\n')) "\"$t\"" else t
    }
}