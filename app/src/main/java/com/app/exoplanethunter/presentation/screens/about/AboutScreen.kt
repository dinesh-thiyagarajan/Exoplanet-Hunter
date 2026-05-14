package com.app.exoplanethunter.presentation.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.theme.*

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.headlineLarge,
            color = CosmicCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Version 1.12.2",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Sections
        AboutSection(
            icon = Icons.Default.Dataset,
            title = stringResource(R.string.about_data_source),
            description = "All planetary data is sourced from the NASA Exoplanet Archive, maintained by Caltech/IPAC under contract with NASA. The data includes confirmed exoplanets and their host planetary systems."
        )

        Spacer(modifier = Modifier.height(16.dp))

        AboutSection(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.about_significance),
            description = "Exoplanet Hunter brings the vastness of the cosmos to your fingertips. It provides a comprehensive view of distant worlds, helping us understand our place in the universe and the potential for life beyond Earth."
        )

        Spacer(modifier = Modifier.height(16.dp))

        AboutSection(
            icon = Icons.Default.Psychology,
            title = stringResource(R.string.about_ml),
            description = "The app utilizes on-device TensorFlow Lite models to estimate habitability and classify planet types based on physical and orbital characteristics. No data ever leaves your device."
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Footer / Attribution
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Data Snapshot: Feb 2026",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Exoplanet Hunter © 2026",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun AboutSection(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CosmicCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = StarWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}
