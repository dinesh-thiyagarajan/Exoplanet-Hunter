package com.app.exoplanethunter.analytics.data.repository

import android.content.Context
import android.os.Bundle
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsRepository(context: Context) : AnalyticsRepository {

    private val firebase = FirebaseAnalytics.getInstance(context)

    override fun track(event: AnalyticsEvent) {
        val (name, params) = event.toNameAndParams()
        firebase.logEvent(name, params)
    }

    private fun AnalyticsEvent.toNameAndParams(): Pair<String, Bundle?> = when (this) {

        // Screen views
        is AnalyticsEvent.PlanetListScreenViewed ->
            "planet_list_screen_viewed" to null

        is AnalyticsEvent.StarSystemListScreenViewed ->
            "star_system_list_screen_viewed" to null

        is AnalyticsEvent.PlanetDetailScreenViewed ->
            "planet_detail_screen_viewed" to Bundle().apply {
                putLong("planet_id", planetId)
                putString("planet_name", planetName)
            }

        is AnalyticsEvent.StarSystemDetailScreenViewed ->
            "star_system_detail_screen_viewed" to Bundle().apply {
                putString("host_name", hostName)
            }

        // Clicks / navigation
        is AnalyticsEvent.PlanetClicked ->
            "planet_clicked" to Bundle().apply {
                putLong("planet_id", planetId)
                putString("planet_name", planetName)
                putString("discovery_method", discoveryMethod)
            }

        is AnalyticsEvent.StarSystemClicked ->
            "star_system_clicked" to Bundle().apply {
                putString("host_name", hostName)
            }

        // Filters
        is AnalyticsEvent.PlanetFilterApplied ->
            "planet_filter_applied" to Bundle().apply {
                putString("filter_type", filterType)
                putString("filter_value", filterValue)
            }

        is AnalyticsEvent.StarSystemFilterApplied ->
            "star_system_filter_applied" to Bundle().apply {
                putString("filter", filter)
            }
    }
}
