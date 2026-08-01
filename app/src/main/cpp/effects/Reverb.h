#pragma once
#include "EffectBase.h"
#include <atomic>
#include <vector>
#include <array>
#include <cmath>

/**
 * Reverb — Schroeder / Freeverb-style algorithmic reverb using comb filters
 * and all-pass diffusers.
 *
 * Parameters:
 *   0 = roomSize  (0–1,   default 0.5)
 *   1 = damping   (0–1,   default 0.5)
 *   2 = width     (0–1,   default 0.8)
 *   3 = mix       (0–1,   default 0.3)
 */
class Reverb final : public EffectBase {
public:
    static constexpr int NUM_COMBS  = 8;
    static constexpr int NUM_ALLPASS = 4;

    void setSampleRate(float sampleRate) override {
        EffectBase::setSampleRate(sampleRate);
        initBuffers(sampleRate);
    }

    void setParameter(int paramId, float value) override {
        switch (paramId) {
            case 0: mRoomSize.store(std::clamp(value, 0.0f, 1.0f)); break;
            case 1: mDamping.store(std::clamp(value, 0.0f, 1.0f));  break;
            case 2: mWidth.store(std::clamp(value, 0.0f, 1.0f));    break;
            case 3: mMix.store(std::clamp(value, 0.0f, 1.0f));      break;
        }
    }

    void process(float* samples, int32_t numFrames, int32_t channelCount) override {
        const float roomSize = mRoomSize.load(std::memory_order_relaxed);
        const float damping  = mDamping.load(std::memory_order_relaxed);
        const float mix      = mMix.load(std::memory_order_relaxed);

        // Feedback / damping coefficients (Freeverb style)
        const float feedback = 0.28f + roomSize * 0.7f;
        const float damp1    = damping * 0.4f;
        const float damp2    = 1.0f - damp1;

        for (int f = 0; f < numFrames; ++f) {
            // Mix down to mono for the reverb network input
            float in = 0.0f;
            for (int c = 0; c < channelCount; ++c) {
                in += samples[f * channelCount + c];
            }
            in /= static_cast<float>(channelCount);
            in *= 0.015f;  // scale down to prevent saturation

            float out = 0.0f;
            // Comb filters
            for (int i = 0; i < NUM_COMBS; ++i) {
                auto& buf = mCombBuf[i];
                const size_t pos = mCombPos[i];
                const float bufVal = buf[pos];
                mCombFilterStore[i] = bufVal * damp2 + mCombFilterStore[i] * damp1;
                buf[pos] = in + mCombFilterStore[i] * feedback;
                mCombPos[i] = (pos + 1) % buf.size();
                out += bufVal;
            }

            // All-pass filters
            for (int i = 0; i < NUM_ALLPASS; ++i) {
                auto& buf = mApBuf[i];
                const size_t pos = mApPos[i];
                const float bufVal = buf[pos];
                buf[pos] = out + bufVal * 0.5f;
                mApPos[i] = (pos + 1) % buf.size();
                out = bufVal - out;
            }

            // Write wet signal back (same to all channels)
            for (int c = 0; c < channelCount; ++c) {
                const int idx = f * channelCount + c;
                samples[idx] = samples[idx] * (1.0f - mix) + out * mix;
            }
        }
    }

private:
    void initBuffers(float sr) {
        // Comb filter delays (tuned prime lengths, scaled from 44100 Hz)
        static const int kCombDelays[NUM_COMBS] = {
            1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617
        };
        static const int kApDelays[NUM_ALLPASS] = { 556, 441, 341, 225 };

        for (int i = 0; i < NUM_COMBS; ++i) {
            const size_t len = static_cast<size_t>(kCombDelays[i] * sr / 44100.0f) + 1;
            mCombBuf[i].assign(len, 0.0f);
            mCombPos[i] = 0;
            mCombFilterStore[i] = 0.0f;
        }
        for (int i = 0; i < NUM_ALLPASS; ++i) {
            const size_t len = static_cast<size_t>(kApDelays[i] * sr / 44100.0f) + 1;
            mApBuf[i].assign(len, 0.0f);
            mApPos[i] = 0;
        }
    }

    std::atomic<float> mRoomSize{0.5f};
    std::atomic<float> mDamping{0.5f};
    std::atomic<float> mWidth{0.8f};
    std::atomic<float> mMix{0.3f};

    std::vector<float> mCombBuf[NUM_COMBS];
    std::vector<float> mApBuf[NUM_ALLPASS];
    size_t             mCombPos[NUM_COMBS]{};
    size_t             mApPos[NUM_ALLPASS]{};
    float              mCombFilterStore[NUM_COMBS]{};
};
