package com.dsp4guitar.studio.audio

/**
 * Kotlin façade for the native DSP4Guitar audio engine (dsp4guitar JNI library).
 *
 * All calls on this object are forwarded to native code via JNI.  Calls are
 * safe from any thread; the C++ layer uses atomics internally.
 */
class AudioEngineJni {

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    external fun create(): Boolean
    external fun destroy()
    external fun start(): Boolean
    external fun stop()
    external fun isRunning(): Boolean

    // ── Device selection ──────────────────────────────────────────────────────

    external fun setAudioDeviceId(deviceId: Int)

    // ── Gain staging ──────────────────────────────────────────────────────────

    external fun setInputGainDb(db: Float)
    external fun setOutputGainDb(db: Float)
    external fun getEstimatedLatencyMs(): Double

    // ── DSP chain control ─────────────────────────────────────────────────────

    external fun setEffectBypass(stageIndex: Int, bypass: Boolean)
    external fun isEffectBypassed(stageIndex: Int): Boolean
    external fun setEffectParameter(stageIndex: Int, paramId: Int, value: Float)

    companion object {
        init {
            System.loadLibrary("dsp4guitar")
        }
    }
}
