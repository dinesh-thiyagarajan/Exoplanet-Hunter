package com.app.exoplanethunter.analytics.domain.model

sealed class AnalyticsEvent {

    // ── Screen views ─────────────────────────────────────────────────────────

    object PlanetListScreenViewed : AnalyticsEvent()

    object AboutScreenViewed : AnalyticsEvent()

    object StarSystemListScreenViewed : AnalyticsEvent()

    object FavoritesScreenViewed : AnalyticsEvent()

    object StatisticsScreenViewed : AnalyticsEvent()

    object PlanetDetailScreenViewed : AnalyticsEvent()

    data class StarSystemDetailScreenViewed(
        val hostName: String
    ) : AnalyticsEvent()

    // ── Click / navigation ────────────────────────────────────────────────────

    /** Low-cardinality only: which discovery method drives engagement. No per-planet id/name. */
    data class PlanetClicked(
        val discoveryMethod: String
    ) : AnalyticsEvent()

    data class StarSystemClicked(
        val hostName: String
    ) : AnalyticsEvent()

    data class PlanetFavorited(
        val planetId: Long,
        val planetName: String
    ) : AnalyticsEvent()

    data class PlanetUnfavorited(
        val planetId: Long,
        val planetName: String
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

    object ManualSyncSuccess : AnalyticsEvent()

    data class ManualSyncFailure(
        val errorMessage: String
    ) : AnalyticsEvent()

    data class StarSystemFilterApplied(
        val filter: String
    ) : AnalyticsEvent()

    // ── Sort ────────────────────────────────────────────────────────────────

    data class PlanetSortApplied(
        val sortOption: String
    ) : AnalyticsEvent()

    // ── Compare ─────────────────────────────────────────────────────────────

    object CompareModeEntered : AnalyticsEvent()

    data class PlanetsCompared(
        val planetAId: Long,
        val planetAName: String,
        val planetBId: Long,
        val planetBName: String
    ) : AnalyticsEvent()

    data class CompareScreenViewed(
        val planetAId: Long,
        val planetBId: Long
    ) : AnalyticsEvent()

    // ── Space facts ───────────────────────────────────────────────────────────

    data class SpaceFactOpened(
        val factId: Int,
        val title: String
    ) : AnalyticsEvent()

    data class SpaceFactSourceOpened(
        val factId: Int,
        val title: String
    ) : AnalyticsEvent()

    // ── Widget ──────────────────────────────────────────────────────────────

    /** User opened a planet by tapping the Planet-of-the-Day home-screen widget. */
    object WidgetPlanetOpened : AnalyticsEvent()
}
