#pragma once
#include "EffectBase.h"
#include <atomic>
#include <vector>
#include <cmath>

/**
 * Chorus — short delay line with LFO-modulated delay time.
 *
 * Parameters:
 *   0 = rate     (0.05–10 Hz, default 0.8)
 *   1 = depth    (0–1,        default 0.5)
 *   2 = delayMs  (5–50 ms,    default 15)
 *   3 = feedback (0–0.9,      default 0.2)
 *   4 = mix      (0–1,        default 0.5)
 */
class Chorus final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        const size_t maxDelaySamples = static_cast<size_t>(sampleRate * 0.1f) + 1;
        mDelayLine[0].assign(maxDelaySamples, 0.0f);
        mDelayLine[1].assign(maxDelaySamples, 0.0f);
        mWritePos[0] = mWritePos[1] = 0;
        mLfoPhaseInc = kTwoPi * mRate.load() / sampleRate;
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0:
                mRate.store(std::clamp(value, 0.05f, 10.0f));
                mLfoPhaseInc = kTwoPi * mRate.load() / mSampleRate;
                break;
            case 1: mDepth.store(std::clamp(value, 0.0f, 1.0f));   break;
            case 2: mDelayMs.store(std::clamp(value, 5.0f, 50.0f));break;
            case 3: mFeedback.store(std::clamp(value, 0.0f, 0.9f));break;
            case 4: mMix.store(std::clamp(value, 0.0f, 1.0f));     break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        if (mDelayLine[0].empty()) return;

        const float depth    = mDepth.load(std::memory_order_relaxed);
        const float delayMs  = mDelayMs.load(std::memory_order_relaxed);
        const float feedback = mFeedback.load(std::memory_order_relaxed);
        const float mix      = mMix.load(std::memory_order_relaxed);
        const size_t bufSize = mDelayLine[0].size();

        for (int f = 0; f < numFrames; ++f) {
            const float lfo = std::sin(mLfoPhase);
            mLfoPhase += mLfoPhaseInc;
            if (mLfoPhase >= kTwoPi) mLfoPhase -= kTwoPi;

            const float modDelaySamples = (delayMs * 0.001f + lfo * depth * 0.01f) * mSampleRate;

            for (int c = 0; c < std::min(channelCount, 2); ++c) {
                const int   idx      = f * channelCount + c;
                const float dry      = samples[idx];
                const size_t wPos    = mWritePos[c];

                // Linear interpolation read
                const float rPosF    = static_cast<float>(wPos) - modDelaySamples;
                const size_t rPos    = static_cast<size_t>(
                    static_cast<long>(bufSize) + static_cast<long>(rPosF)) % bufSize;
                const size_t rPos1   = (rPos + 1) % bufSize;
                const float frac     = rPosF - std::floor(rPosF);
                const float delayed  = mDelayLine[c][rPos] * (1.0f - frac)
                                     + mDelayLine[c][rPos1] * frac;

                mDelayLine[c][wPos]  = dry + feedback * delayed;
                mWritePos[c]         = (wPos + 1) % bufSize;
                samples[idx]         = dry + mix * (delayed - dry);
            }
        }
    }

private:
    static constexpr float kTwoPi = 6.28318530718f;

    std::atomic<float> mRate{0.8f};
    std::atomic<float> mDepth{0.5f};
    std::atomic<float> mDelayMs{15.0f};
    std::atomic<float> mFeedback{0.2f};
    std::atomic<float> mMix{0.5f};

    std::vector<float> mDelayLine[2];
    size_t             mWritePos[2]{0, 0};
    float              mLfoPhase{0.0f};
    float              mLfoPhaseInc{kTwoPi * 0.8f / 48000.0f};
};
