package io.github.astromg01.a06audioengine

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.Build

object DeviceDiagnostics {
    fun report(context: Context): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val outputs = runCatching {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map(::describeDevice)
        }.getOrDefault(emptyList())

        val effects = runCatching {
            AudioEffect.queryEffects()?.toList().orEmpty()
        }.getOrDefault(emptyList())

        fun hasEffect(type: java.util.UUID): Boolean = effects.any { it.type == type }

        return buildString {
            appendLine("DISPOSITIVO")
            appendLine("${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
            appendLine()
            appendLine("SAÍDAS DETECTADAS")
            if (outputs.isEmpty()) {
                appendLine("Nenhuma saída foi reportada pela AudioManager.")
            } else {
                outputs.forEach { appendLine("• $it") }
            }
            appendLine()
            appendLine("EFEITOS EXPOSTOS PELO ANDROID")
            appendLine("Equalizer: ${availability(hasEffect(AudioEffect.EFFECT_TYPE_EQUALIZER))}")
            appendLine("DynamicsProcessing: ${availability(hasEffect(AudioEffect.EFFECT_TYPE_DYNAMICS_PROCESSING))}")
            appendLine("LoudnessEnhancer: ${availability(hasEffect(AudioEffect.EFFECT_TYPE_LOUDNESS_ENHANCER))}")
            appendLine()
            appendLine("Observação: a lista indica efeitos expostos pelo sistema, não garante que possam controlar outros aplicativos.")
        }
    }

    private fun availability(found: Boolean): String =
        if (found) "detectado no catálogo" else "não anunciado pelo catálogo"

    private fun describeDevice(device: AudioDeviceInfo): String {
        val name = device.productName?.toString()?.takeIf { it.isNotBlank() } ?: "Saída sem nome"
        val type = when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "alto-falante interno"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "fone com fio"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "headset com fio"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "headset USB"
            else -> "tipo ${device.type}"
        }
        return "$name ($type)"
    }
}
