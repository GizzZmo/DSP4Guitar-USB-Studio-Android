#pragma once
#include "EffectBase.h"
#include <atomic>
#include <cmath>

/**
 * AutoWah — envelope-controlled band-pass filter ("wah" effect).
 *
 * The envelope follower tracks the input amplitude; as the signal gets louder
 * the filter centre frequency sweeps upward.
 *
 * Parameters:
 *   0 = sensitivity  (0–1, default 0.5)  — how quickly the envelope responds
 *   1 = minFreqHz    (100–1000, default 300)
 *   2 = maxFreqHz    (500–5000, default 2000)
 *   3 = resonance    (0.5–10,   default 3)
 *   4 = mix          (0–1,      default 1)
 */
class AutoWah final : public EffectBase {
public:
    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0: mSensitivity.store(std::clamp(value, 0.0f, 1.0f)); break;
            case 1: mMinFreq.store(std::clamp(value, 100.0f, 1000.0f)); break;
            case 2: mMaxFreq.store(std::clamp(value, 500.0f, 5000.0f)); break;
            case 3: mResonance.store(std::clamp(value, 0.5f, 10.0f));   break;
            case 4: mMix.store(std::clamp(value, 0.0f, 1.0f));          break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float sens    = mSensitivity.load(std::memory_order_relaxed);
        const float minF    = mMinFreq.load(std::memory_order_relaxed);
        const float maxF    = mMaxFreq.load(std::memory_order_relaxed);
        const float Q       = mResonance.load(std::memory_order_relaxed);
        const float mix     = mMix.load(std::memory_order_relaxed);
        const float sr      = mSampleRate;
        const float envAtk  = std::exp(-1.0f / (sr * (1.0f - sens + 0.01f) * 0.02f));
        const float envRel  = std::exp(-1.0f / (sr * (1.0f - sens + 0.01f) * 0.2f));

        for (int f = 0; f < numFrames; ++f) {
            // Track envelope on first channel
            const float absIn = std::abs(samples[f * channelCount]);
            mEnv = (absIn > mEnv) ? envAtk * mEnv + (1.0f - envAtk) * absIn
                                  : envRel * mEnv + (1.0f - envRel) * absIn;

            // Map envelope to filter frequency
            const float freq = minF + mEnv * (maxF - minF);
            const float w0   = 2.0f * 3.14159265f * freq / sr;
            const float alpha = std::sin(w0) / (2.0f * Q);

            // Biquad band-pass coefficients
            const float b0 =  alpha;
            const float b1 =  0.0f;
            const float b2 = -alpha;
            const float a0 =  1.0f + alpha;
            const float a1 = -2.0f * std::cos(w0);
            const float a2 =  1.0f - alpha;

            for (int c = 0; c < channelCount; ++c) {
                const int idx = f * channelCount + c;
                const float x  = samples[idx];
                const float y  = (b0/a0)*x + (b1/a0)*mX1[c] + (b2/a0)*mX2[c]
                                           - (a1/a0)*mY1[c] - (a2/a0)*mY2[c];
                mX2[c] = mX1[c]; mX1[c] = x;
                mY2[c] = mY1[c]; mY1[c] = y;
                samples[idx] = x + mix * (y - x);
            }
        }
    }

private:
    std::atomic<float> mSensitivity{0.5f};
    std::atomic<float> mMinFreq{300.0f};
    std::atomic<float> mMaxFreq{2000.0f};
    std::atomic<float> mResonance{3.0f};
    std::atomic<float> mMix{1.0f};

    float mEnv{0.0f};
    float mX1[2]{}, mX2[2]{}, mY1[2]{}, mY2[2]{};
};
