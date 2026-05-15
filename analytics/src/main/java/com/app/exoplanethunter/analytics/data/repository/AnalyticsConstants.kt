package com.app.exoplanethunter.analytics.data.repository

object AnalyticsConstants {
    // Event Names
    const val PLANET_LIST_SCREEN_VIEWED = "planet_list_screen_viewed"
    const val ABOUT_SCREEN_VIEWED = "about_screen_viewed"
    const val STAR_SYSTEM_LIST_SCREEN_VIEWED = "star_system_list_screen_viewed"
    const val PLANET_DETAIL_SCREEN_VIEWED = "planet_detail_screen_viewed"
    const val STAR_SYSTEM_DETAIL_SCREEN_VIEWED = "star_system_detail_screen_viewed"
    
    const val PLANET_CLICKED = "planet_clicked"
    const val STAR_SYSTEM_CLICKED = "star_system_clicked"
    
    const val PLANET_FILTER_APPLIED = "planet_filter_applied"
    const val PLANET_SEARCHED = "planet_searched"
    const val MANUAL_SYNC_INITIATED = "manual_sync_initiated"
    const val STAR_SYSTEM_FILTER_APPLIED = "star_system_filter_applied"

    // Parameter Keys
    const val PARAM_PLANET_ID = "planet_id"
    const val PARAM_PLANET_NAME = "planet_name"
    const val PARAM_HOST_NAME = "host_name"
    const val PARAM_DISCOVERY_METHOD = "discovery_method"
    const val PARAM_FILTER_TYPE = "filter_type"
    const val PARAM_FILTER_VALUE = "filter_value"
    const val PARAM_QUERY = "query"
    const val PARAM_FILTER = "filter"
}
