package io.github.astromg01.a06audioengine.audio

/**
 * Conservative speaker-oriented profiles.
 *
 * Gains are intentionally modest: the A06's small speaker cannot reproduce deep bass,
 * and aggressive boosts waste headroom and increase distortion.
 */
enum class AudioProfile(
    val title: String,
    val description: String,
    val gainsDb: FloatArray,
    val compressorThresholdDb: Float,
    val compressorRatio: Float
) {
    NATURAL(
        "Natural",
        "Resposta discreta, preservando a gravação.",
        floatArrayOf(-0.3f, 0f, 0f, 0.2f, 0f),
        -18f,
        1.0f
    ),
    VOICE_CLARITY(
        "Voice Clarity",
        "Reduz um pouco o excesso de graves e destaca a presença vocal.",
        floatArrayOf(-1.2f, -0.7f, 0.2f, 1.8f, 0.6f),
        -20f,
        1.8f
    ),
    BALANCED(
        "Balanced",
        "Equilíbrio geral com leve destaque para inteligibilidade.",
        floatArrayOf(-0.4f, 0f, 0.1f, 0.8f, 0.4f),
        -18f,
        1.45f
    ),
    BASS_CONTROL(
        "Bass Control",
        "Graves moderados, sem tentar forçar subgrave no alto-falante.",
        floatArrayOf(1.0f, -0.4f, -0.2f, 0.4f, 0.2f),
        -18f,
        1.55f
    ),
    DYNAMIC(
        "Dynamic",
        "Controle suave de variações de volume e picos.",
        floatArrayOf(-0.4f, 0.1f, 0.1f, 0.6f, 0.3f),
        -22f,
        2.0f
    ),
    GAMING_CINEMA(
        "Gaming / Cinema",
        "Mais presença em efeitos e diálogos, mantendo dinâmica moderada.",
        floatArrayOf(-0.7f, -0.3f, 0.4f, 1.2f, 0.5f),
        -20f,
        1.65f
    );

    companion object {
        val DEFAULT = BALANCED
        val BAND_FREQUENCIES_HZ = floatArrayOf(90f, 250f, 1000f, 3500f, 9000f)
        val BAND_Q = floatArrayOf(0.85f, 0.9f, 1.0f, 1.0f, 0.9f)
    }
}
