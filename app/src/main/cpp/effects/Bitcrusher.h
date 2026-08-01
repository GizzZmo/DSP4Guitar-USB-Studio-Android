#pragma once
#include "EffectBase.h"
#include <atomic>
#include <cmath>

/**
 * Bitcrusher — reduces bit depth and/or sample rate to produce a lo-fi,
 * digital-degradation texture.
 *
 * Parameters:
 *   0 = bitDepth  (1–24, default 16)
 *   1 = mix       (0–1, default 1)
 */
class Bitcrusher final : public EffectBase {
public:
    static constexpr int PARAM_BIT_DEPTH = 0;
    static constexpr int PARAM_MIX       = 1;

    Bitcrusher() = default;

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case PARAM_BIT_DEPTH:
                mBitDepth.store(std::clamp(value, 1.0f, 24.0f));
                break;
            case PARAM_MIX:
                mMix.store(std::clamp(value, 0.0f, 1.0f));
                break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float depth     = mBitDepth.load(std::memory_order_relaxed);
        const float mix       = mMix.load(std::memory_order_relaxed);
        const float levels    = std::pow(2.0f, depth) - 1.0f;
        const int   total     = numFrames * channelCount;

        for (int i = 0; i < total; ++i) {
            const float dry      = samples[i];
            const float crushed  = std::round(dry * levels) / levels;
            samples[i]           = dry + mix * (crushed - dry);
        }
    }

private:
    std::atomic<float> mBitDepth{16.0f};
    std::atomic<float> mMix{1.0f};
};
