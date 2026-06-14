package com.maxrave.simpmusic.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import coil3.compose.AsyncImage
import com.maxrave.simpmusic.wear.WearMediaState
import com.maxrave.simpmusic.wear.WearMediaViewModel

@Composable
fun WearApp(viewModel: WearMediaViewModel) {
    val state by viewModel.mediaState.collectAsState()
    MaterialTheme {
        MediaControlScreen(
            state = state,
            onTogglePlayPause = viewModel::togglePlayPause,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
        )
    }
}

@Composable
fun MediaControlScreen(
    state: WearMediaState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (!state.isPhoneConnected && state.title.isEmpty()) {
            NotConnectedView()
        } else {
            PlayerView(
                state = state,
                onTogglePlayPause = onTogglePlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
            )
        }
    }
}

@Composable
private fun PlayerView(
    state: WearMediaState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Artwork
        Box(contentAlignment = Alignment.Center) {
            if (state.artworkUri.isNotEmpty()) {
                AsyncImage(
                    model = state.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DB954)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♪", color = Color.White, fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = state.title.ifEmpty { "Nothing playing" },
            style = MaterialTheme.typography.title3,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Artist
        if (state.artist.isNotEmpty()) {
            Text(
                text = state.artist,
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        if (state.durationMs > 0) {
            CircularProgressIndicator(
                progress = (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.size(8.dp),
                strokeWidth = 2.dp,
                indicatorColor = Color(0xFF1DB954),
                trackColor = Color(0xFF333333),
            )
            Spacer(modifier = Modifier.height(2.dp))
            val elapsed = formatTime(state.positionMs)
            val total = formatTime(state.durationMs)
            Text(
                text = "$elapsed / $total",
                style = MaterialTheme.typography.caption3,
                color = Color.Gray,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Playback controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Previous
            Button(
                onClick = onPrevious,
                modifier = Modifier.size(36.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF222222)),
            ) {
                Text("⏮", color = Color.White, fontSize = 14.sp)
            }

            // Play / Pause
            Button(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DB954)),
            ) {
                Text(if (state.isPlaying) "⏸" else "▶", color = Color.Black, fontSize = 18.sp)
            }

            // Next
            Button(
                onClick = onNext,
                modifier = Modifier.size(36.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF222222)),
            ) {
                Text("⏭", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun NotConnectedView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📵", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Open SimpMusic\non your phone",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
