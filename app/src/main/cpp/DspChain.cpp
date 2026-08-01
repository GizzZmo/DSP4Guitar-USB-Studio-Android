#include "DspChain.h"

#include "effects/Bitcrusher.h"
#include "effects/Fuzz.h"
#include "effects/MultibandCompressor.h"
#include "effects/RingModulator.h"
#include "effects/AutoWah.h"
#include "effects/Phaser.h"
#include "effects/Chorus.h"
#include "effects/Tremolo.h"
#include "effects/Delay.h"
#include "effects/Reverb.h"

DspChain::DspChain()
    : mBitcrusher(std::make_unique<Bitcrusher>()),
      mFuzz(std::make_unique<Fuzz>()),
      mMultibandComp(std::make_unique<MultibandCompressor>()),
      mRingMod(std::make_unique<RingModulator>()),
      mAutoWah(std::make_unique<AutoWah>()),
      mPhaser(std::make_unique<Phaser>()),
      mChorus(std::make_unique<Chorus>()),
      mTremolo(std::make_unique<Tremolo>()),
      mDelay(std::make_unique<Delay>()),
      mReverb(std::make_unique<Reverb>()) {
    for (auto& b : mBypassed) b.store(true);  // all bypassed by default
}

DspChain::~DspChain() = default;

void DspChain::setSampleRate(float sampleRate) {
    mSampleRate = sampleRate;
    mBitcrusher->setSampleRate(sampleRate);
    mFuzz->setSampleRate(sampleRate);
    mMultibandComp->setSampleRate(sampleRate);
    mRingMod->setSampleRate(sampleRate);
    mAutoWah->setSampleRate(sampleRate);
    mPhaser->setSampleRate(sampleRate);
    mChorus->setSampleRate(sampleRate);
    mTremolo->setSampleRate(sampleRate);
    mDelay->setSampleRate(sampleRate);
    mReverb->setSampleRate(sampleRate);
}

void DspChain::process(float* samples, int32_t numFrames, int32_t channelCount) {
#define PROCESS_STAGE(effect, stage) \
    if (!mBypassed[static_cast<int>(Stage::stage)].load(std::memory_order_relaxed)) { \
        effect->process(samples, numFrames, channelCount); \
    }

    PROCESS_STAGE(mBitcrusher,   Bitcrusher)
    PROCESS_STAGE(mFuzz,         Fuzz)
    PROCESS_STAGE(mMultibandComp,MultibandComp)
    PROCESS_STAGE(mRingMod,      RingMod)
    PROCESS_STAGE(mAutoWah,      AutoWah)
    PROCESS_STAGE(mPhaser,       Phaser)
    PROCESS_STAGE(mChorus,       Chorus)
    PROCESS_STAGE(mTremolo,      Tremolo)
    PROCESS_STAGE(mDelay,        Delay)
    PROCESS_STAGE(mReverb,       Reverb)
#undef PROCESS_STAGE
}

void DspChain::setBypass(Stage stage, bool bypass) {
    mBypassed[static_cast<int>(stage)].store(bypass, std::memory_order_relaxed);
}

bool DspChain::isBypassed(Stage stage) const {
    return mBypassed[static_cast<int>(stage)].load(std::memory_order_relaxed);
}

void DspChain::setParameter(Stage stage, int paramId, float value) {
    switch (stage) {
        case Stage::Bitcrusher:    mBitcrusher->setParameter(paramId, value);    break;
        case Stage::Fuzz:          mFuzz->setParameter(paramId, value);          break;
        case Stage::MultibandComp: mMultibandComp->setParameter(paramId, value); break;
        case Stage::RingMod:       mRingMod->setParameter(paramId, value);       break;
        case Stage::AutoWah:       mAutoWah->setParameter(paramId, value);       break;
        case Stage::Phaser:        mPhaser->setParameter(paramId, value);        break;
        case Stage::Chorus:        mChorus->setParameter(paramId, value);        break;
        case Stage::Tremolo:       mTremolo->setParameter(paramId, value);       break;
        case Stage::Delay:         mDelay->setParameter(paramId, value);         break;
        case Stage::Reverb:        mReverb->setParameter(paramId, value);        break;
        default:                                                                  break;
    }
}
