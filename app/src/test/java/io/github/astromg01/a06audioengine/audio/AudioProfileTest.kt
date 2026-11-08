package io.github.astromg01.a06audioengine.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioProfileTest {
    @Test
    fun allProfilesHaveFiveBandsAndConservativeGains() {
        AudioProfile.entries.forEach { profile ->
            assertEquals(5, profile.gainsDb.size)
            profile.gainsDb.forEach { gain ->
                assertTrue("Unexpected EQ gain in ${profile.title}: $gain", gain in -3f..2.5f)
            }
            assertTrue(profile.compressorRatio >= 1f)
        }
    }

    @Test
    fun sixDistinctProfilesAreDefined() {
        assertEquals(6, AudioProfile.entries.size)
        assertEquals(6, AudioProfile.entries.map { it.title }.toSet().size)
    }
}
