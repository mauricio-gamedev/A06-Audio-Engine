package io.github.astromg01.a06audioengine.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** RBJ peaking-EQ biquad in transposed direct-form II. */
internal class Biquad {
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0
    private var z1 = 0.0
    private var z2 = 0.0

    fun configurePeaking(sampleRate: Int, frequencyHz: Float, q: Float, gainDb: Float) {
        if (sampleRate <= 0 || frequencyHz <= 0f || frequencyHz >= sampleRate / 2f || q <= 0f ||
            gainDb == 0f
        ) {
            setIdentity()
            return
        }

        // Keep delay state while coefficients change so profile changes are less abrupt.
        val omega = 2.0 * PI * frequencyHz / sampleRate
        val alpha = sin(omega) / (2.0 * q)
        val amplitude = 10.0.pow(gainDb / 40.0)
        val cosine = cos(omega)
        val a0 = 1.0 + alpha / amplitude

        b0 = (1.0 + alpha * amplitude) / a0
        b1 = (-2.0 * cosine) / a0
        b2 = (1.0 - alpha * amplitude) / a0
        a1 = (-2.0 * cosine) / a0
        a2 = (1.0 - alpha / amplitude) / a0
    }

    fun process(input: Float): Float {
        val x = if (input.isFinite()) input.toDouble() else 0.0
        val output = b0 * x + z1
        z1 = b1 * x - a1 * output + z2
        z2 = b2 * x - a2 * output
        return if (output.isFinite()) output.toFloat() else 0f
    }

    fun reset() {
        z1 = 0.0
        z2 = 0.0
    }

    private fun setIdentity() {
        b0 = 1.0
        b1 = 0.0
        b2 = 0.0
        a1 = 0.0
        a2 = 0.0
    }
}
