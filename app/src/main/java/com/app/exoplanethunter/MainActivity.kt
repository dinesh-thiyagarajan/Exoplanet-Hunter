package com.app.exoplanethunter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.app.exoplanethunter.presentation.navigation.ExoplanetNavigation
import com.app.exoplanethunter.presentation.theme.ExoplanetHunterTheme
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.spacefacts.SpaceFactNotifier

class MainActivity : ComponentActivity() {

    // Holds a space-fact id when the activity is (re)launched from its notification.
    private var pendingFactId by mutableStateOf<Int?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingFactId = intent.factId()
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            ExoplanetHunterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpaceBlack
                ) {
                    ExoplanetNavigation(
                        initialFactId = pendingFactId,
                        onFactConsumed = { pendingFactId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.factId()?.let { pendingFactId = it }
    }

    private fun Intent.factId(): Int? {
        val id = getIntExtra(SpaceFactNotifier.EXTRA_FACT_ID, -1)
        return if (id >= 0) id else null
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
