#include <jni.h>
#include <android/log.h>
#include <memory>

#include "AudioEngine.h"
#include "DspChain.h"

#define LOG_TAG "DSP4Guitar_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ── Singleton engine instance ─────────────────────────────────────────────────
static std::unique_ptr<AudioEngine> gEngine;

extern "C" {

// ── Lifecycle ─────────────────────────────────────────────────────────────────

JNIEXPORT jboolean JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_create(JNIEnv*, jobject) {
    if (!gEngine) {
        gEngine = std::make_unique<AudioEngine>();
    }
    return static_cast<jboolean>(true);
}

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_destroy(JNIEnv*, jobject) {
    gEngine.reset();
}

JNIEXPORT jboolean JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_start(JNIEnv*, jobject) {
    if (!gEngine) return static_cast<jboolean>(false);
    return static_cast<jboolean>(gEngine->start());
}

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_stop(JNIEnv*, jobject) {
    if (gEngine) gEngine->stop();
}

JNIEXPORT jboolean JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_isRunning(JNIEnv*, jobject) {
    return static_cast<jboolean>(gEngine && gEngine->isRunning());
}

// ── Device selection ─────────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_setAudioDeviceId(JNIEnv*, jobject,
                                                                   jint deviceId) {
    if (gEngine) gEngine->setAudioDeviceId(static_cast<int32_t>(deviceId));
}

// ── Gain staging ─────────────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_setInputGainDb(JNIEnv*, jobject,
                                                                 jfloat db) {
    if (gEngine) gEngine->setInputGainDb(db);
}

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_setOutputGainDb(JNIEnv*, jobject,
                                                                  jfloat db) {
    if (gEngine) gEngine->setOutputGainDb(db);
}

JNIEXPORT jdouble JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_getEstimatedLatencyMs(JNIEnv*, jobject) {
    if (!gEngine) return 0.0;
    return static_cast<jdouble>(gEngine->getEstimatedLatencyMs());
}

// ── DSP chain control ─────────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_setEffectBypass(JNIEnv*, jobject,
                                                                  jint stageIndex,
                                                                  jboolean bypass) {
    if (!gEngine) return;
    const auto stage = static_cast<DspChain::Stage>(stageIndex);
    gEngine->getDspChain().setBypass(stage, static_cast<bool>(bypass));
}

JNIEXPORT jboolean JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_isEffectBypassed(JNIEnv*, jobject,
                                                                   jint stageIndex) {
    if (!gEngine) return static_cast<jboolean>(true);
    const auto stage = static_cast<DspChain::Stage>(stageIndex);
    return static_cast<jboolean>(gEngine->getDspChain().isBypassed(stage));
}

JNIEXPORT void JNICALL
Java_com_dsp4guitar_studio_audio_AudioEngineJni_setEffectParameter(JNIEnv*, jobject,
                                                                     jint stageIndex,
                                                                     jint paramId,
                                                                     jfloat value) {
    if (!gEngine) return;
    const auto stage = static_cast<DspChain::Stage>(stageIndex);
    gEngine->getDspChain().setParameter(stage, static_cast<int>(paramId), value);
}

} // extern "C"
