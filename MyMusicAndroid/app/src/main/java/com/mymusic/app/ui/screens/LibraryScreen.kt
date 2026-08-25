package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import kotlinx.coroutines.launch

/* Same tab order as the web app: Playlists, Artists, Albums, Songs, Genres, Folders */
private val TABS = listOf("Playlists", "Artists", "Albums", "Songs", "Genres", "Folders")

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { TABS.size })
    val scope = rememberCoroutineScope()

    Scaffold(bottomBar = { MiniPlayerBar(viewModel, nav) }) { padding ->
        Column(Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = pagerState.currentPage, containerColor = Orange, contentColor = Color.White) {
                TABS.forEachIndexed { i, label ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                        text = { Text(label) }
                    )
                }
            }

            // HorizontalPager gives the same swipe-between-tabs gesture the web app has
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (TABS[page]) {
                    "Playlists" -> PlaylistsTab(state.library, viewModel, nav)
                    "Artists" -> GroupedList(state.library.groupBy { it.artist }, "artist", viewModel, nav)
                    "Albums" -> GroupedList(state.library.groupBy { it.album }, "album", viewModel, nav)
                    "Songs" -> SongList(state.library.sortedByDescending { it.dateAdded }, viewModel, nav)
                    "Genres" -> GroupedList(state.library.groupBy { it.genre ?: "Unknown genre" }, "genre", viewModel, nav)
                    "Folders" -> GroupedList(state.library.groupBy { it.folder }, "folder", viewModel, nav)
                }
            }
        }
    }
}

@Composable
private fun SongList(tracks: List<Track>, viewModel: MainViewModel, nav: NavHostController) {
    LazyColumn {
        items(tracks) { track ->
            TrackRow(track) {
                viewModel.playQueue(tracks, tracks.indexOf(track))
                nav.navigate("nowplaying")
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArtUri, contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE0E0E0))
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(track.title, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

/** Used for Artists / Albums / Genres / Folders - tapping a group opens its full track list,
 *  matching the web app's detail page rather than playing immediately. */
@Composable
private fun GroupedList(groups: Map<String, List<Track>>, type: String, viewModel: MainViewModel, nav: NavHostController) {
    LazyColumn {
        items(groups.keys.sorted()) { key ->
            val tracks = groups[key].orEmpty()
            Row(
                Modifier.fillMaxWidth()
                    .clickable { nav.navigate("collection/$type/${android.net.Uri.encode(key)}") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = tracks.firstOrNull()?.albumArtUri, contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE0E0E0))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(key, fontWeight = FontWeight.Medium)
                    Text("${tracks.size} songs", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistsTab(library: List<Track>, viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.playlists) {
        state.playlists.forEach { viewModel.watchPlaylist(it.id) }
    }
    val playlistTrackIds by viewModel.playlistTrackIds.collectAsState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            item {
                val liked = library.filter { state.liked.contains(it.id) }
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { nav.navigate("liked") }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) { Text("Thumbs up (${liked.size})", fontWeight = FontWeight.Medium) }
            }
            items(state.playlists) { playlist ->
                val ids = playlistTrackIds[playlist.id].orEmpty()
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { nav.navigate("playlist/${playlist.id}") }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(playlist.name, fontWeight = FontWeight.Medium)
                        Text("${ids.size} songs", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { pendingDelete = playlist.id }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete playlist")
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Orange,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "New playlist", tint = Color.White) }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New playlist") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Playlist name") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) viewModel.createPlaylist(name.trim())
                    showCreateDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }

    pendingDelete?.let { id ->
        val name = state.playlists.find { it.id == id }?.name ?: ""
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this playlist?") },
            text = { Text("\"$name\" will be deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePlaylist(id); pendingDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}
