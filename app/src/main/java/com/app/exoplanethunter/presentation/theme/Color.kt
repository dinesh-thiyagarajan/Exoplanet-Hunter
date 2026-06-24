package com.app.exoplanethunter.presentation.theme

import androidx.compose.ui.graphics.Color

// ===========================================================================
// Observatory Almanac palette
// An observatory logbook: ink canvas, brass as the single decorative colour,
// a warm text ramp, and temperature as the only semantic encoding.
// ===========================================================================

// Foundation
val Ink = Color(0xFF0E1319)          // canvas / background
val InkRaised = Color(0xFF11161E)    // nav bars, sunken areas
val Surface = Color(0xFF161C24)      // raised card
val SurfaceRaised = Color(0xFF1E2630) // elevated fills, track backgrounds
val Hairline = Color(0xFF2A313C)     // dividers, rules, borders

// Brand — the ONLY decorative colour. Used once per view.
val Brass = Color(0xFFC68A3E)
val BrassDim = Color(0xFF8C6026)

// Text ramp (warm)
val InkText = Color(0xFFECE7DA)      // primary
val InkTextDim = Color(0xFF9A9488)   // secondary
val InkTextFaint = Color(0xFF5E5C54) // muted / captions

// Semantic — encodes temperature only
val TempTemperate = Color(0xFF6F9D5A)
val TempHot = Color(0xFFC0524A)
val TempCold = Color(0xFF5B7CA0)
val TempUnknown = Color(0xFF8B8275)

// ===========================================================================
// Legacy token names — repointed to the almanac palette so existing screens
// keep compiling while they are migrated. Prefer the tokens above for new work.
// ===========================================================================

val SpaceBlack = Ink
val DeepSpaceBlue = InkRaised
val NebulaPurple = Surface
val CosmicBlue = SurfaceRaised

val StarWhite = InkText
val StarGold = Brass
val NebulaPink = Brass
val CosmicCyan = Brass
val AuroraGreen = TempTemperate
val SolarOrange = Brass

val SurfaceDark = InkRaised
val SurfaceCard = Surface
val SurfaceCardLight = SurfaceRaised

val HabitableGreen = TempTemperate
val CautionYellow = Brass
val HostileRed = TempHot

// Planet temperature colours (used by the 3D renderer)
val FrozenBlue = TempCold
val CoolBlue = Color(0xFF6E96B0)
val TemperateGreen = TempTemperate
val WarmYellow = Brass
val HotOrange = Color(0xFFC0703E)
val ScorchingRed = TempHot

val TextPrimary = InkText
val TextSecondary = InkTextDim
val TextMuted = InkTextFaint
