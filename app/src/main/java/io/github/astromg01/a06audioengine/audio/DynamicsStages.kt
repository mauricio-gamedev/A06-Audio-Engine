package io.github.astromg01.a06audioengine.audio

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow

internal class ChannelCompressor(sampleRate: Int) {
    private val attack = exp(-1.0 / (sampleRate * 0.006)).toFloat()
    private val release = exp(-1.0 / (sampleRate * 0.090)).toFloat()
    private var envelope = 0f
    private var gain = 1f

    fun process(input: Float, profile: AudioProfile, intensity: Float): Float {
        val ratio = 1f + (profile.compressorRatio - 1f) * intensity.coerceIn(0f, 1f)
        if (ratio <= 1.001f) return input

        val level = abs(input)
        val detectorCoefficient = if (level > envelope) attack else release
        envelope = detectorCoefficient * envelope + (1f - detectorCoefficient) * level

        val threshold = 10.0.pow(profile.compressorThresholdDb / 20.0).toFloat()
        val desiredGain = if (envelope > threshold && envelope > 0f) {
            val inputOverThresholdDb = 20f * log10(envelope / threshold)
            val outputOverThresholdDb = inputOverThresholdDb / ratio
            val target = threshold * 10.0.pow(outputOverThresholdDb / 20.0).toFloat()
            (target / envelope).coerceIn(0f, 1f)
        } else {
            1f
        }

        val gainCoefficient = if (desiredGain < gain) attack else release
        gain = gainCoefficient * gain + (1f - gainCoefficient) * desiredGain
        return input * gain
    }

    fun reset() {
        envelope = 0f
        gain = 1f
    }
}

/** Fast-attack, smooth-release sample-peak limiter. No make-up gain is applied. */
internal class PeakLimiter(sampleRate: Int) {
    companion object {
        const val CEILING = 0.94f
    }

    private val attack = exp(-1.0 / (sampleRate * 0.001)).toFloat()
    private val release = exp(-1.0 / (sampleRate * 0.080)).toFloat()
    private var envelope = 0f
    private var gain = 1f

    fun process(input: Float): Float {
        val safeInput = if (input.isFinite()) input else 0f
        val level = abs(safeInput)
        val detectorCoefficient = if (level > envelope) attack else release
        envelope = detectorCoefficient * envelope + (1f - detectorCoefficient) * level

        val desiredGain = if (envelope > CEILING && envelope > 0f) {
            (CEILING / envelope).coerceIn(0f, 1f)
        } else {
            1f
        }
        val gainCoefficient = if (desiredGain < gain) attack else release
        gain = gainCoefficient * gain + (1f - gainCoefficient) * desiredGain

        return (safeInput * gain).coerceIn(-CEILING, CEILING)
    }

    fun reset() {
        envelope = 0f
        gain = 1f
    }
}
