package com.app.exoplanethunter.presentation.components

import com.app.exoplanethunter.exoplanet.domain.model.PlanetClassification
import com.app.exoplanethunter.testing.planet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class PlanetAppearanceTest {

    @Before
    fun fixLocale() {
        // Formatting uses the default locale; pin it so "32.9" isn't "32,9" on some machines.
        Locale.setDefault(Locale.US)
    }

    // --- Composition ------------------------------------------------------------

    @Test
    fun `earth-like density reads as rocky, inferred from density`() {
        val (composition, basis) = planet(radiusEarth = 1.0, massEarth = 1.0).composition()
        assertEquals(Composition.Rocky, composition)
        assertEquals(CompositionBasis.Density, basis)
    }

    @Test
    fun `very dense small planet reads as iron-rich`() {
        assertEquals(Composition.IronRich, planet(radiusEarth = 1.0, massEarth = 1.4).composition().first)
    }

    @Test
    fun `K2-18 b reads as water-rich`() {
        // 2.37 R⊕, 8.92 M⊕ → ~3.7 g/cm³
        assertEquals(Composition.WaterRich, planet(radiusEarth = 2.37, massEarth = 8.92).composition().first)
    }

    @Test
    fun `low-density mid-size planet reads as a gas-rich mini-Neptune`() {
        assertEquals(Composition.MiniNeptune, planet(radiusEarth = 2.5, massEarth = 4.0).composition().first)
    }

    @Test
    fun `giants split into gas giant, hot Jupiter and puffy`() {
        assertEquals(Composition.GasGiant, planet(radiusEarth = 11.2, massEarth = 317.8).composition().first)
        assertEquals(
            Composition.HotJupiter,
            planet(radiusEarth = 11.0, massEarth = 300.0, eqTempK = 1500.0).composition().first,
        )
        assertEquals(Composition.PuffyGiant, planet(radiusEarth = 20.0, massEarth = 100.0).composition().first)
    }

    @Test
    fun `radius alone follows the radius valley`() {
        fun c(r: Double) = planet(radiusEarth = r).composition()
        assertEquals(Composition.Rocky to CompositionBasis.Radius, c(1.2))
        assertEquals(Composition.RockyOrWater to CompositionBasis.Radius, c(1.8))
        assertEquals(Composition.MiniNeptune to CompositionBasis.Radius, c(3.0))
        assertEquals(Composition.IceGiant to CompositionBasis.Radius, c(5.0))
        assertEquals(Composition.GasGiant to CompositionBasis.Radius, c(10.0))
    }

    @Test
    fun `mass alone is used when radius is missing`() {
        assertEquals(Composition.RockyOrWater to CompositionBasis.Mass, planet(massEarth = 5.0).composition())
    }

    @Test
    fun `no size data means unknown`() {
        assertEquals(Composition.Unknown to CompositionBasis.None, planet().composition())
    }

    // --- Surface ----------------------------------------------------------------

    @Test
    fun `rocky surface follows temperature`() {
        fun kind(t: Double?) = planet(radiusEarth = 1.0, massEarth = 1.0, eqTempK = t).surfaceKind()
        assertEquals(SurfaceKind.Lava, kind(1500.0))
        assertEquals(SurfaceKind.Desert, kind(600.0))
        assertEquals(SurfaceKind.Temperate, kind(280.0))
        assertEquals(SurfaceKind.Frozen, kind(100.0))
        assertEquals(SurfaceKind.Barren, kind(null))
    }

    @Test
    fun `water-rich worlds are oceans when temperate and steam haze when hot`() {
        assertEquals(SurfaceKind.Ocean, planet(radiusEarth = 2.37, massEarth = 8.92, eqTempK = 284.0).surfaceKind())
        assertEquals(SurfaceKind.Haze, planet(radiusEarth = 2.37, massEarth = 8.92, eqTempK = 700.0).surfaceKind())
    }

    @Test
    fun `only cold giants with a known temperature get rings`() {
        val cold = planet(radiusEarth = 10.0, massEarth = 300.0, eqTempK = 130.0)
        assertTrue(cold.surfaceKind().hasRings(cold))

        // WASP-12 b regression: no catalogue temperature must not be treated as "cold".
        val unknown = planet(radiusEarth = 10.0, massEarth = 300.0)
        assertFalse(unknown.surfaceKind().hasRings(unknown))

        val warm = planet(radiusEarth = 10.0, massEarth = 300.0, eqTempK = 500.0)
        assertEquals(SurfaceKind.WarmGiant, warm.surfaceKind())
        assertFalse(warm.surfaceKind().hasRings(warm))
    }

    // --- Temperature estimate ---------------------------------------------------

    @Test
    fun `measured temperature always wins`() {
        assertEquals(300.0, planet(eqTempK = 300.0, insolation = 4.0).estimatedTempK()!!, 0.0)
    }

    @Test
    fun `temperature estimated from insolation`() {
        assertEquals(278.0, planet(insolation = 1.0).estimatedTempK()!!, 0.01)
    }

    @Test
    fun `temperature estimated from a sun-like star at 1 AU`() {
        val t = planet(starTempK = 5772.0, starRadiusSolar = 1.0, semiMajorAxisAu = 1.0).estimatedTempK()!!
        assertEquals(278.3, t, 0.5)
    }

    @Test
    fun `orbit size comes from Kepler's third law when only the period is known`() {
        val t = planet(starTempK = 5772.0, starRadiusSolar = 1.0, starMassSolar = 1.0, periodDays = 365.25)
            .estimatedTempK()!!
        assertEquals(278.3, t, 0.5)
    }

    @Test
    fun `WASP-12 b estimate matches the published temperature`() {
        // Published equilibrium temperature ~2,580 K.
        val t = planet(starTempK = 6300.0, starRadiusSolar = 1.6, semiMajorAxisAu = 0.023).estimatedTempK()!!
        assertEquals(2580.0, t, 60.0)
    }

    @Test
    fun `no temperature without enough data`() {
        assertNull(planet(starTempK = 5772.0).estimatedTempK())
        assertNull(planet().estimatedTempK())
    }

    // --- Derived physics --------------------------------------------------------

    @Test
    fun `earth's density and gravity`() {
        val earth = planet(radiusEarth = 1.0, massEarth = 1.0)
        assertEquals(5.51, earth.bulkDensity()!!, 0.001)
        assertEquals(1.0, earth.surfaceGravity()!!, 0.001)
    }

    @Test
    fun `density and gravity need both mass and radius`() {
        assertNull(planet(radiusEarth = 1.0).bulkDensity())
        assertNull(planet(massEarth = 1.0).surfaceGravity())
        assertNull(planet(radiusEarth = 0.0, massEarth = 1.0).surfaceGravity())
    }

    @Test
    fun `tidal locking is assumed for orbits under 10 days`() {
        assertTrue(isLikelyTidallyLocked(planet(periodDays = 6.1)))
        assertFalse(isLikelyTidallyLocked(planet(periodDays = 32.9)))
        assertFalse(isLikelyTidallyLocked(planet()))
    }

    @Test
    fun `orbital period picks a readable unit`() {
        assertEquals("12.0 h", formatOrbitalPeriod(0.5))
        assertEquals("32.9 days", formatOrbitalPeriod(32.94))
        assertEquals("365 days", formatOrbitalPeriod(365.0))
        assertEquals("11.9 yr", formatOrbitalPeriod(4332.6))
    }

    @Test
    fun `hot stars give bluer light than cool ones`() {
        val hot = starLightColor(10_000.0)
        val cool = starLightColor(3_000.0)
        assertTrue(hot.blue > cool.blue)
        assertTrue(cool.red > cool.blue)
    }

    @Test
    fun `list disc colour uses the estimated temperature when none is measured`() {
        assertEquals(planetAccentColor(278.0, null, null), planetAccentColor(planet(insolation = 1.0)))
    }

    // --- AI badge agreement -----------------------------------------------------

    @Test
    fun `AI type is shown only when it agrees with the composition`() {
        assertTrue(PlanetClassification.SUB_NEPTUNE.agreesWith(Composition.WaterRich))
        assertTrue(PlanetClassification.ROCKY.agreesWith(Composition.Rocky))
        assertTrue(PlanetClassification.GAS_GIANT.agreesWith(Composition.HotJupiter))
        // Kepler-167 e: the model's 15 R⊕ cutoff calls it Neptune-like; density says gas giant.
        assertFalse(PlanetClassification.NEPTUNE_LIKE.agreesWith(Composition.GasGiant))
        assertFalse(PlanetClassification.ROCKY.agreesWith(Composition.GasGiant))
    }

    @Test
    fun `unknown AI type is never shown, any known type is when composition is unknown`() {
        assertFalse(PlanetClassification.UNKNOWN.agreesWith(Composition.Rocky))
        assertFalse(PlanetClassification.UNKNOWN.agreesWith(Composition.Unknown))
        assertTrue(PlanetClassification.ROCKY.agreesWith(Composition.Unknown))
    }
}
