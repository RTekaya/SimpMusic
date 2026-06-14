package com.maxrave.simpmusic.wear

import android.content.ComponentName
import android.os.Handler
import android.os.Looper
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.common.util.concurrent.MoreExecutors
import com.maxrave.simpmusic.wear.WearConstants.CMD_NEXT
import com.maxrave.simpmusic.wear.WearConstants.CMD_PAUSE
import com.maxrave.simpmusic.wear.WearConstants.CMD_PLAY
import com.maxrave.simpmusic.wear.WearConstants.CMD_PREV
import com.maxrave.simpmusic.wear.WearConstants.CMD_SEEK
import com.maxrave.simpmusic.wear.WearConstants.CMD_TOGGLE
import java.nio.ByteBuffer

@UnstableApi
class WearCommandListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val token = SessionToken(
            this,
            ComponentName(packageName, "com.maxrave.media3.service.SimpleMediaService"),
        )
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            try {
                val mc = future.get()
                when (messageEvent.path) {
                    CMD_TOGGLE -> if (mc.isPlaying) mc.pause() else mc.play()
                    CMD_PLAY -> mc.play()
                    CMD_PAUSE -> mc.pause()
                    CMD_NEXT -> mc.seekToNextMediaItem()
                    CMD_PREV -> mc.seekToPreviousMediaItem()
                    CMD_SEEK -> {
                        val position = ByteBuffer.wrap(messageEvent.data).long
                        mc.seekTo(position)
                    }
                }
                // Release after command is processed
                Handler(Looper.getMainLooper()).postDelayed({ mc.release() }, 500)
            } catch (_: Exception) {}
        }, MoreExecutors.directExecutor())
    }
}
