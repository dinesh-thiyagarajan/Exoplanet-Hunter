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
    const val FAVORITES_SCREEN_VIEWED = "favorites_screen_viewed"
    const val STATISTICS_SCREEN_VIEWED = "statistics_screen_viewed"
    const val PLANET_FAVORITED = "planet_favorited"
    const val PLANET_UNFAVORITED = "planet_unfavorited"
    
    const val PLANET_FILTER_APPLIED = "planet_filter_applied"
    const val PLANET_SEARCHED = "planet_searched"
    const val MANUAL_SYNC_INITIATED = "manual_sync_initiated"
    const val MANUAL_SYNC_SUCCESS = "manual_sync_success"
    const val MANUAL_SYNC_FAILURE = "manual_sync_failure"
    const val STAR_SYSTEM_FILTER_APPLIED = "star_system_filter_applied"

    const val PLANET_SORT_APPLIED = "planet_sort_applied"
    const val COMPARE_MODE_ENTERED = "compare_mode_entered"
    const val PLANETS_COMPARED = "planets_compared"
    const val COMPARE_SCREEN_VIEWED = "compare_screen_viewed"
    const val SPACE_FACT_OPENED = "space_fact_opened"
    const val SPACE_FACT_SOURCE_OPENED = "space_fact_source_opened"

    // Parameter Keys
    const val PARAM_PLANET_ID = "planet_id"
    const val PARAM_PLANET_NAME = "planet_name"
    const val PARAM_HOST_NAME = "host_name"
    const val PARAM_DISCOVERY_METHOD = "discovery_method"
    const val PARAM_FILTER_TYPE = "filter_type"
    const val PARAM_FILTER_VALUE = "filter_value"
    const val PARAM_QUERY = "query"
    const val PARAM_FILTER = "filter"
    const val PARAM_ERROR_MESSAGE = "error_message"
    const val PARAM_SORT_OPTION = "sort_option"
    const val PARAM_PLANET_A_ID = "planet_a_id"
    const val PARAM_PLANET_A_NAME = "planet_a_name"
    const val PARAM_PLANET_B_ID = "planet_b_id"
    const val PARAM_PLANET_B_NAME = "planet_b_name"
    const val PARAM_FACT_ID = "fact_id"
    const val PARAM_FACT_TITLE = "fact_title"
}
