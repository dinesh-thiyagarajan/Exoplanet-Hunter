package com.app.exoplanethunter.analytics.domain.model

sealed class AnalyticsEvent {

    // ── Screen views ─────────────────────────────────────────────────────────

    object PlanetListScreenViewed : AnalyticsEvent()

    object AboutScreenViewed : AnalyticsEvent()

    object StarSystemListScreenViewed : AnalyticsEvent()

    data class PlanetDetailScreenViewed(
        val planetId: Long,
        val planetName: String
    ) : AnalyticsEvent()

    data class StarSystemDetailScreenViewed(
        val hostName: String
    ) : AnalyticsEvent()

    // ── Click / navigation ────────────────────────────────────────────────────

    data class PlanetClicked(
        val planetId: Long,
        val planetName: String,
        val discoveryMethod: String
    ) : AnalyticsEvent()

    data class StarSystemClicked(
        val hostName: String
    ) : AnalyticsEvent()

    // ── Filters & Search ───────────────────────────────────────────────────────

    data class PlanetFilterApplied(
        val filterType: String,
        val filterValue: String
    ) : AnalyticsEvent()

    data class PlanetSearched(
        val query: String
    ) : AnalyticsEvent()

    object ManualSyncInitiated : AnalyticsEvent()

    data class StarSystemFilterApplied(
        val filter: String
    ) : AnalyticsEvent()
}
