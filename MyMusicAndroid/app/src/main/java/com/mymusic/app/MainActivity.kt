package com.mymusic.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.mymusic.app.ui.MyMusicApp as MyMusicRootUi
import com.mymusic.app.ui.theme.MyMusicTheme
import com.mymusic.app.viewmodel.MainViewModel

/**
 * This activity's whole job on first launch is the ONE real permission prompt the entire
 * project was built to get: READ_MEDIA_AUDIO. Once granted, Android remembers it exactly
 * like it does for every other installed app - no re-asking on next open, no folder to
 * choose, no per-directory grants. That's the fundamental thing the web version could
 * never do, and it's just a normal system permission request here.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val audioPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.refreshLibrary() else viewModel.onPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.refreshLibrary()
        } else {
            permissionLauncher.launch(audioPermission)
        }

        setContent {
            MyMusicTheme {
                MyMusicRootUi(viewModel = viewModel, onRequestPermission = { permissionLauncher.launch(audioPermission) })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-scan every time the app comes back to the foreground, so a song downloaded
        // while the app was in the background just appears - no manual refresh needed.
        if (ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.refreshLibrary()
        }
    }
}
