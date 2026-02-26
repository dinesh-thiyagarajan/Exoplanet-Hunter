package com.app.exoplanethunter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.exoplanethunter.presentation.navigation.ExoplanetNavigation
import com.app.exoplanethunter.presentation.theme.ExoplanetHunterTheme
import com.app.exoplanethunter.presentation.theme.SpaceBlack

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
