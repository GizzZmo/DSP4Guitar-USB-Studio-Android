# DSP4Guitar USB Studio (Android) 🎸⚡

![Android CI](https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android/actions/workflows/android-ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20NDK%20%7C%20API%2026%2B-brightgreen)
![C++ Standard](https://img.shields.io/badge/c%2B%2B-17-orange)

A high-performance, ultra-low-latency real-time guitar multi-effects processor engineered for Android devices. Designed to interface directly with class-compliant **USB Audio Class 2.0 (UAC 2.0)** interfaces over **USB-OTG**, bypassing the high-latency Android OS audio mixer via **Oboe / AAudio** in exclusive mode.

---

## ⚡ Features

- **Ultra-Low Latency Engine:** C++ Native Audio Engine built on **Oboe (AAudio/OpenSL ES)** using `SharingMode::Exclusive` and `PerformanceMode::LowLatency`.
- **Zero-Allocation Audio Thread:** Fully lock-free signal path adhering strictly to real-time C++ audio programming standards (no `malloc`, no `mutex`, no system calls in `processBlock`).
- **USB OTG Integration:** Direct USB Host connection with hardware audio interfaces supporting up to 24-bit / 96kHz.
- **DSP Effect Chain:**
  - Tube-modeled Amp Simulator & Cabinet IR Loader
  - Overdrive / Distortion with anti-aliasing oversampling
  - Parametric EQ, Chorus, Delay, and Reverb
  - Noise Gate & Chromatic Tuner
- **Hardware-Aware UI:** Cyberpunk / Terminal dark aesthetic optimized for high-contrast live performance visibility.

---

## 🛠️ Tech Stack

- **Core Audio Engine:** C++17, Google Oboe / JUCE DSP Framework
- **Build System:** CMake 3.22+, Gradle 8.x
- **Target Platform:** Android 8.0 (API Level 26) and higher
- **Architectures:** `arm64-v8a`, `armeabi-v7a`, `x86_64`

---

## 🚀 Quick Start & Building

### Prerequisites

1. **Android Studio** (Ladybug or newer)
2. **Android NDK** (r25c or newer)
3. **CMake** (3.22.1+)
4. A USB OTG cable and a Class-Compliant USB Audio Interface (e.g., Focusrite Scarlett, Behringer U-Phoria, IK Multimedia iRig HD).

### Build Instructions

```bash
# 1. Clone the repository with submodules
git clone --recursive [https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android.git](https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android.git)
cd DSP4Guitar-USB-Studio-Android

# 2. Copy environment template
cp .env.example .env

# 3. Build the Debug APK using Gradle Wrapper
./gradlew assembleDebug
