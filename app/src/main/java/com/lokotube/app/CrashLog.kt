package com.lokotube.app

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Black box" crash logger. Catches uncaught exceptions, keeps the last
 * few stack traces in a private file, and lets the user share the latest
 * one on the next launch. No telemetry — files stay on the device until
 * the user explicitly shares them.
 */
object CrashLog {
    private const val DIR = "crash"
    private const val MAX_FILES = 12
    private const val PREFS = "lokotube_crash"
    private const val KEY_PENDING = "pending"

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { record(thread, throwable) }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun crashDir(context: Context): File =
        File(context.filesDir, DIR).apply { mkdirs() }

    private fun record(thread: Thread, throwable: Throwable) {
        val ctx = LokoTubeApplication.appContext
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val header = buildString {
            appendLine("LokoTube ${BuildConfig.VERSION_NAME}")
            appendLine("Time: $stamp")
            appendLine("Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
            appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("Thread: ${thread.name}")
            appendLine()
        }
        File(crashDir(ctx), "crash-${System.currentTimeMillis()}.txt")
            .writeText(header + sw.toString())
        trim(ctx)
        setPending(ctx, true)
    }

    private fun trim(context: Context) {
        val files = crashDir(context).listFiles() ?: return
        files.sortedByDescending { it.lastModified() }
            .drop(MAX_FILES)
            .forEach { it.delete() }
    }

    private fun setPending(context: Context, pending: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PENDING, pending).apply()
    }

    fun latest(context: Context): File? =
        crashDir(context).listFiles()?.maxByOrNull { it.lastModified() }

    fun hasPending(context: Context): Boolean {
        val pending = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_PENDING, false)
        return pending && latest(context) != null
    }

    fun acknowledge(context: Context) = setPending(context, false)

    fun shareLatest(context: Context): Boolean {
        val file = latest(context) ?: return false
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Kirim log crash"))
        return true
    }
}