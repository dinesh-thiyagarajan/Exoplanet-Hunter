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

        is AnalyticsEvent.StarSystemFilterApplied -> {
            val event = this
            Keys.STAR_SYSTEM_FILTER_APPLIED to Bundle().apply {
                putString(Keys.PARAM_FILTER, event.filter)
            }
        }
    }
}
