package com.app.exoplanethunter.spacefacts

/**
 * A single space / exoplanet fact or theory shown in the periodic notification and the
 * fact detail screen.
 *
 * @property id              Stable identifier used in deep links and rotation bookkeeping.
 * @property title           Short headline, used as the notification title.
 * @property shortDescription One-line teaser, used as the notification body.
 * @property detail          ~100-word explanation shown on the detail screen.
 * @property sourceUrl       Wikipedia / science article opened in a Chrome Custom Tab.
 */
data class SpaceFact(
    val id: Int,
    val title: String,
    val shortDescription: String,
    val detail: String,
    val sourceUrl: String
)
