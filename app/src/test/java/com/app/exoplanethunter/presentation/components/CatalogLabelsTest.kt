package com.app.exoplanethunter.presentation.components

import com.app.exoplanethunter.presentation.screens.galaxymap.scaleRingsLy
import com.app.exoplanethunter.testing.planet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CatalogLabelsTest {

    @Before
    fun fixLocale() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `temperature scale has six steps with unknown for missing data`() {
        assertEquals(TempBucket.Unknown, TempBucket.of(null))
        assertEquals(TempBucket.Frozen, TempBucket.of(100.0))
        assertEquals(TempBucket.Cold, TempBucket.of(180.0))
        assertEquals(TempBucket.Temperate, TempBucket.of(255.0))
        assertEquals(TempBucket.Temperate, TempBucket.of(320.0))
        assertEquals(TempBucket.Hot, TempBucket.of(700.0))
        assertEquals(TempBucket.Scorched, TempBucket.of(2580.0))
    }

    @Test
    fun `every temperature step has its word`() {
        assertEquals(listOf("Scorched", "Hot", "Temperate", "Cold", "Frozen", "Unknown"), TempBucket.entries.map { it.label })
        assertEquals("Temperate", temperatureLabel(288.0))
    }

    @Test
    fun `star type is shown only for real spectral classes`() {
        assertEquals("M-type", starTypeLabel("M2.5 V"))
        assertEquals("G-type", starTypeLabel(" g2v"))
        assertNull(starTypeLabel(null))
        assertNull(starTypeLabel("WD"))
    }

    @Test
    fun `size class follows radius, and says when it is unknown`() {
        assertEquals("Size unknown", classificationLabel(planet()))
        assertEquals("Earth-size", classificationLabel(planet(radiusEarth = 1.0)))
        assertEquals("Super-Earth", classificationLabel(planet(radiusEarth = 1.6)))
        assertEquals("Sub-Neptune", classificationLabel(planet(radiusEarth = 2.6)))
        assertEquals("Gas giant", classificationLabel(planet(radiusEarth = 11.0, periodDays = 400.0)))
        assertEquals("Hot Jupiter", classificationLabel(planet(radiusEarth = 11.0, periodDays = 3.0)))
    }

    @Test
    fun `distances read as light-years with one decimal only when close`() {
        assertEquals("4.2 ly", formatLightYears(1.3))
        assertEquals("124 ly", formatLightYears(38.0))
        assertEquals("1,411 ly", formatLightYears(432.6))
        assertEquals("—", formatLightYears(null))
    }

    @Test
    fun `catalogue line skips a missing star type`() {
        assertEquals("EH-0007 · Size unknown", planet(id = 7).catalogueLine())
    }

    @Test
    fun `galaxy map rings are three round distances inside the field`() {
        assertEquals(listOf(25, 50, 75), scaleRingsLy(100.0))
        assertEquals(listOf(100, 200, 300), scaleRingsLy(420.0))
        assertEquals(listOf(5, 10, 15), scaleRingsLy(8.0))
    }
}
