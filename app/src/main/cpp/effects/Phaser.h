#pragma once
#include "EffectBase.h"
#include <atomic>
#include <array>
#include <cmath>

/**
 * Phaser — 4-stage all-pass filter modulated by an LFO.
 *
 * Parameters:
 *   0 = rate     (0.05–10 Hz, default 0.5)
 *   1 = depth    (0–1,        default 0.7)
 *   2 = feedback (0–0.99,     default 0.5)
 *   3 = mix      (0–1,        default 0.5)
 */
class Phaser final : public EffectBase {
public:
    static constexpr int NUM_STAGES = 4;

    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        mLfoPhaseInc = calcLfoInc(mRate.load());
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0:
                mRate.store(std::clamp(value, 0.05f, 10.0f));
                mLfoPhaseInc = calcLfoInc(mRate.load());
                break;
            case 1: mDepth.store(std::clamp(value, 0.0f, 1.0f));    break;
            case 2: mFeedback.store(std::clamp(value, 0.0f, 0.99f));break;
            case 3: mMix.store(std::clamp(value, 0.0f, 1.0f));      break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float depth    = mDepth.load(std::memory_order_relaxed);
        const float feedback = mFeedback.load(std::memory_order_relaxed);
        const float mix      = mMix.load(std::memory_order_relaxed);

        for (int f = 0; f < numFrames; ++f) {
            const float lfo = 0.5f + 0.5f * std::sin(mLfoPhase);
            mLfoPhase += mLfoPhaseInc;
            if (mLfoPhase >= kTwoPi) mLfoPhase -= kTwoPi;

            // All-pass coefficient driven by LFO
            const float minFreq = 200.0f, maxFreq = 4000.0f;
            const float freq = minFreq + lfo * depth * (maxFreq - minFreq);
            const float a    = (std::tan(kPi * freq / mSampleRate) - 1.0f) /
                               (std::tan(kPi * freq / mSampleRate) + 1.0f);

            for (int c = 0; c < channelCount; ++c) {
                const int idx  = f * channelCount + c;
                float x        = samples[idx] + feedback * mFbState[c];
                float ap       = x;
                for (int s = 0; s < NUM_STAGES; ++s) {
                    const float y    = a * ap + mApState[c][s] - a * mApState[c][s];
                    mApState[c][s]   = ap;
                    ap               = y;
                }
                mFbState[c]    = ap;
                samples[idx]   = samples[idx] + mix * (ap - samples[idx]);
            }
        }
    }

private:
    float calcLfoInc(float rate) const {
        return kTwoPi * rate / mSampleRate;
    }

    static constexpr float kTwoPi = 6.28318530718f;
    static constexpr float kPi    = 3.14159265359f;

    std::atomic<float> mRate{0.5f};
    std::atomic<float> mDepth{0.7f};
    std::atomic<float> mFeedback{0.5f};
    std::atomic<float> mMix{0.5f};

    float mLfoPhase{0.0f};
    float mLfoPhaseInc{kTwoPi * 0.5f / 48000.0f};

    float mApState[2][NUM_STAGES]{};
    float mFbState[2]{};
};
