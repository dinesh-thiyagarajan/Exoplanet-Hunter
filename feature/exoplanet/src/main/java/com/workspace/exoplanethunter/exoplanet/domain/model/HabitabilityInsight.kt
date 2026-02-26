package com.workspace.exoplanethunter.exoplanet.domain.model

data class HabitabilityInsight(
    val overallScore: Double,
    val scores: Map<String, Double>,
    val insights: List<String>,
    val classification: PlanetClassification
)

enum class PlanetClassification(val label: String, val description: String) {
    SUB_EARTH("Sub-Earth", "Smaller than Earth, may lack atmosphere"),
    ROCKY("Rocky World", "Earth-sized rocky planet"),
    POTENTIALLY_HABITABLE("Potentially Habitable", "Earth-like conditions possible"),
    SUPER_EARTH("Super-Earth", "Larger rocky planet with thick atmosphere"),
    NEPTUNE_LIKE("Neptune-like", "Ice giant with gaseous envelope"),
    GAS_GIANT("Gas Giant", "Massive gas planet like Jupiter"),
    UNKNOWN("Unknown", "Insufficient data for classification")
}
