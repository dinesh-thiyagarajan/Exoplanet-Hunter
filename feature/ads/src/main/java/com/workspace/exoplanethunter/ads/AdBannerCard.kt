package com.workspace.exoplanethunter.ads

import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Dark card color matching app theme (SurfaceCard = 0xFF1C2340)
private val AdCardBackground = Color(0xFF1C2340)
private val AdLabelColor = Color(0xFF666680) // TextMuted

/**
 * A banner ad composable wrapped in a themed card that blends with the app's
 * space-themed dark UI. Shows nothing when ads are disabled via [AdManager].
 */
@Composable
fun AdBannerCard(modifier: Modifier = Modifier) {
    // When ads are disabled, render nothing at all
    if (!AdManager.adsEnabled) return

    val adUnitId = AdManager.adUnitId
    if (adUnitId.isBlank()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle "Ad" watermark shown behind the ad
            Text(
                text = "Ad",
                color = AdLabelColor.copy(alpha = 0.3f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp)
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { /* no-op on recomposition */ }
            )
        }
    }
}
