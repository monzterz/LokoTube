package com.leplay.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.RemoteViews
import androidx.media3.common.Player

/** Home-screen mini player: title/channel + prev/play-pause/next, driven by
 *  media-button intents that route through the MediaSessionService. */
class PlayerAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context, null))
        }
    }

    companion object {
        /** Called by PlaybackService whenever playback/metadata changes. */
        fun refresh(context: Context, player: Player?) {
            val awm = AppWidgetManager.getInstance(context)
            val ids = awm.getAppWidgetIds(ComponentName(context, PlayerAppWidget::class.java))
            if (ids.isEmpty()) return
            awm.updateAppWidget(ids, buildViews(context, player))
        }

        private fun mediaButton(context: Context, requestCode: Int, keyCode: Int): PendingIntent {
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                setPackage(context.packageName)
                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun openApp(context: Context): PendingIntent {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            return PendingIntent.getActivity(
                context, 200, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun buildViews(context: Context, player: Player?): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_player)
            val title = player?.mediaMetadata?.title?.toString().orEmpty()
            val channel = player?.mediaMetadata?.artist?.toString().orEmpty()
            views.setTextViewText(R.id.widget_title, title.ifBlank { context.getString(R.string.app_name) })
            views.setTextViewText(R.id.widget_channel, channel)
            val playing = player?.isPlaying == true
            views.setImageViewResource(
                R.id.widget_play,
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            )
            views.setOnClickPendingIntent(R.id.widget_prev, mediaButton(context, 101, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
            views.setOnClickPendingIntent(R.id.widget_play, mediaButton(context, 102, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_next, mediaButton(context, 103, KeyEvent.KEYCODE_MEDIA_NEXT))
            views.setOnClickPendingIntent(R.id.widget_title, openApp(context))
            views.setOnClickPendingIntent(R.id.widget_channel, openApp(context))
            return views
        }
    }
}