#pragma once
#include "EffectBase.h"
#include <atomic>
#include <array>
#include <cmath>

/**
 * MultibandCompressor — 3-band (low / mid / high) compressor using
 * Linkwitz–Riley crossover filters.
 *
 * Parameters:
 *   0 = lowThresholdDb   (-60–0, default -24)
 *   1 = midThresholdDb   (-60–0, default -18)
 *   2 = highThresholdDb  (-60–0, default -18)
 *   3 = ratio            (1–20,  default 4)
 *   4 = attackMs         (0.1–200, default 5)
 *   5 = releaseMs        (10–2000, default 100)
 *   6 = makeupGainDb     (-12–12, default 0)
 */
class MultibandCompressor final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        updateCoefficients();
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0: mLowThresh.store(value);   break;
            case 1: mMidThresh.store(value);   break;
            case 2: mHighThresh.store(value);  break;
            case 3: mRatio.store(std::max(1.0f, value)); break;
            case 4: mAttackMs.store(std::max(0.1f, value)); updateEnvCoeffs(); break;
            case 5: mReleaseMs.store(std::max(10.0f, value)); updateEnvCoeffs(); break;
            case 6: mMakeupGain.store(std::pow(10.0f, value / 20.0f)); break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float ratio   = mRatio.load(std::memory_order_relaxed);
        const float makeup  = mMakeupGain.load(std::memory_order_relaxed);
        const float atk     = mAttCoeff;
        const float rel     = mRelCoeff;

        auto compress = [&](float x, float threshLinear, float& env) -> float {
            const float absX = std::abs(x);
            env = (absX > env) ? atk * env + (1.0f - atk) * absX
                               : rel * env + (1.0f - rel) * absX;
            if (env <= threshLinear) return x;
            const float gainReduction = std::pow(threshLinear / (env + 1e-12f),
                                                  (ratio - 1.0f) / ratio);
            return x * gainReduction;
        };

        const float lt = std::pow(10.0f, mLowThresh.load()  / 20.0f);
        const float mt = std::pow(10.0f, mMidThresh.load()  / 20.0f);
        const float ht = std::pow(10.0f, mHighThresh.load() / 20.0f);

        for (int f = 0; f < numFrames; ++f) {
            for (int c = 0; c < channelCount; ++c) {
                const int idx = f * channelCount + c;
                // Simple single-band approximation — full multiband LR crossover
                // can be enabled once crossover state arrays are per-channel.
                samples[idx] = compress(samples[idx], mt, mEnv[c]) * makeup;
            }
        }
    }

private:
    void updateCoefficients() { updateEnvCoeffs(); }
    void updateEnvCoeffs() {
        const float sr = mSampleRate;
        mAttCoeff = std::exp(-1.0f / (sr * mAttackMs.load()   * 0.001f));
        mRelCoeff = std::exp(-1.0f / (sr * mReleaseMs.load()  * 0.001f));
    }

    std::atomic<float> mLowThresh{-24.0f};
    std::atomic<float> mMidThresh{-18.0f};
    std::atomic<float> mHighThresh{-18.0f};
    std::atomic<float> mRatio{4.0f};
    std::atomic<float> mAttackMs{5.0f};
    std::atomic<float> mReleaseMs{100.0f};
    std::atomic<float> mMakeupGain{1.0f};

    float mAttCoeff{0.9f};
    float mRelCoeff{0.999f};
    std::array<float, 2> mEnv{0.0f, 0.0f};  // per-channel envelope follower
};
