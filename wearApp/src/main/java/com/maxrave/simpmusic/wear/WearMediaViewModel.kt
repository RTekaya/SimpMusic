package com.maxrave.simpmusic.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.Wearable
import com.maxrave.simpmusic.wear.WearConstants.CMD_NEXT
import com.maxrave.simpmusic.wear.WearConstants.CMD_PREV
import com.maxrave.simpmusic.wear.WearConstants.CMD_SEEK
import com.maxrave.simpmusic.wear.WearConstants.CMD_TOGGLE
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer

class WearMediaViewModel(application: Application) : AndroidViewModel(application) {

    private val wearApp = WearApplication.get(application)
    private val messageClient = Wearable.getMessageClient(application)
    private val nodeClient = Wearable.getNodeClient(application)

    val mediaState: StateFlow<WearMediaState> = wearApp.mediaState

    fun togglePlayPause() = sendCommand(CMD_TOGGLE)
    fun next() = sendCommand(CMD_NEXT)
    fun previous() = sendCommand(CMD_PREV)

    fun seekTo(positionMs: Long) {
        val data = ByteBuffer.allocate(8).putLong(positionMs).array()
        sendCommand(CMD_SEEK, data)
    }

    private fun sendCommand(path: String, data: ByteArray = ByteArray(0)) {
        viewModelScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, path, data).await()
                }
            } catch (_: Exception) {
                // Phone unreachable — ignore silently
            }
        }
    }
}
