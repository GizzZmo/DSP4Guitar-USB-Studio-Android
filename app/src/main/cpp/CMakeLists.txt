cmake_minimum_required(VERSION 3.22.1)
project(dsp4guitar VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Oboe (fetched by Gradle as an AAR; headers come via prefab) ──────────────
find_package(oboe REQUIRED CONFIG)

# ── Source files ─────────────────────────────────────────────────────────────
set(DSP4GUITAR_SOURCES
    AudioEngine.cpp
    DspChain.cpp
    dsp4guitar_jni.cpp
    effects/Bitcrusher.cpp
    effects/Fuzz.cpp
    effects/MultibandCompressor.cpp
    effects/RingModulator.cpp
    effects/AutoWah.cpp
    effects/Phaser.cpp
    effects/Chorus.cpp
    effects/Tremolo.cpp
    effects/Delay.cpp
    effects/Reverb.cpp
)

add_library(dsp4guitar SHARED ${DSP4GUITAR_SOURCES})

target_include_directories(dsp4guitar PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(dsp4guitar
    oboe::oboe
    android
    log
    OpenSLES
)

# Enable link-time optimisation for release builds
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(dsp4guitar PRIVATE -O3 -ffast-math)
    set_target_properties(dsp4guitar PROPERTIES INTERPROCEDURAL_OPTIMIZATION ON)
endif()
