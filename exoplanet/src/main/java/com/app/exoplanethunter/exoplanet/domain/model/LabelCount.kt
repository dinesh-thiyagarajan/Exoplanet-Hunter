package com.app.exoplanethunter.exoplanet.domain.model

/** A generic labelled tally, used for statistics breakdowns (by method, year, size class, …). */
data class LabelCount(
    val label: String,
    val count: Int
)
