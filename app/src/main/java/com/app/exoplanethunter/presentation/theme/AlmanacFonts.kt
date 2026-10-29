package com.app.exoplanethunter.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.app.exoplanethunter.R

/**
 * Downloadable Google Fonts for the Matcha Night design.
 *
 * - [HeadlineFamily] (Caprasimo) — screen titles, planet names, section titles, buttons.
 * - [BodyFamily] (Figtree) — interface, body copy and data (with tabular figures).
 *
 * Fonts are fetched from Google Play Services on first launch and cached; until
 * then Compose falls back to the platform serif/sans.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val caprasimo = GoogleFont("Caprasimo")
private val figtree = GoogleFont("Figtree")

val HeadlineFamily = FontFamily(
    Font(googleFont = caprasimo, fontProvider = provider, weight = FontWeight.Normal),
)

val BodyFamily = FontFamily(
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// Older names, kept so existing call sites compile; the monospace face is gone.
val SerifFamily = HeadlineFamily
val SansFamily = BodyFamily
val MonoFamily = BodyFamily
