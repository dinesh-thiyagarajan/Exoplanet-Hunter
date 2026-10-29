package com.app.exoplanethunter.update

/** How an available update is offered. */
enum class UpdateMode(val analyticsName: String) {
    /** Downloads in the background while the app is used; a bar then offers a restart. */
    FLEXIBLE("flexible"),

    /** Full-screen Play update the user goes through before carrying on. */
    IMMEDIATE("immediate"),
}

/**
 * Decides whether, and how, to offer an update Play says is available. Pure, so it's unit-tested.
 *
 * - Versions older than [minVersionCode] (set in Remote Config) must update: always immediate,
 *   asked again every time the app comes back, however often they decline.
 * - A release given update priority 4–5 through the Play Developer API is also immediate.
 * - Anything else is a flexible update. After a "No thanks", it isn't offered again for
 *   [DECLINE_COOLDOWN_MILLIS], so people aren't nagged on every launch.
 */
object UpdatePolicy {

    const val DECLINE_COOLDOWN_MILLIS = 3 * 24 * 60 * 60 * 1000L
    const val HIGH_PRIORITY = 4

    fun choose(
        installedVersionCode: Long,
        minVersionCode: Long,
        updatePriority: Int,
        immediateAllowed: Boolean,
        flexibleAllowed: Boolean,
        lastDeclinedMillis: Long,
        nowMillis: Long,
    ): UpdateMode? {
        val mustUpdate = installedVersionCode < minVersionCode
        if (mustUpdate && immediateAllowed) return UpdateMode.IMMEDIATE

        val recentlyDeclined = lastDeclinedMillis > 0 && nowMillis - lastDeclinedMillis < DECLINE_COOLDOWN_MILLIS
        if (recentlyDeclined && !mustUpdate) return null

        return when {
            updatePriority >= HIGH_PRIORITY && immediateAllowed -> UpdateMode.IMMEDIATE
            flexibleAllowed -> UpdateMode.FLEXIBLE
            immediateAllowed && mustUpdate -> UpdateMode.IMMEDIATE
            else -> null
        }
    }
}
