package com.mymusic.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mymusic.app.data.Track
import com.mymusic.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/** Mirrors #track-menu from the web app: Play next, Add to queue, Like, Add to list,
 *  Go to artist, Go to album, Remove from list (only inside a playlist you're viewing),
 *  Delete. Shown as a bottom sheet, which is the native equivalent of the web version's
 *  slide-up context menu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackMenuSheet(
    track: Track,
    viewModel: MainViewModel,
    currentPlaylistId: Long?, // set when viewing a specific playlist's contents - enables "Remove from list"
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onGoToArtist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onOpenAddToList: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column {
            MenuRow("Play next") { onPlayNext(); onDismiss() }
            MenuRow("Add to queue") { onAddToQueue(); onDismiss() }
            MenuRow(if (state.liked.contains(track.id)) "Unlike" else "Like") {
                viewModel.toggleLike(track.id); onDismiss()
            }
            MenuRow("Add to list") { onOpenAddToList(); onDismiss() }
            if (currentPlaylistId != null) {
                MenuRow("Remove from list") {
                    scope.launch { viewModel.removeFromPlaylist(currentPlaylistId, track.id) }
                    onDismiss()
                }
            }
            MenuRow("Go to artist") { onGoToArtist(); onDismiss() }
            MenuRow("Go to album") { onGoToAlbum(); onDismiss() }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}
