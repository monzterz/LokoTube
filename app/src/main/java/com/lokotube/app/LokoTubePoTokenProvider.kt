package com.lokotube.app

import android.util.Log
import com.lokotube.app.potoken.PoTokenGenerator
import org.schabi.newpipe.extractor.services.youtube.PoTokenProvider
import org.schabi.newpipe.extractor.services.youtube.PoTokenResult

class LokoTubePoTokenProvider(private val generator: PoTokenGenerator) : PoTokenProvider {
    companion object {
        private const val TAG = "LokoTubePoTokenProvider"
    }

    private fun resolvePoToken(videoId: String?): PoTokenResult? {
        videoId ?: return null
        val started = android.os.SystemClock.elapsedRealtime()
        return try {
            val sessionId = Account.visitorData() ?: ("guest_" + System.currentTimeMillis())
            val res = generator.getWebClientPoToken(videoId, sessionId)
            val took = android.os.SystemClock.elapsedRealtime() - started
            if (res == null) {
                PlaybackLog.event("poToken_none", videoId, "ms" to took)
                return null
            }
            PlaybackLog.event(
                "poToken_ok", videoId,
                "ms" to took,
                "player" to (res.playerRequestPoToken != null),
                "streaming" to (res.streamingDataPoToken != null),
            )
            PoTokenResult(
                sessionId,
                res.playerRequestPoToken,
                res.streamingDataPoToken
            )
        } catch (e: Exception) {
            PlaybackLog.event("poToken_error", videoId, "why" to e.javaClass.simpleName, "msg" to e.message?.take(100))
            Log.w(TAG, "Failed to generate PoToken for $videoId: ${e.message}")
            null
        }
    }

    override fun getWebClientPoToken(videoId: String?): PoTokenResult? = resolvePoToken(videoId)
}
