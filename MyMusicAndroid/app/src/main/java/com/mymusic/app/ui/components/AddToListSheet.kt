package com.mymusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mymusic.app.data.Track
import com.mymusic.app.viewmodel.MainViewModel

/** Same rule as the web app: with exactly one playlist, add directly with no picker; with
 *  several, ask which one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToListSheet(track: Track, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()

    if (state.playlists.size == 1) {
        viewModel.addToPlaylist(state.playlists[0].id, track.id)
        onDismiss()
        return
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(
                "Add to playlist", fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 8.dp)
            )
            if (state.playlists.isEmpty()) {
                Text(
                    "No playlists yet — make one from the Playlists tab first",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.playlists.forEach { pl ->
                    Text(
                        pl.name,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { viewModel.addToPlaylist(pl.id, track.id); onDismiss() }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}
