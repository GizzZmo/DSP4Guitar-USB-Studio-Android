package com.example.dsp.engine

import com.example.dsp.model.EffectUnit
import com.example.dsp.model.TunerNoteState
import kotlin.math.*

class DspProcessors {

    // 1. Noise Gate
    class NoiseGate {
        private var envelope = 0.0f
        private var gain = 1.0f

        fun process(samples: FloatArray, thresholdDb: Float, releaseMs: Float, sampleRate: Int) {
            val threshold = 10.0f.pow(thresholdDb / 20.0f)
            val releaseAlpha = exp(-1.0f / (sampleRate * (releaseMs / 1000.0f)))

            for (i in samples.indices) {
                val inputAbs = abs(samples[i])
                envelope = max(inputAbs, envelope * 0.999f)
                val targetGain = if (envelope >= threshold) 1.0f else 0.0f
                gain = gain * releaseAlpha + targetGain * (1.0f - releaseAlpha)
                samples[i] *= gain
            }
        }
    }

    // 2. Tube Overdrive (Soft Clipping TS-Style)
    class TubeOverdrive {
        private var lowPassState = 0.0f

        fun process(samples: FloatArray, drive: Float, tone: Float, level: Float) {
            val gain = 1.0f + drive * 18.0f
            val alpha = 0.1f + tone * 0.8f

            for (i in samples.indices) {
                var x = samples[i] * gain
                // Soft clipping Tube Screamer approximation
                x = when {
                    x > 1.0f -> 2.0f / 3.0f
                    x < -1.0f -> -2.0f / 3.0f
                    else -> x - (x * x * x) / 3.0f
                }
                // Tone low-pass filter
                lowPassState = lowPassState + alpha * (x - lowPassState)
                samples[i] = lowPassState * level
            }
        }
    }

    // 3. Heavy Distortion (Hard Clipping & High Gain)
    class Distortion {
        fun process(samples: FloatArray, gainParam: Float, contour: Float, level: Float) {
            val drive = 1.0f + gainParam * 35.0f
            val clipLevel = 0.6f - gainParam * 0.2f

            for (i in samples.indices) {
                var x = samples[i] * drive
                // Hard clipping
                x = x.coerceIn(-clipLevel, clipLevel) / clipLevel
                // Asymmetric warmth offset
                x += 0.05f * (x * x)
                samples[i] = x * level
            }
        }
    }

    // 4. Tube Amp Simulator (Preamp + Sag + Tone Stack)
    class AmpSim {
        private var sag = 1.0f
        private var low = 0.0f
        private var mid = 0.0f
        private var high = 0.0f

        fun process(
            samples: FloatArray,
            gain: Float,
            bass: Float,
            middle: Float,
            treble: Float,
            presence: Float,
            master: Float
        ) {
            val drive = 1.0f + gain * 25.0f

            for (i in samples.indices) {
                var x = samples[i] * drive

                // Tube power sag dynamic response
                val absX = abs(x)
                if (absX > 0.8f) {
                    sag = max(0.6f, sag - 0.001f)
                } else {
                    sag = min(1.0f, sag + 0.0005f)
                }
                x *= sag

                // Tube triode non-linear saturation
                x = tanh(x * 1.2f)

                // Simple 3-band tone stack simulation
                low = low + (0.1f + bass * 0.2f) * (x - low)
                high = x - low
                mid = mid + (0.15f + middle * 0.25f) * (high - mid)

                var shaped = (low * bass * 1.2f) + (mid * middle * 1.5f) + ((high - mid) * treble * 1.3f)
                shaped += presence * 0.2f * (shaped - low)

                samples[i] = shaped * master
            }
        }
    }

    // 5. Cabinet IR Filter
    class CabinetIr {
        private var lastSample = 0.0f

        fun process(samples: FloatArray, cabType: Float, micPosition: Float) {
            val resonance = 0.3f + cabType * 0.15f
            val damping = 0.2f + (1.0f - micPosition) * 0.5f

            for (i in samples.indices) {
                val input = samples[i]
                val filtered = lastSample + damping * (input - lastSample)
                lastSample = filtered
                samples[i] = input * (1.0f - resonance) + filtered * resonance
            }
        }
    }

    // 6. 7-Band Graphic Equalizer
    class GraphicEq {
        private val states = FloatArray(7)

        fun process(samples: FloatArray, bands: FloatArray) {
            val alphas = floatArrayOf(0.05f, 0.1f, 0.2f, 0.35f, 0.5f, 0.7f, 0.85f)
            for (i in samples.indices) {
                var sample = samples[i]
                var eqOutput = 0.0f
                for (b in 0..6) {
                    states[b] += alphas[b] * (sample - states[b])
                    val bandGain = bands[b] // -12dB to +12dB scaled to 0.25..2.0
                    eqOutput += states[b] * bandGain
                }
                samples[i] = eqOutput / 3.0f
            }
        }
    }

    // 7. Stereo Chorus Modulation
    class Chorus {
        private val bufferSize = 4410
        private val buffer = FloatArray(bufferSize)
        private var writePos = 0
        private var lfoPhase = 0.0f

