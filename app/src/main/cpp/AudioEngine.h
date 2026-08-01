#pragma once

#include <oboe/Oboe.h>
#include <atomic>
#include <memory>
#include <string>

class DspChain;

/**
 * AudioEngine manages the Oboe audio stream and bridges the Android audio
 * subsystem to the DSP processing chain.
 *
 * Threading model:
 *  - All public API calls arrive from the UI thread (via JNI).
 *  - The Oboe callback fires on a dedicated real-time audio thread.
 *  - Parameter updates use std::atomic<> to avoid locks on the audio path.
 */
class AudioEngine : public oboe::AudioStreamDataCallback,
                    public oboe::AudioStreamErrorCallback {
public:
    AudioEngine();
    ~AudioEngine() override;

    // Lifecycle
    bool start();
    void stop();
    bool isRunning() const { return mIsRunning.load(std::memory_order_relaxed); }

    // Device selection (call from UI thread)
    void setAudioDeviceId(int32_t deviceId);

    // Gain staging
    void setInputGainDb(float db);
    void setOutputGainDb(float db);

    // Latency reporting (updated after each callback)
    double getEstimatedLatencyMs() const;

    // oboe::AudioStreamDataCallback
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream* stream,
                                          void* audioData,
                                          int32_t numFrames) override;

    // oboe::AudioStreamErrorCallback
    void onErrorAfterClose(oboe::AudioStream* stream, oboe::Result error) override;

    DspChain& getDspChain() { return *mDspChain; }

private:
    bool openStream();
    void closeStream();
    float dbToLinear(float db) const;

    std::shared_ptr<oboe::AudioStream> mStream;
    std::unique_ptr<DspChain>          mDspChain;

    std::atomic<bool>    mIsRunning{false};
    std::atomic<int32_t> mDeviceId{oboe::kUnspecified};
    std::atomic<float>   mInputGain{1.0f};
    std::atomic<float>   mOutputGain{1.0f};
    std::atomic<double>  mLatencyMs{0.0};

    int32_t mSampleRate{48000};
    int32_t mChannelCount{2};
    int32_t mBufferSizeFrames{256};
};
