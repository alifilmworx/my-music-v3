package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mymusic.app.ui.components.AddToListSheet
import com.mymusic.app.ui.theme.Orange
import com.mymusic.app.viewmodel.MainViewModel

@Composable
fun NowPlayingScreen(viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    val track = state.currentTrack ?: run { nav.popBackStack(); return }
    var showOverflow by remember { mutableStateOf(false) }
    var showAddToList by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(Unit) {
                // swipe down to close, matching the web app's gesture
                detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 40) nav.popBackStack() }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
                }
                Text("", color = Color.White)
                Box {
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
                        DropdownMenuItem(text = { Text(if (state.liked.contains(track.id)) "Unlike" else "Like") },
                            onClick = { viewModel.toggleLike(track.id); showOverflow = false })
                        DropdownMenuItem(text = { Text("Add to list") },
                            onClick = { showOverflow = false; showAddToList = true })
                        DropdownMenuItem(text = { Text("Go to artist") },
                            onClick = { showOverflow = false; nav.navigate("collection/artist/${android.net.Uri.encode(track.artist)}") })
                        DropdownMenuItem(text = { Text("Go to album") },
                            onClick = { showOverflow = false; nav.navigate("collection/album/${android.net.Uri.encode(track.album)}") })
                    }
                }
            }

            // Blurred backdrop + sharp square card - same layered look as the web app,
            // with left/right swipe changing tracks (same gesture as the web version)
            Box(
                Modifier.weight(1f).fillMaxWidth()
                    .pointerInput(track.id) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount < -60) viewModel.next()
                            else if (dragAmount > 60) viewModel.previous()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().scale(1.15f).blur(48.dp)
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
                AsyncImage(
                    model = track.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                )
            }

            Column(Modifier.padding(24.dp)) {
                Text(track.title, color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text(track.artist, color = Color.White.copy(alpha = 0.7f), maxLines = 1)

                Spacer(Modifier.height(8.dp))
                Slider(
                    value = state.positionMs.toFloat(),
                    valueRange = 0f..(track.duration.toFloat().coerceAtLeast(1f)),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    colors = SliderDefaults.colors(thumbColor = Orange, activeTrackColor = Orange)
                )

                Row(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle", tint = if (state.shuffleOn) Orange else Color.White.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        Icon(Icons.Filled.Repeat, contentDescription = "Repeat", tint = if (state.repeatOn) Orange else Color.White.copy(alpha = 0.6f))
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleLike(track.id) }) {
                        Icon(
                            Icons.Filled.ThumbUp, contentDescription = "Like",
                            tint = if (state.liked.contains(track.id)) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { viewModel.previous() }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.togglePlay() },
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(32.dp)).background(Color.White)
                    ) {
                        Icon(
                            if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause", tint = Color.Black, modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.next() }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { viewModel.toggleDislike(track.id) }) {
                        Icon(
                            Icons.Filled.ThumbDown, contentDescription = "Dislike",
                            tint = if (state.disliked.contains(track.id)) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    if (showAddToList) {
        AddToListSheet(track = track, viewModel = viewModel, onDismiss = { showAddToList = false })
    }
}
