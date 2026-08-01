#pragma once

#include <array>
#include <memory>
#include <cstdint>

// Forward-declare each effect
class Bitcrusher;
class Fuzz;
class MultibandCompressor;
class RingModulator;
class AutoWah;
class Phaser;
class Chorus;
class Tremolo;
class Delay;
class Reverb;

/**
 * DspChain holds and sequences the 10-stage fixed signal chain.
 *
 * Each stage can be independently bypassed.  Parameter updates arrive from
 * the JNI layer via atomic setters and are therefore safe to call from any
 * thread while the audio callback is running.
 *
 * Signal flow:
 *   Input → Bitcrusher → Fuzz → MultibandComp → RingMod → AutoWah
 *         → Phaser → Chorus → Tremolo → Delay → Reverb → Output
 */
class DspChain {
public:
    // Stage indices — keep in sync with Kotlin EffectStage enum
    enum class Stage : int {
        Bitcrusher   = 0,
        Fuzz         = 1,
        MultibandComp= 2,
        RingMod      = 3,
        AutoWah      = 4,
        Phaser       = 5,
        Chorus       = 6,
        Tremolo      = 7,
        Delay        = 8,
        Reverb       = 9,
        Count        = 10
    };

    DspChain();
    ~DspChain();

    // Called whenever the stream sample rate changes
    void setSampleRate(float sampleRate);

    // Main processing call — interleaved float samples in-place
    void process(float* samples, int32_t numFrames, int32_t channelCount);

    // Per-stage bypass control (thread-safe)
    void setBypass(Stage stage, bool bypass);
    bool isBypassed(Stage stage) const;

    // Per-stage parameter update (thread-safe, stage-specific)
    // paramId and value are forwarded to the appropriate effect object.
    void setParameter(Stage stage, int paramId, float value);

private:
    float mSampleRate{48000.0f};

    // Effect instances
    std::unique_ptr<Bitcrusher>          mBitcrusher;
    std::unique_ptr<Fuzz>                mFuzz;
    std::unique_ptr<MultibandCompressor> mMultibandComp;
    std::unique_ptr<RingModulator>       mRingMod;
    std::unique_ptr<AutoWah>             mAutoWah;
    std::unique_ptr<Phaser>              mPhaser;
    std::unique_ptr<Chorus>              mChorus;
    std::unique_ptr<Tremolo>             mTremolo;
    std::unique_ptr<Delay>               mDelay;
    std::unique_ptr<Reverb>              mReverb;

    std::array<std::atomic<bool>, static_cast<int>(Stage::Count)> mBypassed{};
};
