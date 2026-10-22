package com.app.exoplanethunter.presentation.screens.starsystem

import com.app.exoplanethunter.testing.planet
import com.app.exoplanethunter.testing.starSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class OrbitMechanicsTest {

    private fun bodiesOf(vararg planets: com.app.exoplanethunter.exoplanet.domain.model.Exoplanet, massSolar: Double? = 1.0) =
        buildBodies(starSystem(host = "Test", massSolar = massSolar, planets = planets.toList()))

    @Test
    fun `orbit size is derived from the period by Kepler's third law`() {
        val body = bodiesOf(planet(periodDays = 365.25)).single()
        assertEquals(1.0, body.aAu, 0.001)
        assertTrue("derived orbits are flagged so they draw dashed", body.aEstimated)
    }

    @Test
    fun `period is derived from orbit size when missing`() {
        val body = bodiesOf(planet(semiMajorAxisAu = 1.0)).single()
        assertEquals(365.25, body.periodDays, 0.5)
        assertTrue(!body.aEstimated)
    }

    @Test
    fun `heavier star means a wider orbit for the same period`() {
        val light = bodiesOf(planet(periodDays = 365.25), massSolar = 0.5).single()
        val heavy = bodiesOf(planet(periodDays = 365.25), massSolar = 2.0).single()
        assertTrue(heavy.aAu > light.aAu)
    }

    @Test
    fun `unknown star mass falls back to one solar mass`() {
        assertEquals(1.0, bodiesOf(planet(periodDays = 365.25), massSolar = null).single().aAu, 0.001)
    }

    @Test
    fun `planets with neither orbit size nor period are left out`() {
        val bodies = bodiesOf(planet(id = 1, periodDays = 10.0), planet(id = 2))
        assertEquals(listOf(1L), bodies.map { it.planet.id })
    }

    @Test
    fun `bodies are ordered from the star outwards and labelled by letter`() {
        val bodies = buildBodies(
            starSystem(
                host = "TRAPPIST-1",
                massSolar = 0.09,
                planets = listOf(
                    planet(id = 1, name = "TRAPPIST-1 e", semiMajorAxisAu = 0.029),
                    planet(id = 2, name = "TRAPPIST-1 b", semiMajorAxisAu = 0.0115),
                ),
            )
        )
        assertEquals(listOf("b", "e"), bodies.map { it.label })
    }

    @Test
    fun `a planet returns to the same spot after one period`() {
        val body = bodiesOf(planet(semiMajorAxisAu = 1.0, periodDays = 365.25, eccentricity = 0.3)).single()
        val start = body.positionAt(10.0)
        val later = body.positionAt(10.0 + body.periodDays)
        assertEquals(start.first, later.first, 1e-6)
        assertEquals(start.second, later.second, 1e-6)
    }

    @Test
    fun `eccentric orbit swings between periapsis and apoapsis`() {
        val e = 0.5
        val body = bodiesOf(planet(semiMajorAxisAu = 1.0, periodDays = 365.25, eccentricity = e)).single()
        val distances = (0 until 1000).map { i ->
            val (x, y) = body.positionAt(i * body.periodDays / 1000)
            hypot(x, y)
        }
        // The star sits at a focus: closest a(1−e), farthest a(1+e).
        assertEquals(1 - e, distances.min(), 0.01)
        assertEquals(1 + e, distances.max(), 0.01)
    }

    @Test
    fun `planet moves faster near the star (Kepler's second law)`() {
        val body = bodiesOf(planet(semiMajorAxisAu = 1.0, periodDays = 365.25, eccentricity = 0.5)).single()
        val step = body.periodDays / 2000
        val speeds = (0 until 2000).map { i ->
            val (x0, y0) = body.positionAt(i * step)
            val (x1, y1) = body.positionAt(i * step + step)
            hypot(x0, y0) to hypot(x1 - x0, y1 - y0)
        }
        val nearSpeed = speeds.minBy { it.first }.second
        val farSpeed = speeds.maxBy { it.first }.second
        // v_peri / v_apo = (1 + e) / (1 − e) = 3 for e = 0.5
        assertEquals(3.0, nearSpeed / farSpeed, 0.15)
    }

    @Test
    fun `eccentricity is clamped so orbits stay closed`() {
        assertEquals(0.95, bodiesOf(planet(semiMajorAxisAu = 1.0, eccentricity = 1.2)).single().e, 0.0)
    }

    @Test
    fun `layout is the same every time for a given planet`() {
        val a = bodiesOf(planet(id = 42, semiMajorAxisAu = 1.0)).single()
        val b = bodiesOf(planet(id = 42, semiMajorAxisAu = 1.0)).single()
        assertEquals(a.omega, b.omega, 0.0)
        assertEquals(a.m0, b.m0, 0.0)
    }

    @Test
    fun `the Sun's habitable zone matches Kopparapu's 0_99 to 1_70 AU`() {
        val hz = habitableZone(starSystem(tempK = 5772.0, radiusSolar = 1.0))!!
        assertEquals(0.98, hz.start, 0.02)
        assertEquals(1.69, hz.endInclusive, 0.02)
        assertTrue(1.0 in hz)
    }

    @Test
    fun `TRAPPIST-1's habitable zone holds e, f and g but not d or h`() {
        // As reported in the discovery paper (Gillon et al. 2017).
        val hz = habitableZone(starSystem(tempK = 2566.0, radiusSolar = 0.119))!!
        assertTrue("e", 0.0293 in hz)
        assertTrue("f", 0.0385 in hz)
        assertTrue("g", 0.0469 in hz)
        assertTrue("d", 0.0223 !in hz)
        assertTrue("h", 0.0619 !in hz)
    }

    @Test
    fun `no habitable zone without the star's size and temperature`() {
        assertNull(habitableZone(starSystem(tempK = 5772.0)))
        assertNull(habitableZone(starSystem(radiusSolar = 1.0)))
    }

    // --- Multiple stars ---------------------------------------------------------

    @Test
    fun `a single star has no companions`() {
        val sys = starSystem(numStars = 1, planets = listOf(planet()))
        assertTrue(!isCircumbinary(sys))
        assertEquals(emptyList<String>(), companionLabels(sys))
    }

    @Test
    fun `an ordinary binary has one distant companion, B`() {
        val sys = starSystem(host = "HD 189733", numStars = 2, planets = listOf(planet(name = "HD 189733 b")))
        assertTrue(!isCircumbinary(sys))
        assertEquals(listOf("B"), companionLabels(sys))
    }

    @Test
    fun `when the host is star B, its companion is A`() {
        val sys = starSystem(host = "16 Cyg B", numStars = 3, planets = listOf(planet(name = "16 Cyg B b")))
        assertEquals(listOf("A", "C"), companionLabels(sys))
    }

    @Test
    fun `AB in the planet name means the planets orbit both stars`() {
        val sys = starSystem(host = "HIP 79098", numStars = 2, planets = listOf(planet(name = "HIP 79098 AB b")))
        assertTrue(isCircumbinary(sys))
        assertEquals("both stars are drawn in the centre", emptyList<String>(), companionLabels(sys))
    }

    @Test
    fun `Kepler-16 is circumbinary even though its name doesn't say so`() {
        assertTrue(isCircumbinary(starSystem(host = "Kepler-16", numStars = 2, planets = listOf(planet(name = "Kepler-16 b")))))
    }

    @Test
    fun `a circumbinary trinary keeps the pair central and marks star C`() {
        val sys = starSystem(host = "Kepler-64", numStars = 4, planets = listOf(planet(name = "Kepler-64 b")))
        assertEquals(listOf("C", "D"), companionLabels(sys))
    }
}
