package com.app.exoplanethunter.spacefacts

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/** Opens a URL in a Chrome Custom Tab, falling back to the default browser if needed. */
object CustomTabLauncher {

    fun open(context: Context, url: String) {
        val uri = Uri.parse(url)
        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, uri)
        } catch (e: ActivityNotFoundException) {
            // No browser capable of Custom Tabs — fall back to a plain view intent.
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        }
    }
}
