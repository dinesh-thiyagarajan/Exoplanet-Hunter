package com.app.exoplanethunter.presentation.theme

import androidx.compose.ui.graphics.Color

// ===========================================================================
// Matcha Night palette (design handoff, "Star hug" icon)
// Dark only: a warm olive-black ground, matcha green for buttons and selection,
// orange only for stars and favourites, and temperature as the one colour scale
// that carries meaning (always shown with its word).
// ===========================================================================

// Ground
val Ink = Color(0xFF0F1009)          // page background
val Well = Color(0xFF090A05)         // bottom nav, chart and orrery wells, ad slot
val Surface = Color(0xFF191B11)      // cards, rows on cards, search field, unselected chips
val Raised = Color(0xFF24271B)       // progress and bar tracks, dialogs, secondary buttons
val Line = Color(0xFF2F3226)         // orbit lines, outlined borders, dashed boxes

// Text
val InkText = Color(0xFFF5EAD8)      // titles, key values
val InkTextDim = Color(0xFFDCD3C4)   // body copy
val InkTextFaint = Color(0xFFA19786) // labels, captions, unit lines, inactive nav

// Brand
val Matcha = Color(0xFFAEBF92)       // primary buttons, selected chips, charts, links, AI chip
val OnMatcha = Color(0xFF272E1B)     // text and icons on matcha
val MatchaDeep = Color(0xFF3D472B)   // active nav pill, toggled-on chips, update bar
val OnMatchaDeep = Color(0xFFCCDBB2) // icons and text on matcha deep
val MatchaMid = Color(0xFF8FA073)    // the planet character in the icon and splash

// Stars — used only for stars and favourites
val StarOrange = Color(0xFFF6A06B)
val StarDeep = Color(0xFFD67F48)
val StarGlow = Color(0xFF8C491A)
val StarCheek = Color(0xFFFFE1D0)

// Star colour by spectral class
val StarClassM = StarDeep
val StarClassK = StarOrange
val StarClassG = Color(0xFFFFE1D0)
val StarClassF = Color(0xFFF5EAD8)
val StarClassHot = Color(0xFFCFE3F5) // A, B, O: bluish white

// Temperature scale — always shown with its word
val TempScorched = Color(0xFFE86154)
val TempHotColor = Color(0xFFEC9845)
val TempTemperate = Color(0xFF65D68A)
val TempCold = Color(0xFF73B7DF)
val TempFrozen = Color(0xFFB6E4F0)
val TempUnknown = Color(0xFFA19786)

// ===========================================================================
// Legacy token names, repointed to Matcha Night so older call sites pick up the
// new look. Prefer the tokens above in new code.
// ===========================================================================

val InkRaised = Well
val SurfaceRaised = Raised
val Hairline = Line
val Brass = Matcha
val BrassLight = Matcha
val BrassDim = MatchaDeep
val TempHot = TempScorched

val SpaceBlack = Ink
val DeepSpaceBlue = Well
val NebulaPurple = Surface
val CosmicBlue = Raised

val StarWhite = InkText
val StarGold = StarOrange
val NebulaPink = Matcha
val CosmicCyan = Matcha
val AuroraGreen = TempTemperate
val SolarOrange = TempHotColor

val SurfaceDark = Well
val SurfaceCard = Surface
val SurfaceCardLight = Raised

val HabitableGreen = TempTemperate
val CautionYellow = TempHotColor
val HostileRed = TempScorched

// Planet temperature colours (used by the planet renderer)
val FrozenBlue = TempFrozen
val CoolBlue = TempCold
val TemperateGreen = TempTemperate
val WarmYellow = TempHotColor
val HotOrange = TempHotColor
val ScorchingRed = TempScorched

val TextPrimary = InkText
val TextSecondary = InkTextDim
val TextMuted = InkTextFaint
