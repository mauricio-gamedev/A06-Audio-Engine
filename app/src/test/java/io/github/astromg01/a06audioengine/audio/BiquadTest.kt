package io.github.astromg01.a06audioengine.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BiquadTest {
    @Test
    fun zeroDecibelPeakingFilterIsUnity() {
        val filter = Biquad()
        filter.configurePeaking(48_000, 1_000f, 1f, 0f)

        val input = floatArrayOf(0f, 0.25f, -0.5f, 0.9f, -0.1f)
        input.forEach { sample ->
            assertEquals(sample, filter.process(sample), 0.000001f)
        }
    }

    @Test
    fun peakingFilterRemainsFiniteForLongInput() {
        val filter = Biquad()
        filter.configurePeaking(48_000, 3_500f, 1f, 2.5f)
        val random = Random(6)

        repeat(100_000) {
            val sample = random.nextFloat() * 2f - 1f
            val output = filter.process(sample)
            assertTrue("Output must be finite", output.isFinite())
            assertTrue("Filter state must remain bounded", kotlin.math.abs(output) < 10f)
        }
    }

    @Test
    fun frequenciesAboveNyquistAreBypassed() {
        val filter = Biquad()
        filter.configurePeaking(16_000, 9_000f, 1f, 2f)
        assertEquals(0.4f, filter.process(0.4f), 0.000001f)
    }
}
