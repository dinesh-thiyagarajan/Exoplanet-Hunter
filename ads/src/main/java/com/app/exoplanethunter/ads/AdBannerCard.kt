package com.app.exoplanethunter.ads

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Dark card color matching app theme (SurfaceCard = 0xFF1C2340)
private val AdCardBackground = Color(0xFF1C2340)
private val AdLabelColor = Color(0xFF666680) // TextMuted

/**
 * Whether [AdBannerCard] will draw anything. Lists check this before adding an ad item, so
 * an empty slot doesn't leave a double gap between rows.
 */
@Composable
fun bannerAdsVisible(): Boolean {
    val adsEnabled by AdManager.adsEnabledFlow.collectAsState()
    return adsEnabled && AdManager.adUnitId.isNotBlank()
}

/**
 * A few banner views shared by the ad slots of one list. Without it, every slot scrolling
 * back into view created a fresh `AdView` and fired a new ad request; with it, slot `n`
 * reuses the already-loaded banner in bucket `n % size`. Slots that share a bucket are
 * `size` slots apart, so they're never on screen together.
 */
class BannerAdPool internal constructor(private val size: Int) {
    private val views = HashMap<Int, AdView>()

    internal fun obtain(context: Context, slot: Int, adUnitId: String): AdView {
        val view = views.getOrPut(slot % size) { newBannerView(context, adUnitId) }
        // Still attached to the slot that showed it last if that slot hasn't been disposed yet.
        (view.parent as? ViewGroup)?.removeView(view)
        return view
    }

    internal fun destroy() {
        views.values.forEach { it.destroy() }
        views.clear()
    }
}

/** A [BannerAdPool] tied to the calling screen: its banners are destroyed when it leaves. */
@Composable
fun rememberBannerAdPool(size: Int = 4): BannerAdPool {
    val pool = remember { BannerAdPool(size) }
    DisposableEffect(pool) { onDispose { pool.destroy() } }
    return pool
}

private fun newBannerView(context: Context, adUnitId: String) = AdView(context).apply {
    setAdSize(AdSize.BANNER)
    this.adUnitId = adUnitId
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    )
    loadAd(AdRequest.Builder().build())
}

/**
 * A banner ad composable wrapped in a themed card that blends with the app's
 * space-themed dark UI. Shows nothing when ads are disabled via [AdManager].
 *
 * In a scrolling list, pass a [pool] and the ad's [slot] number so banners are reused
 * instead of reloaded each time the slot scrolls back into view.
 */
@Composable
fun AdBannerCard(
    modifier: Modifier = Modifier,
    pool: BannerAdPool? = null,
    slot: Int = 0,
) {
    // When ads are disabled, render nothing at all. Collected as state so a runtime
    // toggle (Remote Config) shows/hides banners without a restart.
    if (!bannerAdsVisible()) return
    val adUnitId = AdManager.adUnitId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                factory = { context ->
                    pool?.obtain(context, slot, adUnitId) ?: newBannerView(context, adUnitId)
                },
                update = { /* no-op on recomposition */ },
            )
        }
    }
}
