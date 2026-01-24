package com.example.myapplication.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun NetworkVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = false,
    loop: Boolean = false,
    showControls: Boolean = true
) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            playWhenReady = autoplay
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = showControls
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.useController = showControls
            view.player = exoPlayer
        }
    )
}
