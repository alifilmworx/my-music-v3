package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
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
import com.mymusic.app.ui.theme.Orange
import com.mymusic.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    val recent = remember(state.library) { state.library.sortedByDescending { it.dateAdded }.take(5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listen Now", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Filled.Search, contentDescription = "Search") } }
            )
        },
        bottomBar = { MiniPlayerBar(viewModel, nav) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange)
                        .clickable {
                            if (state.library.isNotEmpty()) {
                                viewModel.playQueue(state.library.shuffled(), 0)
                                nav.navigate("nowplaying")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Shuffle, contentDescription = null, tint = Color.White)
                        Text("Shuffle all", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            items(recent) { track ->
                HomeTile(track) {
                    viewModel.playQueue(recent, recent.indexOf(track))
                    nav.navigate("nowplaying")
                }
            }
        }
    }
}

@Composable
private fun HomeTile(track: Track, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = null,
            modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(4.dp))
        Text(track.title, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        Text(track.artist, maxLines = 1, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MiniPlayerBar(viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    val track = state.currentTrack ?: return
    Surface(shadowElevation = 4.dp, color = Color.White) {
        Row(
            Modifier.fillMaxWidth().clickable { nav.navigate("nowplaying") }.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.albumArtUri, contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE0E0E0))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, maxLines = 1, fontWeight = FontWeight.SemiBold)
                Text(track.artist, maxLines = 1, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { viewModel.togglePlay() }) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause
                    else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }
    }
}
