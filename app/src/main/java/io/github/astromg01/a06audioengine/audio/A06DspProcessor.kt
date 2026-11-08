package io.github.astromg01.a06audioengine.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * In-app PCM processor. It is attached to the A06 Audio Engine's Media3 AudioSink;
 * it does not intercept audio from other applications.
 */
@UnstableApi
class A06DspProcessor : BaseAudioProcessor() {
    private data class Settings(
        val profile: AudioProfile,
        val intensity: Float,
        val enabled: Boolean,
        val revision: Long
    )

    @Volatile
    private var settings = Settings(AudioProfile.DEFAULT, 1f, true, 0L)

    private var activeRevision = -1L
    private var activeSampleRate = 0
    private var activeChannels = 0
    private var filters: Array<Array<Biquad>> = emptyArray()
    private var compressors: Array<ChannelCompressor> = emptyArray()
    private var limiters: Array<PeakLimiter> = emptyArray()

    @Synchronized
    fun update(profile: AudioProfile, intensity: Float, enabled: Boolean) {
        val old = settings
        settings = Settings(
            profile = profile,
            intensity = intensity.coerceIn(0f, 1f),
            enabled = enabled,
            revision = old.revision + 1L
        )
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val supportedEncoding = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT ||
            inputAudioFormat.encoding == C.ENCODING_PCM_FLOAT
        val validFormat = inputAudioFormat.sampleRate >= 8_000 &&
            inputAudioFormat.channelCount in 1..8
        return if (supportedEncoding && validFormat) inputAudioFormat else AudioProcessor.AudioFormat.NOT_SET
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return

        val format = inputAudioFormat
        val snapshot = settings
        ensureState(format, snapshot)

        val bytesPerSample = if (format.encoding == C.ENCODING_PCM_FLOAT) 4 else 2
        val frameBytes = bytesPerSample * format.channelCount
        val byteCount = inputBuffer.remaining()
        val output = replaceOutputBuffer(byteCount).order(ByteOrder.nativeOrder())
        inputBuffer.order(ByteOrder.nativeOrder())

        while (inputBuffer.remaining() >= frameBytes) {
            for (channel in 0 until format.channelCount) {
                val sample = if (format.encoding == C.ENCODING_PCM_FLOAT) {
                    inputBuffer.getFloat()
                } else {
                    inputBuffer.getShort() / 32768f
                }

                var processed = sample
                if (snapshot.enabled) {
                    for (filter in filters[channel]) processed = filter.process(processed)
                    processed = compressors[channel].process(
                        processed,
                        snapshot.profile,
                        snapshot.intensity
                    )
                }
                processed = limiters[channel].process(processed)

                if (format.encoding == C.ENCODING_PCM_FLOAT) {
                    output.putFloat(processed)
                } else {
                    val quantized = (processed * 32768f).toInt().coerceIn(-32768, 32767)
                    output.putShort(quantized.toShort())
                }
            }
        }

        // PCM buffers are frame-aligned in normal operation. Preserve any unexpected tail bytes.
        while (inputBuffer.hasRemaining()) output.put(inputBuffer.get())
        output.flip()
    }

    override fun onReset() {
        activeRevision = -1L
        activeSampleRate = 0
        activeChannels = 0
        filters = emptyArray()
        compressors = emptyArray()
        limiters = emptyArray()
    }

    private fun ensureState(format: AudioProcessor.AudioFormat, snapshot: Settings) {
        if (activeRevision == snapshot.revision &&
            activeSampleRate == format.sampleRate &&
            activeChannels == format.channelCount
        ) return

        activeRevision = snapshot.revision
        activeSampleRate = format.sampleRate
        activeChannels = format.channelCount

        filters = Array(format.channelCount) {
            Array(AudioProfile.BAND_FREQUENCIES_HZ.size) { band ->
                Biquad().apply {
                    configurePeaking(
                        format.sampleRate,
                        AudioProfile.BAND_FREQUENCIES_HZ[band],
                        AudioProfile.BAND_Q[band],
                        if (snapshot.enabled) {
                            snapshot.profile.gainsDb[band] * snapshot.intensity
                        } else {
                            0f
                        }
                    )
                }
            }
        }
        compressors = Array(format.channelCount) { ChannelCompressor(format.sampleRate) }
        limiters = Array(format.channelCount) { PeakLimiter(format.sampleRate) }
    }
}
