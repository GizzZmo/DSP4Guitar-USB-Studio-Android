#pragma once

#include <cstdint>

/**
 * Abstract base class for all DSP effect stages.
 * Implementations must be real-time safe: no allocations, no locks, no I/O.
 */
class EffectBase {
public:
    virtual ~EffectBase() = default;

    virtual void setSampleRate(float sampleRate) { mSampleRate = sampleRate; }

    /**
     * Process interleaved float samples in-place.
     * @param samples      pointer to interleaved PCM data
     * @param numFrames    number of sample frames
     * @param channelCount channels per frame (typically 2)
     */
    virtual void process(float* samples, int32_t numFrames, int32_t channelCount) = 0;

    /**
     * Set a parameter by integer ID.  IDs are effect-specific and mirrored
     * in the Kotlin EffectParameter enums.
     */
    virtual void setParameter(int paramId, float value) {}

protected:
    float mSampleRate{48000.0f};
};
