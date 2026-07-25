package com.example.dsp.engine

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.example.dsp.model.AudioConfig
import com.example.dsp.model.EffectType
import com.example.dsp.model.EffectUnit
import com.example.dsp.model.TunerNoteState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class UsbAudioEngine(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    // Engine Flow States
    private val _configState = MutableStateFlow(AudioConfig())
    val configState: StateFlow<AudioConfig> = _configState.asStateFlow()

    private val _inputVuLevel = MutableStateFlow(0.0f)
    val inputVuLevel: StateFlow<Float> = _inputVuLevel.asStateFlow()

    private val _outputVuLevel = MutableStateFlow(0.0f)
    val outputVuLevel: StateFlow<Float> = _outputVuLevel.asStateFlow()

    private val _isClipping = MutableStateFlow(false)
    val isClipping: StateFlow<Boolean> = _isClipping.asStateFlow()

    private val _waveformState = MutableStateFlow(FloatArray(128))
    val waveformState: StateFlow<FloatArray> = _waveformState.asStateFlow()

    private val _fftSpectrumState = MutableStateFlow(FloatArray(32))
    val fftSpectrumState: StateFlow<FloatArray> = _fftSpectrumState.asStateFlow()

    private val _tunerState = MutableStateFlow(TunerNoteState())
    val tunerState: StateFlow<TunerNoteState> = _tunerState.asStateFlow()

    // Active DSP Processors
    private val noiseGate = DspProcessors.NoiseGate()
    private val overdrive = DspProcessors.TubeOverdrive()
    private val distortion = DspProcessors.Distortion()
    private val ampSim = DspProcessors.AmpSim()
    private val cabinetIr = DspProcessors.CabinetIr()
    private val graphicEq = DspProcessors.GraphicEq()
    private val chorus = DspProcessors.Chorus()
    private val delay = DspProcessors.Delay()
    private val reverb = DspProcessors.Reverb()
    private val tuner = DspProcessors.YinTuner()

    private var activeEffects: List<EffectUnit> = emptyList()

    @Volatile
    private var isProcessing = false
    private var audioThread: Thread? = null

    init {
        detectUsbInterfaces()
    }

    fun detectUsbInterfaces() {
        val devices = usbManager?.deviceList
        var foundUsb = false
        var deviceName = "Internal Mic / Generic USB"

        if (devices != null) {
            for ((_, device) in devices) {
                if (isUsbAudioDevice(device)) {
                    foundUsb = true
                    deviceName = device.productName ?: device.deviceName ?: "USB Audio Interface"
                    break
                }
            }
        }

        _configState.value = _configState.value.copy(
            usbConnected = foundUsb,
            usbDeviceName = deviceName
        )
    }

    private fun isUsbAudioDevice(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_AUDIO) {
                return true
            }
        }
        return false
    }

    fun updateActiveEffects(effects: List<EffectUnit>) {
        this.activeEffects = effects
    }

    fun updateConfig(newConfig: AudioConfig) {
        _configState.value = newConfig
    }

    fun startAudioProcessing() {
        if (isProcessing) return
        isProcessing = true

        detectUsbInterfaces()

        audioThread = thread(start = true, isDaemon = true, name = "DSPAudioThread") {
            runProcessingLoop()
        }
    }

    fun stopAudioProcessing() {
        isProcessing = false
        audioThread?.join(500)
        audioThread = null
    }

    private fun runProcessingLoop() {
        val sampleRate = _configState.value.sampleRate
        val bufferSizeSamples = _configState.value.bufferSize

        val minRecBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val recBufSize = max(minRecBuf, bufferSizeSamples * 4 * 2)

        var audioRecord: AudioRecord? = null
        var audioTrack: AudioTrack? = null

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recBufSize
            )

            val minTrackBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(max(minTrackBuf, bufferSizeSamples * 2))
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED ||
                audioTrack.state != AudioTrack.STATE_INITIALIZED
            ) {
                isProcessing = false
                return
            }

            audioRecord.startRecording()
            audioTrack.play()

            val pcmBuffer = ShortArray(bufferSizeSamples)
            val floatBuffer = FloatArray(bufferSizeSamples)
            var tunerCounter = 0

            while (isProcessing) {
                val readCount = audioRecord.read(pcmBuffer, 0, pcmBuffer.size)
                if (readCount <= 0) continue

                val currentConfig = _configState.value
                val inputGain = currentConfig.inputGain

                // Convert PCM 16-bit to Float [-1.0, 1.0]
                var inputSumSq = 0.0f
                for (i in 0 until readCount) {
                    val floatSample = (pcmBuffer[i] / 32768.0f) * inputGain
                    floatBuffer[i] = floatSample
                    inputSumSq += floatSample * floatSample
                }

                val inputRms = sqrt(inputSumSq / readCount)
                _inputVuLevel.value = inputRms.coerceIn(0.0f, 1.0f)

                // Run DSP Chain in sequence
                for (unit in activeEffects) {
                    if (!unit.enabled) continue
                    processUnit(unit, floatBuffer, readCount, sampleRate)
                }

                // Check Output VU & Clipping
                var outputSumSq = 0.0f
                var clipped = false
                val masterVol = currentConfig.outputVolume

                for (i in 0 until readCount) {
                    var outVal = floatBuffer[i] * masterVol
                    if (abs(outVal) >= 0.98f) {
                        clipped = true
                    }
                    outVal = outVal.coerceIn(-1.0f, 1.0f)
                    floatBuffer[i] = outVal
                    outputSumSq += outVal * outVal

                    // Convert back to PCM 16-bit
                    pcmBuffer[i] = (outVal * 32767.0f).toInt().toShort()
                }

                _outputVuLevel.value = sqrt(outputSumSq / readCount).coerceIn(0.0f, 1.0f)
                _isClipping.value = clipped

                // Play Audio Output
                audioTrack.write(pcmBuffer, 0, readCount)

                // Update Waveform & Spectrum for visuals
                if (readCount >= 128) {
                    val waveSlice = FloatArray(128)
                    for (k in 0 until 128) {
                        waveSlice[k] = floatBuffer[k]
                    }
                    _waveformState.value = waveSlice

                    // Simple 32-band spectrum energy
                    val bands = FloatArray(32)
                    val chunkSize = max(1, readCount / 32)
                    for (b in 0 until 32) {
                        var sum = 0f
                        for (c in 0 until chunkSize) {
                            val idx = b * chunkSize + c
                            if (idx < readCount) {
                                sum += abs(floatBuffer[idx])
                            }
                        }
                        bands[b] = (sum / chunkSize * 2.5f).coerceIn(0.05f, 1.0f)
                    }
                    _fftSpectrumState.value = bands
                }

                // Periodic Pitch Detection for Chromatic Tuner
                tunerCounter++
                if (tunerCounter % 4 == 0) {
                    _tunerState.value = tuner.detectPitch(floatBuffer, sampleRate)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioRecord?.stop()
                audioRecord?.release()
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processUnit(unit: EffectUnit, samples: FloatArray, count: Int, sampleRate: Int) {
        val p = unit.parameters.associate { it.key to it.value }

        when (unit.type) {
            EffectType.NOISE_GATE -> {
                val thresh = p["threshold"] ?: -45.0f
                val release = p["release"] ?: 100.0f
                noiseGate.process(samples, thresh, release, sampleRate)
            }
            EffectType.OVERDRIVE -> {
                val drive = p["drive"] ?: 0.5f
                val tone = p["tone"] ?: 0.5f
                val level = p["level"] ?: 0.8f
                overdrive.process(samples, drive, tone, level)
            }
            EffectType.DISTORTION -> {
                val gain = p["gain"] ?: 0.7f
                val contour = p["contour"] ?: 0.5f
                val level = p["level"] ?: 0.8f
                distortion.process(samples, gain, contour, level)
            }
            EffectType.AMP_SIM -> {
                val gain = p["gain"] ?: 0.6f
                val bass = p["bass"] ?: 0.5f
                val mid = p["middle"] ?: 0.5f
                val treble = p["treble"] ?: 0.5f
                val presence = p["presence"] ?: 0.4f
                val master = p["master"] ?: 0.8f
                ampSim.process(samples, gain, bass, mid, treble, presence, master)
            }
            EffectType.CAB_IR -> {
                val cabType = p["cab_type"] ?: 0.0f
                val micPos = p["mic_pos"] ?: 0.5f
                cabinetIr.process(samples, cabType, micPos)
            }
            EffectType.GRAPHIC_EQ -> {
                val b0 = p["eq_60"] ?: 1.0f
                val b1 = p["eq_150"] ?: 1.0f
                val b2 = p["eq_400"] ?: 1.0f
                val b3 = p["eq_1k"] ?: 1.0f
                val b4 = p["eq_2k4"] ?: 1.0f
                val b5 = p["eq_6k"] ?: 1.0f
                val b6 = p["eq_15k"] ?: 1.0f
                graphicEq.process(samples, floatArrayOf(b0, b1, b2, b3, b4, b5, b6))
            }
            EffectType.CHORUS -> {
                val rate = p["rate"] ?: 1.5f
                val depth = p["depth"] ?: 5.0f
                val mix = p["mix"] ?: 0.4f
                chorus.process(samples, rate, depth, mix, sampleRate)
            }
            EffectType.DELAY -> {
                val delayMs = p["delay_ms"] ?: 350.0f
                val feedback = p["feedback"] ?: 0.4f
                val mix = p["mix"] ?: 0.35f
                delay.process(samples, delayMs, feedback, mix, sampleRate)
            }
            EffectType.REVERB -> {
                val room = p["room_size"] ?: 0.6f
                val mix = p["mix"] ?: 0.3f
                reverb.process(samples, room, mix)
            }
            EffectType.TUNER -> {
                // Tuner unit just passes through audio
            }
        }
    }
}