        fun process(samples: FloatArray, rateHz: Float, depthMs: Float, mix: Float, sampleRate: Int) {
            val lfoInc = (2.0f * PI.toFloat() * rateHz) / sampleRate
            val maxDelaySamples = (depthMs * 0.001f * sampleRate).coerceIn(10.0f, (bufferSize - 100).toFloat())

            for (i in samples.indices) {
                buffer[writePos] = samples[i]

                lfoPhase += lfoInc
                if (lfoPhase > 2.0f * PI) lfoPhase -= 2.0f * PI.toFloat()

                val delaySamples = (maxDelaySamples * (0.5f + 0.5f * sin(lfoPhase)))
                var readPos = writePos - delaySamples.toInt()
                if (readPos < 0) readPos += bufferSize

                val delayedSample = buffer[readPos]
                samples[i] = samples[i] * (1.0f - mix) + delayedSample * mix

                writePos = (writePos + 1) % bufferSize
            }
        }
    }

    // 8. Delay / Tape Echo
    class Delay {
        private val maxDelay = 192000 // up to 4 seconds at 48kHz
        private val buffer = FloatArray(maxDelay)
        private var writePos = 0

        fun process(samples: FloatArray, delayMs: Float, feedback: Float, mix: Float, sampleRate: Int) {
            val delaySamples = ((delayMs / 1000.0f) * sampleRate).toInt().coerceIn(10, maxDelay - 1)

            for (i in samples.indices) {
                var readPos = writePos - delaySamples
                if (readPos < 0) readPos += maxDelay

                val delayedSample = buffer[readPos]
                val input = samples[i]

                buffer[writePos] = input + delayedSample * feedback.coerceIn(0.0f, 0.95f)
                samples[i] = input * (1.0f - mix) + delayedSample * mix

                writePos = (writePos + 1) % maxDelay
            }
        }
    }

    // 9. Reverb Network
    class Reverb {
        private val combBuffer1 = FloatArray(1557)
        private val combBuffer2 = FloatArray(1617)
        private val combBuffer3 = FloatArray(1791)
        private var p1 = 0
        private var p2 = 0
        private var p3 = 0

        fun process(samples: FloatArray, roomSize: Float, mix: Float) {
            val feedback = 0.5f + roomSize * 0.45f

            for (i in samples.indices) {
                val input = samples[i]

                val out1 = combBuffer1[p1]
                combBuffer1[p1] = input + out1 * feedback
                p1 = (p1 + 1) % combBuffer1.size

                val out2 = combBuffer2[p2]
                combBuffer2[p2] = input + out2 * feedback
                p2 = (p2 + 1) % combBuffer2.size

                val out3 = combBuffer3[p3]
                combBuffer3[p3] = input + out3 * feedback
                p3 = (p3 + 1) % combBuffer3.size

                val wet = (out1 + out2 + out3) / 3.0f
                samples[i] = input * (1.0f - mix) + wet * mix
            }
        }
    }

    // 10. YIN Pitch Detector for Chromatic Tuner
    class YinTuner {
        private val guitarStrings = listOf(
            Triple("E2", 82.41f, 6),
            Triple("A2", 110.00f, 5),
            Triple("D3", 146.83f, 4),
            Triple("G3", 196.00f, 3),
            Triple("B3", 246.94f, 2),
            Triple("E4", 329.63f, 1)
        )

        fun detectPitch(buffer: FloatArray, sampleRate: Int): TunerNoteState {
            val size = buffer.size
            if (size < 1024) return TunerNoteState()

            // Calculate Difference Function
            val halfSize = size / 2
            val d = FloatArray(halfSize)
            for (tau in 1 until halfSize) {
                var sum = 0.0f
                for (i in 0 until halfSize) {
                    val delta = buffer[i] - buffer[i + tau]
                    sum += delta * delta
                }
                d[tau] = sum
            }

            // Cumulative Mean Normalized Difference
            val cmndf = FloatArray(halfSize)
            cmndf[0] = 1.0f
            var runningSum = 0.0f
            var bestTau = -1
            val threshold = 0.15f

            for (tau in 1 until halfSize) {
                runningSum += d[tau]
                cmndf[tau] = d[tau] * tau / runningSum
                if (bestTau == -1 && cmndf[tau] < threshold) {
                    bestTau = tau
                }
            }

            if (bestTau <= 0) {
                // Find minimum tau if threshold not reached
                var minVal = Float.MAX_VALUE
                for (tau in 20 until halfSize) {
                    if (cmndf[tau] < minVal) {
                        minVal = cmndf[tau]
                        bestTau = tau
                    }
                }
            }

            val freq = if (bestTau > 0) sampleRate.toFloat() / bestTau else 82.41f
            val validFreq = freq.coerceIn(50.0f, 1000.0f)

            // Find closest string
            var closestString = guitarStrings[0]
            var minFreqDiff = Float.MAX_VALUE

            for (str in guitarStrings) {
                val diff = abs(str.second - validFreq)
                if (diff < minFreqDiff) {
                    minFreqDiff = diff
                    closestString = str
                }
            }

            // Calculate cents offset: 1200 * log2(f_detected / f_target)
            val cents = (1200.0f * log2(validFreq / closestString.second)).coerceIn(-50.0f, 50.0f)
            val isInTune = abs(cents) <= 4.0f

            return TunerNoteState(
                noteName = closestString.first,
                targetFreq = closestString.second,
                detectedFreq = validFreq,
                centsOffset = cents,
                isInTune = isInTune,
                stringNumber = closestString.third,
                tuningName = "Standard E"
            )
        }
    }
}
