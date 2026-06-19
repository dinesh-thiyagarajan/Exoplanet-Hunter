package com.app.exoplanethunter.analytics.data.repository

import android.content.Context
import android.os.Bundle
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.app.exoplanethunter.analytics.data.repository.AnalyticsConstants as Keys

class FirebaseAnalyticsRepository(context: Context) : AnalyticsRepository {

    private val firebase = FirebaseAnalytics.getInstance(context)

    override fun track(event: AnalyticsEvent) {
        val (name, params) = event.toNameAndParams()
        firebase.logEvent(name, params)
    }

    private fun AnalyticsEvent.toNameAndParams(): Pair<String, Bundle?> = when (this) {

        // Screen views
        is AnalyticsEvent.PlanetListScreenViewed ->
            Keys.PLANET_LIST_SCREEN_VIEWED to null

        is AnalyticsEvent.AboutScreenViewed ->
            Keys.ABOUT_SCREEN_VIEWED to null

        is AnalyticsEvent.StarSystemListScreenViewed ->
            Keys.STAR_SYSTEM_LIST_SCREEN_VIEWED to null

        is AnalyticsEvent.FavoritesScreenViewed ->
            Keys.FAVORITES_SCREEN_VIEWED to null

        is AnalyticsEvent.StatisticsScreenViewed ->
            Keys.STATISTICS_SCREEN_VIEWED to null

        is AnalyticsEvent.PlanetDetailScreenViewed -> {
            val event = this
            Keys.PLANET_DETAIL_SCREEN_VIEWED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_ID, event.planetId)
                putString(Keys.PARAM_PLANET_NAME, event.planetName)
            }
        }

        is AnalyticsEvent.StarSystemDetailScreenViewed -> {
            val event = this
            Keys.STAR_SYSTEM_DETAIL_SCREEN_VIEWED to Bundle().apply {
                putString(Keys.PARAM_HOST_NAME, event.hostName)
            }
        }

        // Clicks / navigation
        is AnalyticsEvent.PlanetClicked -> {
            val event = this
            Keys.PLANET_CLICKED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_ID, event.planetId)
                putString(Keys.PARAM_PLANET_NAME, event.planetName)
                putString(Keys.PARAM_DISCOVERY_METHOD, event.discoveryMethod)
            }
        }

        is AnalyticsEvent.StarSystemClicked -> {
            val event = this
            Keys.STAR_SYSTEM_CLICKED to Bundle().apply {
                putString(Keys.PARAM_HOST_NAME, event.hostName)
            }
        }

        is AnalyticsEvent.PlanetFavorited -> {
            val event = this
            Keys.PLANET_FAVORITED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_ID, event.planetId)
                putString(Keys.PARAM_PLANET_NAME, event.planetName)
            }
        }

        is AnalyticsEvent.PlanetUnfavorited -> {
            val event = this
            Keys.PLANET_UNFAVORITED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_ID, event.planetId)
                putString(Keys.PARAM_PLANET_NAME, event.planetName)
            }
        }

        // Filters & Search
        is AnalyticsEvent.PlanetFilterApplied -> {
            val event = this
            Keys.PLANET_FILTER_APPLIED to Bundle().apply {
                putString(Keys.PARAM_FILTER_TYPE, event.filterType)
                putString(Keys.PARAM_FILTER_VALUE, event.filterValue)
            }
        }

        is AnalyticsEvent.PlanetSearched -> {
            val event = this
            Keys.PLANET_SEARCHED to Bundle().apply {
                putString(Keys.PARAM_QUERY, event.query)
            }
        }

        is AnalyticsEvent.ManualSyncInitiated ->
            Keys.MANUAL_SYNC_INITIATED to null

        is AnalyticsEvent.ManualSyncSuccess ->
            Keys.MANUAL_SYNC_SUCCESS to null

        is AnalyticsEvent.ManualSyncFailure -> {
            val event = this
            Keys.MANUAL_SYNC_FAILURE to Bundle().apply {
                putString(Keys.PARAM_ERROR_MESSAGE, event.errorMessage)
            }
        }

        is AnalyticsEvent.StarSystemFilterApplied -> {
            val event = this
            Keys.STAR_SYSTEM_FILTER_APPLIED to Bundle().apply {
                putString(Keys.PARAM_FILTER, event.filter)
            }
        }

        // Sort
        is AnalyticsEvent.PlanetSortApplied -> {
            val event = this
            Keys.PLANET_SORT_APPLIED to Bundle().apply {
                putString(Keys.PARAM_SORT_OPTION, event.sortOption)
            }
        }

        // Compare
        is AnalyticsEvent.CompareModeEntered ->
            Keys.COMPARE_MODE_ENTERED to null

        is AnalyticsEvent.PlanetsCompared -> {
            val event = this
            Keys.PLANETS_COMPARED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_A_ID, event.planetAId)
                putString(Keys.PARAM_PLANET_A_NAME, event.planetAName)
                putLong(Keys.PARAM_PLANET_B_ID, event.planetBId)
                putString(Keys.PARAM_PLANET_B_NAME, event.planetBName)
            }
        }

        is AnalyticsEvent.CompareScreenViewed -> {
            val event = this
            Keys.COMPARE_SCREEN_VIEWED to Bundle().apply {
                putLong(Keys.PARAM_PLANET_A_ID, event.planetAId)
                putLong(Keys.PARAM_PLANET_B_ID, event.planetBId)
            }
        }

        // Space facts
        is AnalyticsEvent.SpaceFactOpened -> {
            val event = this
            Keys.SPACE_FACT_OPENED to Bundle().apply {
                putInt(Keys.PARAM_FACT_ID, event.factId)
                putString(Keys.PARAM_FACT_TITLE, event.title)
            }
        }

        is AnalyticsEvent.SpaceFactSourceOpened -> {
            val event = this
            Keys.SPACE_FACT_SOURCE_OPENED to Bundle().apply {
                putInt(Keys.PARAM_FACT_ID, event.factId)
                putString(Keys.PARAM_FACT_TITLE, event.title)
            }
        }
    }
}
