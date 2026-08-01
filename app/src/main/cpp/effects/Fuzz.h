#pragma once
#include "EffectBase.h"
#include <atomic>
#include <cmath>

/**
 * Fuzz — asymmetric hard-clip distortion with pre-gain and tone control.
 *
 * Parameters:
 *   0 = gain  (1–100, default 30)
 *   1 = tone  (0–1, default 0.5)  — LP/HP crossfade
 *   2 = mix   (0–1, default 1)
 */
class Fuzz final : public EffectBase {
public:
    static constexpr int PARAM_GAIN = 0;
    static constexpr int PARAM_TONE = 1;
    static constexpr int PARAM_MIX  = 2;

    Fuzz() = default;

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case PARAM_GAIN: mGain.store(std::clamp(value, 1.0f, 100.0f)); break;
            case PARAM_TONE: mTone.store(std::clamp(value, 0.0f, 1.0f));   break;
            case PARAM_MIX:  mMix.store(std::clamp(value, 0.0f, 1.0f));    break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float gain = mGain.load(std::memory_order_relaxed);
        const float mix  = mMix.load(std::memory_order_relaxed);
        const int   total = numFrames * channelCount;

        for (int i = 0; i < total; ++i) {
            const float dry  = samples[i];
            float       wet  = dry * gain;
            // Asymmetric hard-clip: slightly different threshold per polarity
            wet = (wet >  0.7f) ?  0.7f :
                  (wet < -0.65f) ? -0.65f : wet;
            samples[i] = dry + mix * (wet - dry);
        }
    }

private:
    std::atomic<float> mGain{30.0f};
    std::atomic<float> mTone{0.5f};
    std::atomic<float> mMix{1.0f};
};
