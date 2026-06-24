package com.app.exoplanethunter.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ===========================================================================
// Observatory Almanac typography
// Newsreader (serif) for display, Archivo (sans) for interface,
// IBM Plex Mono for all data/labels.
// ===========================================================================

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SerifFamily, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp, color = InkText
    ),
    displayMedium = TextStyle(
        fontFamily = SerifFamily, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp, color = InkText
    ),
    headlineLarge = TextStyle(
        fontFamily = SerifFamily, fontWeight = FontWeight.Medium,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = 0.sp, color = InkText
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifFamily, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp, color = InkText
    ),
    titleLarge = TextStyle(
        fontFamily = SerifFamily, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp, color = InkText
    ),
    titleMedium = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp, color = InkText
    ),
    bodyLarge = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp, color = InkText
    ),
    bodyMedium = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp, color = InkTextDim
    ),
    bodySmall = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp, color = InkTextDim
    ),
    labelLarge = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, color = InkText
    ),
    labelSmall = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp, color = InkTextFaint
    )
)

// ===========================================================================
// Reusable almanac text styles (use directly in redesigned screens)
// ===========================================================================

/** Brass small-caps eyebrow, e.g. "FIELD CATALOGUE". Apply to UPPERCASE text. */
val AlmanacEyebrow = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 2.sp, color = Brass
)

/** Muted mono section label with letter-spacing, e.g. "PHYSICAL · VS EARTH". */
val AlmanacSectionLabel = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Normal,
    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp, color = InkTextDim
)

/** Mono catalogue metadata, e.g. "EH-0042 · M-DWARF · ROCKY". */
val AlmanacMeta = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Normal,
    fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp, color = InkTextFaint
)

/** Mono tabular data value, e.g. "1.34 R⊕". */
val AlmanacData = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
    fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp, color = InkText
)

/** Large mono hero figure, e.g. the catalogue count or Earth-similarity score. */
val AlmanacHeroFigure = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
    fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp, color = Brass
)

/** Italic serif caption, e.g. "plate xii — drawn to scale". */
val AlmanacCaption = TextStyle(
    fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic,
    fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp, color = InkTextFaint
)
