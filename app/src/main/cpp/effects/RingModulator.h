#pragma once
#include "EffectBase.h"
#include <atomic>
#include <cmath>

/**
 * RingModulator — multiplies the signal with a sinusoidal carrier.
 *
 * Parameters:
 *   0 = frequency  (1–5000 Hz, default 440)
 *   1 = mix        (0–1, default 1)
 */
class RingModulator final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        mPhaseInc = calcPhaseInc(mFrequency.load());
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0:
                mFrequency.store(std::clamp(value, 1.0f, 5000.0f));
                mPhaseInc = calcPhaseInc(mFrequency.load());
                break;
            case 1:
                mMix.store(std::clamp(value, 0.0f, 1.0f));
                break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float mix = mMix.load(std::memory_order_relaxed);

        for (int f = 0; f < numFrames; ++f) {
            const float carrier = std::sin(mPhase);
            mPhase += mPhaseInc;
            if (mPhase >= kTwoPi) mPhase -= kTwoPi;

            for (int c = 0; c < channelCount; ++c) {
                const int idx  = f * channelCount + c;
                const float dry = samples[idx];
                samples[idx]    = dry + mix * (dry * carrier - dry);
            }
        }
    }

private:
    float calcPhaseInc(float freq) const {
        return kTwoPi * freq / mSampleRate;
    }

    static constexpr float kTwoPi = 6.28318530718f;

    std::atomic<float> mFrequency{440.0f};
    std::atomic<float> mMix{1.0f};

    float mPhase{0.0f};
    float mPhaseInc{kTwoPi * 440.0f / 48000.0f};
};
