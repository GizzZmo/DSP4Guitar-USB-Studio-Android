package com.example.dsp.model

enum class EffectType(
    val displayName: String,
    val category: String,
    val defaultColor: Long
) {
    NOISE_GATE("Noise Gate", "Dynamics", 0xFF455A64),
    TUNER("Chromatic Tuner", "Utility", 0xFF37474F),
    OVERDRIVE("Tube Overdrive", "Gain", 0xFFFFB300),
    DISTORTION("Metal Distortion", "Gain", 0xFFD50000),
    AMP_SIM("Guitar Amp Sim", "Amplifier", 0xFFFF6D00),
    CAB_IR("Cabinet IR", "Speaker", 0xFF4E342E),
    GRAPHIC_EQ("7-Band EQ", "Tone", 0xFF00838F),
    CHORUS("Stereo Chorus", "Modulation", 0xFFAA00FF),
    DELAY("Tape Echo Delay", "Time", 0xFF00E5FF),
    REVERB("Studio Reverb", "Space", 0xFF6200EA)
}

data class EffectParameter(
    val key: String,
    val label: String,
    val value: Float,
    val minValue: Float,
    val maxValue: Float,
    val unit: String = ""
)

data class EffectUnit(
    val id: String,
    val type: EffectType,
    val name: String,
    val enabled: Boolean = true,
    val parameters: List<EffectParameter>,
    val colorHex: Long = type.defaultColor
) {
    fun getParamValue(key: String): Float {
        return parameters.find { it.key == key }?.value ?: 0f
    }

    fun withParam(key: String, newValue: Float): EffectUnit {
        val updated = parameters.map { param ->
            if (param.key == key) {
                param.copy(value = newValue.coerceIn(param.minValue, param.maxValue))
            } else param
        }
        return copy(parameters = updated)
    }
}

data class AudioConfig(
    val sampleRate: Int = 48000,
    val bufferSize: Int = 128,
    val inputGain: Float = 1.0f,
    val outputVolume: Float = 1.0f,
    val directMonitoring: Boolean = false,
    val usbDeviceName: String = "Internal Mic / USB Audio Codec",
    val usbConnected: Boolean = false,
    val measuredLatencyMs: Float = 3.2f,
    val isEngineRunning: Boolean = true
)

data class TunerNoteState(
    val noteName: String = "E2",
    val targetFreq: Float = 82.41f,
    val detectedFreq: Float = 82.41f,
    val centsOffset: Float = 0.0f,
    val isInTune: Boolean = true,
    val stringNumber: Int = 6,
    val tuningName: String = "Standard E"
)

data class PresetData(
    val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val effects: List<EffectUnit>
)
