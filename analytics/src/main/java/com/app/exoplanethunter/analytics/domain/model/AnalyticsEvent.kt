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

    object GalaxyMapScreenViewed : AnalyticsEvent()

    // ── Click / navigation ────────────────────────────────────────────────────

    /** Low-cardinality only: which discovery method drives engagement. No per-planet id/name. */
    data class PlanetClicked(
        val discoveryMethod: String
    ) : AnalyticsEvent()

    data class StarSystemClicked(
        val hostName: String
    ) : AnalyticsEvent()

    /** A star tapped on the 3D galaxy map. */
    object GalaxyMapStarSelected : AnalyticsEvent()

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

    /** User switched an orrery overlay on the star system screen on or off. */
    data class OrreryToggleChanged(
        val hostName: String,
        val toggle: OrreryToggle,
        val enabled: Boolean
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

    object SpaceFactLibraryViewed : AnalyticsEvent()

    data class SpaceFactSourceOpened(
        val factId: Int,
        val title: String
    ) : AnalyticsEvent()

    // ── Widget ──────────────────────────────────────────────────────────────

    /** User opened a planet by tapping the Planet-of-the-Day home-screen widget. */
    object WidgetPlanetOpened : AnalyticsEvent()

    // ── In-app updates ──────────────────────────────────────────────────────

    /** The Play update prompt was shown. [updateType] is "flexible" or "immediate". */
    data class AppUpdatePrompted(
        val updateType: String,
        val availableVersionCode: Int
    ) : AnalyticsEvent()

    /** What the user did with it: "accepted", "declined", "failed" or "installed". */
    data class AppUpdateResult(
        val updateType: String,
        val result: String
    ) : AnalyticsEvent()
}

/** The orrery's on/off controls; [key] is what's sent to analytics. */
enum class OrreryToggle(val key: String) {
    HABITABLE_ZONE("habitable_zone"),
    SOLAR_SYSTEM("solar_system"),
    TILT_3D("3d"),
}
