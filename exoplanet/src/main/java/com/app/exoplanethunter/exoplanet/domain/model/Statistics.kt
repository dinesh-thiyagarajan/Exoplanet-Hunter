package com.app.exoplanethunter.exoplanet.domain.model

/** Aggregate catalog statistics shown on the Statistics screen. */
data class Statistics(
    val totalPlanets: Int,
    val totalSystems: Int,
    val methodCounts: List<LabelCount>,
    val yearCounts: List<LabelCount>,
    val sizeDistribution: List<LabelCount>
)
