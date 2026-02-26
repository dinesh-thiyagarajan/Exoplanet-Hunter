package com.workspace.exoplanethunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.workspace.exoplanethunter.presentation.navigation.ExoplanetNavigation
import com.workspace.exoplanethunter.presentation.theme.ExoplanetHunterTheme
import com.workspace.exoplanethunter.presentation.theme.SpaceBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExoplanetHunterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpaceBlack
                ) {
                    ExoplanetNavigation()
                }
            }
        }
    }
}
