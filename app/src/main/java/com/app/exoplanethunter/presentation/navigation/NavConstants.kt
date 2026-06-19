package com.app.exoplanethunter.presentation.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val ABOUT = "about"
    const val PLANET_DETAIL = "planet_detail/{${NavArgs.PLANET_ID}}"
    const val STAR_SYSTEM_DETAIL = "star_system_detail/{${NavArgs.SYSTEM_ID}}"
    const val COMPARE = "compare/{${NavArgs.COMPARE_A}}/{${NavArgs.COMPARE_B}}"
}

object NavArgs {
    const val PLANET_ID = "planetId"
    const val SYSTEM_ID = "systemId"
    const val COMPARE_A = "compareA"
    const val COMPARE_B = "compareB"
}
