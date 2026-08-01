#pragma once
#include "EffectBase.h"
#include <atomic>
#include <cmath>

/**
 * Tremolo — amplitude modulation via LFO.
 *
 * Parameters:
 *   0 = rate   (0.1–20 Hz, default 5)
 *   1 = depth  (0–1,       default 0.5)
 *   2 = mix    (0–1,       default 1)
 */
class Tremolo final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        mLfoPhaseInc = kTwoPi * mRate.load() / sampleRate;
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0:
                mRate.store(std::clamp(value, 0.1f, 20.0f));
                mLfoPhaseInc = kTwoPi * mRate.load() / mSampleRate;
                break;
            case 1: mDepth.store(std::clamp(value, 0.0f, 1.0f)); break;
            case 2: mMix.store(std::clamp(value, 0.0f, 1.0f));   break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float depth = mDepth.load(std::memory_order_relaxed);
        const float mix   = mMix.load(std::memory_order_relaxed);
        const int   total = numFrames * channelCount;

        for (int f = 0; f < numFrames; ++f) {
            const float gain = 1.0f - depth * 0.5f * (1.0f - std::sin(mLfoPhase));
            mLfoPhase += mLfoPhaseInc;
            if (mLfoPhase >= kTwoPi) mLfoPhase -= kTwoPi;

            for (int c = 0; c < channelCount; ++c) {
                const int idx   = f * channelCount + c;
                const float dry = samples[idx];
                samples[idx]    = dry + mix * (dry * gain - dry);
            }
        }
    }

private:
    static constexpr float kTwoPi = 6.28318530718f;

    std::atomic<float> mRate{5.0f};
    std::atomic<float> mDepth{0.5f};
    std::atomic<float> mMix{1.0f};

    float mLfoPhase{0.0f};
    float mLfoPhaseInc{kTwoPi * 5.0f / 48000.0f};
};
