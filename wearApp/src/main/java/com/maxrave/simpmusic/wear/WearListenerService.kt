package com.maxrave.simpmusic.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.maxrave.simpmusic.wear.WearConstants.KEY_ARTWORK
import com.maxrave.simpmusic.wear.WearConstants.KEY_ARTIST
import com.maxrave.simpmusic.wear.WearConstants.KEY_DURATION
import com.maxrave.simpmusic.wear.WearConstants.KEY_IS_PLAYING
import com.maxrave.simpmusic.wear.WearConstants.KEY_POSITION
import com.maxrave.simpmusic.wear.WearConstants.KEY_REPEAT
import com.maxrave.simpmusic.wear.WearConstants.KEY_SHUFFLE
import com.maxrave.simpmusic.wear.WearConstants.KEY_TITLE
import com.maxrave.simpmusic.wear.WearConstants.PATH_MEDIA_STATE

class WearListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val app = WearApplication.get(this)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == PATH_MEDIA_STATE
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                app.updateMediaState(
                    WearMediaState(
                        title = dataMap.getString(KEY_TITLE, ""),
                        artist = dataMap.getString(KEY_ARTIST, ""),
                        artworkUri = dataMap.getString(KEY_ARTWORK, ""),
                        isPlaying = dataMap.getBoolean(KEY_IS_PLAYING, false),
                        positionMs = dataMap.getLong(KEY_POSITION, 0L),
                        durationMs = dataMap.getLong(KEY_DURATION, 0L),
                        shuffleOn = dataMap.getInt(KEY_SHUFFLE, 0) != 0,
                        repeatMode = dataMap.getInt(KEY_REPEAT, 0),
                        isPhoneConnected = true,
                    ),
                )
            }
        }
    }

    override fun onPeerConnected(peer: Node) {
        WearApplication.get(this).updateConnectionState(true)
    }

    override fun onPeerDisconnected(peer: Node) {
        WearApplication.get(this).updateConnectionState(false)
    }
}
