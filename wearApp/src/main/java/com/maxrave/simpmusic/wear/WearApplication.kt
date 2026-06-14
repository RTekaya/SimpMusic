package com.maxrave.simpmusic.wear

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearMediaState(
    val title: String = "",
    val artist: String = "",
    val artworkUri: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleOn: Boolean = false,
    val repeatMode: Int = 0,
    val isPhoneConnected: Boolean = false,
)

class WearApplication : Application() {
    private val _mediaState = MutableStateFlow(WearMediaState())
    val mediaState: StateFlow<WearMediaState> = _mediaState.asStateFlow()

    fun updateMediaState(state: WearMediaState) {
        _mediaState.value = state
    }

    fun updateConnectionState(connected: Boolean) {
        _mediaState.value = _mediaState.value.copy(isPhoneConnected = connected)
    }

    companion object {
        fun get(context: Context): WearApplication =
            context.applicationContext as WearApplication
    }
}
