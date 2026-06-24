package com.app.exoplanethunter.presentation.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.app.exoplanethunter.R

/**
 * Downloadable Google Fonts for the Observatory Almanac design.
 *
 * - [SerifFamily] (Newsreader) — display: editorial headlines, planet names.
 * - [SansFamily] (Archivo) — interface: labels, body, controls.
 * - [MonoFamily] (IBM Plex Mono) — data: catalogue numbers, measurements, ticks.
 *
 * Fonts are fetched from Google Play Services on first launch and cached; until
 * then Compose falls back to the platform serif/sans/monospace.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val newsreader = GoogleFont("Newsreader")
private val archivo = GoogleFont("Archivo")
private val plexMono = GoogleFont("IBM Plex Mono")

val SerifFamily = FontFamily(
    Font(googleFont = newsreader, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = newsreader, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = newsreader, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = newsreader, fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = newsreader, fontProvider = provider, weight = FontWeight.Medium, style = FontStyle.Italic),
)

val SansFamily = FontFamily(
    Font(googleFont = archivo, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = archivo, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = archivo, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = archivo, fontProvider = provider, weight = FontWeight.Bold),
)

val MonoFamily = FontFamily(
    Font(googleFont = plexMono, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = plexMono, fontProvider = provider, weight = FontWeight.Medium),
)
