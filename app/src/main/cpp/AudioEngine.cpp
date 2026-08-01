#include "AudioEngine.h"
#include "DspChain.h"

#include <android/log.h>
#include <cmath>

#define LOG_TAG "AudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

AudioEngine::AudioEngine()
    : mDspChain(std::make_unique<DspChain>()) {}

AudioEngine::~AudioEngine() {
    stop();
}

bool AudioEngine::start() {
    if (mIsRunning.load()) return true;
    if (!openStream()) return false;
    mIsRunning.store(true);
    LOGI("Audio engine started. SR=%d, ch=%d, buf=%d",
         mSampleRate, mChannelCount, mBufferSizeFrames);
    return true;
}

void AudioEngine::stop() {
    if (!mIsRunning.load()) return;
    mIsRunning.store(false);
    closeStream();
    LOGI("Audio engine stopped.");
}

void AudioEngine::setAudioDeviceId(int32_t deviceId) {
    mDeviceId.store(deviceId);
    if (mIsRunning.load()) {
        stop();
        start();
    }
}

void AudioEngine::setInputGainDb(float db) {
    mInputGain.store(dbToLinear(db));
}

void AudioEngine::setOutputGainDb(float db) {
    mOutputGain.store(dbToLinear(db));
}

double AudioEngine::getEstimatedLatencyMs() const {
    return mLatencyMs.load(std::memory_order_relaxed);
}

// ── Oboe callbacks ───────────────────────────────────────────────────────────

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream* /*stream*/,
                                                    void* audioData,
                                                    int32_t numFrames) {
    auto* samples = static_cast<float*>(audioData);

    // Apply input gain
    const float inGain  = mInputGain.load(std::memory_order_relaxed);
    const float outGain = mOutputGain.load(std::memory_order_relaxed);
    const int   total   = numFrames * mChannelCount;

    for (int i = 0; i < total; ++i) {
        samples[i] *= inGain;
    }

    // Process through DSP chain
    mDspChain->process(samples, numFrames, mChannelCount);

    // Apply output gain
    for (int i = 0; i < total; ++i) {
        samples[i] *= outGain;
    }

    // Update latency estimate
    if (mStream) {
        auto result = mStream->calculateLatencyMillis();
        if (result) {
            mLatencyMs.store(result.value(), std::memory_order_relaxed);
        }
    }

    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream* /*stream*/, oboe::Result error) {
    LOGE("Stream error: %s — attempting restart", oboe::convertToText(error));
    mIsRunning.store(false);
    start();
}

// ── Private helpers ──────────────────────────────────────────────────────────

bool AudioEngine::openStream() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
           .setPerformanceMode(oboe::PerformanceMode::LowLatency)
           .setSharingMode(oboe::SharingMode::Exclusive)
           .setFormat(oboe::AudioFormat::Float)
           .setChannelCount(mChannelCount)
           .setSampleRate(mSampleRate)
           .setDataCallback(this)
           .setErrorCallback(this);

    if (mDeviceId.load() != oboe::kUnspecified) {
        builder.setDeviceId(mDeviceId.load());
    }

    auto result = builder.openStream(mStream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to open stream: %s", oboe::convertToText(result));
        return false;
    }

    mSampleRate     = mStream->getSampleRate();
    mChannelCount   = mStream->getChannelCount();
    mBufferSizeFrames = mStream->getFramesPerBurst() * 2;
    mStream->setBufferSizeInFrames(mBufferSizeFrames);

    mDspChain->setSampleRate(static_cast<float>(mSampleRate));

    auto startResult = mStream->requestStart();
    if (startResult != oboe::Result::OK) {
        LOGE("Failed to start stream: %s", oboe::convertToText(startResult));
        return false;
    }

    return true;
}

void AudioEngine::closeStream() {
    if (mStream) {
        mStream->stop();
        mStream->close();
        mStream.reset();
    }
}

float AudioEngine::dbToLinear(float db) const {
    return std::pow(10.0f, db / 20.0f);
}
