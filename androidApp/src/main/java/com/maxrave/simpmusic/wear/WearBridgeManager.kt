package com.maxrave.simpmusic.wear

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.MoreExecutors
import com.maxrave.simpmusic.wear.WearConstants.KEY_ARTIST
import com.maxrave.simpmusic.wear.WearConstants.KEY_ARTWORK
import com.maxrave.simpmusic.wear.WearConstants.KEY_DURATION
import com.maxrave.simpmusic.wear.WearConstants.KEY_IS_PLAYING
import com.maxrave.simpmusic.wear.WearConstants.KEY_POSITION
import com.maxrave.simpmusic.wear.WearConstants.KEY_REPEAT
import com.maxrave.simpmusic.wear.WearConstants.KEY_SHUFFLE
import com.maxrave.simpmusic.wear.WearConstants.KEY_TITLE
import com.maxrave.simpmusic.wear.WearConstants.KEY_TS
import com.maxrave.simpmusic.wear.WearConstants.PATH_MEDIA_STATE

@UnstableApi
class WearBridgeManager(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private val handler = Handler(Looper.getMainLooper())
    private var controller: MediaController? = null
    private var isReleased = false

    private val positionTicker = object : Runnable {
        override fun run() {
            if (controller?.isPlaying == true) {
                pushState()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            pushState()
            if (isPlaying) handler.post(positionTicker)
            else handler.removeCallbacks(positionTicker)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) = pushState()
        override fun onPlaybackStateChanged(playbackState: Int) = pushState()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = pushState()
        override fun onRepeatModeChanged(repeatMode: Int) = pushState()
    }

    fun start() {
        if (isReleased) return
        val token = SessionToken(
            context,
            ComponentName(context.packageName, "com.maxrave.media3.service.SimpleMediaService"),
        )
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            if (isReleased) return@addListener
            try {
                val mc = future.get()
                controller = mc
                mc.addListener(playerListener)
                pushState()
                if (mc.isPlaying) handler.post(positionTicker)
                scheduleReconnectWatchdog()
            } catch (_: Exception) {
                handler.postDelayed({ start() }, RETRY_DELAY_MS)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun scheduleReconnectWatchdog() {
        handler.postDelayed({
            if (isReleased) return@postDelayed
            val mc = controller
            if (mc == null || !mc.isConnected) {
                mc?.removeListener(playerListener)
                mc?.release()
                controller = null
                start()
            } else {
                scheduleReconnectWatchdog()
            }
        }, WATCHDOG_INTERVAL_MS)
    }

    fun pushState() {
        val mc = controller ?: return
        val metadata = mc.mediaMetadata
        val req = PutDataMapRequest.create(PATH_MEDIA_STATE).apply {
            dataMap.putString(KEY_TITLE, metadata.title?.toString() ?: "")
            dataMap.putString(KEY_ARTIST, metadata.artist?.toString() ?: "")
            dataMap.putString(KEY_ARTWORK, metadata.artworkUri?.toString() ?: "")
            dataMap.putBoolean(KEY_IS_PLAYING, mc.isPlaying)
            dataMap.putLong(KEY_POSITION, mc.currentPosition)
            dataMap.putLong(KEY_DURATION, mc.duration.coerceAtLeast(0L))
            dataMap.putInt(KEY_SHUFFLE, if (mc.shuffleModeEnabled) 1 else 0)
            dataMap.putInt(KEY_REPEAT, mc.repeatMode)
            dataMap.putLong(KEY_TS, System.currentTimeMillis())
        }
        dataClient.putDataItem(req.asPutDataRequest().setUrgent())
    }

    fun release() {
        isReleased = true
        handler.removeCallbacksAndMessages(null)
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }

    companion object {
        private const val RETRY_DELAY_MS = 5_000L
        private const val WATCHDOG_INTERVAL_MS = 15_000L
    }
}
