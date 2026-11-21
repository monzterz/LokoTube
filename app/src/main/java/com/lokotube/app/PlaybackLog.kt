package com.lokotube.app

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Playback "black box": a rolling, on-device record of why a stream failed, kept
 * next to the crash log so a user can send one file that explains a playback error.
 *
 * The point is to answer the questions a screenshot cannot: which HTTP status the
 * media host returned, which client produced the URL, whether a PoToken was part
 * of it, and whether the automatic recovery path retried or gave up. Nothing
 * leaves the device unless the user shares the file.
 */
object PlaybackLog {
    private const val DIR = "playback"
    private const val FILE = "playback.log"
    private const val MAX_LINES = 400
    private const val MAX_BYTES = 192 * 1024

    private val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val lock = Any()

    private fun dir(context: Context): File = File(context.filesDir, DIR).apply { mkdirs() }

    private fun file(context: Context): File = File(dir(context), FILE)

    fun event(kind: String, videoId: String?, vararg fields: Pair<String, Any?>) {
        val ctx = runCatching { LokoTubeApplication.appContext }.getOrNull() ?: return
        val line = buildString {
            append(stamp.format(Date()))
            append(' ').append(kind)
            if (videoId != null) append(" video=").append(videoId)
            for ((key, value) in fields) {
                if (value == null) continue
                append(' ').append(key).append('=').append(value)
            }
        }
        synchronized(lock) {
            runCatching {
                val f = file(ctx)
                f.appendText(line + "\n")
                if (f.length() > MAX_BYTES) trim(ctx, f)
            }
        }
    }

    private fun trim(context: Context, f: File) {
        val lines = runCatching { f.readLines() }.getOrDefault(emptyList())
        if (lines.size > MAX_LINES) {
            f.writeText(lines.takeLast(MAX_LINES).joinToString("\n") + "\n")
        }
    }

    fun latest(context: Context): File? = file(context).takeIf { it.exists() && it.length() > 0 }

    fun shareLatest(context: Context): Boolean {
        val f = latest(context) ?: return false
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Kirim log pemutaran"))
        return true
    }

    /**
     * Signed media URLs are long and full of personal identifiers; only the parts that
     * explain a refusal are kept: the host, the itag, the client marker and whether the
     * URL carried a PoToken / an expiry.
     */
    fun describeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return runCatching {
            val q = url.substringAfter('?', "")
            fun param(name: String) = Regex("(?:^|&)" + name + "=([^&]*)").find(q)?.groupValues?.get(1)
            val host = url.substringAfter("://").substringBefore('/')
            val itag = url.substringAfter("/itag/", "").substringBefore('/').takeIf { it.isNotEmpty() }
            buildString {
                append(host)
                if (itag != null) append(" itag=").append(itag)
                param("c")?.let { append(" c=").append(it) }
                append(" pot=").append(if (param("pot") != null) "yes" else "no")
                param("expire")?.toLongOrNull()?.let { exp ->
                    val left = exp * 1000 - System.currentTimeMillis()
                    append(" expireIn=").append(left / 60_000).append("m")
                }
                append(" mime=").append(param("mime")?.substringBefore(';')?.let { java.net.URLDecoder.decode(it, "UTF-8") })
            }
        }.getOrNull()
    }
}
