package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mymusic.app.data.Track
import com.mymusic.app.ui.components.AddToListSheet
import com.mymusic.app.ui.components.TrackMenuSheet
import com.mymusic.app.ui.theme.Orange
import com.mymusic.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    title: String,
    tracks: List<Track>,
    viewModel: MainViewModel,
    nav: NavHostController,
    playlistId: Long? = null // set only when viewing a custom playlist - enables "Remove from list"
) {
    var menuTrack by remember { mutableStateOf<Track?>(null) }
    var addToListTrack by remember { mutableStateOf<Track?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            item {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { if (tracks.isNotEmpty()) { viewModel.playQueue(tracks, 0); nav.navigate("nowplaying") } },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) { Text("Play") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { if (tracks.isNotEmpty()) { viewModel.playQueue(tracks.shuffled(), 0); nav.navigate("nowplaying") } }) {
                        Text("Shuffle")
                    }
                }
            }
            items(tracks) { track ->
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { viewModel.playQueue(tracks, tracks.indexOf(track)); nav.navigate("nowplaying") }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.albumArtUri, contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE0E0E0))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    IconButton(onClick = { menuTrack = track }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                }
            }
        }
    }

    menuTrack?.let { t ->
        TrackMenuSheet(
            track = t, viewModel = viewModel, currentPlaylistId = playlistId,
            onDismiss = { menuTrack = null },
            onPlayNext = { viewModel.playQueue(listOf(t), 0) },
            onAddToQueue = { viewModel.playQueue(viewModel.state.value.queue + t, viewModel.state.value.queueIndex) },
            onGoToArtist = { nav.navigate("collection/artist/${android.net.Uri.encode(t.artist)}") },
            onGoToAlbum = { nav.navigate("collection/album/${android.net.Uri.encode(t.album)}") },
            onOpenAddToList = { addToListTrack = t }
        )
    }
    addToListTrack?.let { t ->
        AddToListSheet(track = t, viewModel = viewModel, onDismiss = { addToListTrack = null })
    }
}
