#pragma once
#include "EffectBase.h"
#include <atomic>
#include <vector>
#include <cmath>

/**
 * Delay — stereo digital delay with feedback and ping-pong mode.
 *
 * Parameters:
 *   0 = delayMs   (10–2000 ms, default 500)
 *   1 = feedback  (0–0.95,     default 0.4)
 *   2 = mix       (0–1,        default 0.5)
 *   3 = pingPong  (0=off, 1=on, default 0)
 */
class Delay final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        const size_t maxSamples = static_cast<size_t>(sampleRate * 2.1f) + 1;
        mDelayLine[0].assign(maxSamples, 0.0f);
        mDelayLine[1].assign(maxSamples, 0.0f);
        mWritePos[0] = mWritePos[1] = 0;
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0: mDelayMs.store(std::clamp(value, 10.0f, 2000.0f)); break;
            case 1: mFeedback.store(std::clamp(value, 0.0f, 0.95f));   break;
            case 2: mMix.store(std::clamp(value, 0.0f, 1.0f));         break;
            case 3: mPingPong.store(value > 0.5f);                     break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        if (mDelayLine[0].empty()) return;

        const float feedback = mFeedback.load(std::memory_order_relaxed);
        const float mix      = mMix.load(std::memory_order_relaxed);
        const bool  pingPong = mPingPong.load(std::memory_order_relaxed);
        const size_t bufSize = mDelayLine[0].size();

        const size_t delaySamples = static_cast<size_t>(
            mDelayMs.load(std::memory_order_relaxed) * 0.001f * mSampleRate);
        const size_t clampedDelay = std::min(delaySamples, bufSize - 1);

        for (int f = 0; f < numFrames; ++f) {
            for (int c = 0; c < std::min(channelCount, 2); ++c) {
                const int   idx  = f * channelCount + c;
                const float dry  = samples[idx];
                const size_t wPos = mWritePos[c];
                const size_t rPos = (wPos + bufSize - clampedDelay) % bufSize;

                const int fbSrc = pingPong ? (1 - c) : c;
                const float delayed = mDelayLine[c][rPos];

                mDelayLine[c][wPos] = dry + feedback * mDelayLine[fbSrc][rPos];
                mWritePos[c]        = (wPos + 1) % bufSize;
                samples[idx]        = dry + mix * (delayed - dry);
            }
        }
    }

private:
    std::atomic<float> mDelayMs{500.0f};
    std::atomic<float> mFeedback{0.4f};
    std::atomic<float> mMix{0.5f};
    std::atomic<bool>  mPingPong{false};

    std::vector<float> mDelayLine[2];
    size_t             mWritePos[2]{0, 0};
};
