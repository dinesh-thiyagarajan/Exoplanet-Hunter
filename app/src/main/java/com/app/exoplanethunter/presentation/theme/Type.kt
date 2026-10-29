package com.app.exoplanethunter.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ===========================================================================
// Matcha Night typography
// Caprasimo for headlines, Figtree for everything else. Data uses Figtree with
// tabular figures so columns of numbers line up. No monospace, no caps labels.
// ===========================================================================

private const val TABULAR = "tnum"

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
        fontSize = 38.sp, lineHeight = 42.sp, color = InkText
    ),
    displayMedium = TextStyle(
        fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, lineHeight = 38.sp, color = InkText
    ),
    headlineLarge = TextStyle(
        fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 32.sp, color = InkText
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
        fontSize = 22.sp, lineHeight = 26.sp, color = InkText
    ),
    titleLarge = TextStyle(
        fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 24.sp, color = InkText
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 22.sp, color = InkText
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, color = InkText
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, color = InkTextDim
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp, color = InkTextDim
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp, color = InkTextFaint
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, color = InkText
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, color = InkText
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, color = InkTextFaint
    )
)

// ===========================================================================
// Shared text styles
// ===========================================================================

/** Screen title on the tab screens, e.g. "Planets". */
val ScreenTitle = TextStyle(
    fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
    fontSize = 34.sp, lineHeight = 38.sp, color = InkText
)

/** Section title inside a screen, e.g. "Size to scale". */
val SectionTitle = TextStyle(
    fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
    fontSize = 22.sp, lineHeight = 26.sp, color = InkText
)

/** Card title, e.g. the host star name. */
val CardTitle = TextStyle(
    fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
    fontSize = 20.sp, lineHeight = 24.sp, color = InkText
)

/** Button label, Caprasimo 16. */
val ButtonText = TextStyle(
    fontFamily = HeadlineFamily, fontWeight = FontWeight.Normal,
    fontSize = 16.sp, lineHeight = 20.sp
)

/** Row name in lists, e.g. "K2-18 b". */
val RowName = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
    fontSize = 16.sp, lineHeight = 21.sp, color = InkText
)

/** Small caps label, kept only for "PLANET OF THE DAY". Apply to UPPERCASE text. */
val AlmanacEyebrow = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.9.sp, color = Matcha
)

/** Label above a value or chart, e.g. "Est. temp". */
val AlmanacSectionLabel = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.Medium,
    fontSize = 13.sp, lineHeight = 18.sp, color = InkTextFaint
)

/** Secondary line and captions, e.g. "EH-0412 · M-type · Sub-Neptune". */
val AlmanacMeta = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, lineHeight = 18.sp, color = InkTextFaint
)

/** Tabular data value, e.g. "1.34 R⊕". */
val AlmanacData = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
    fontSize = 15.sp, lineHeight = 20.sp, color = InkText, fontFeatureSettings = TABULAR
)

/** Hero figure, e.g. "≈ 255 K" on planet detail. */
val HeroFigure = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 26.sp, lineHeight = 30.sp, color = InkText, fontFeatureSettings = TABULAR
)

/** Big stat number, e.g. the catalogue count on Stats. */
val AlmanacHeroFigure = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 34.sp, lineHeight = 38.sp, color = Matcha, fontFeatureSettings = TABULAR
)

/** Small caption, e.g. "Artist's impression · Drag to spin". */
val AlmanacCaption = TextStyle(
    fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
    fontSize = 12.sp, lineHeight = 16.sp, color = InkTextFaint
)
