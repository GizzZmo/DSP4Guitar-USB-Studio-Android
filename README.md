# DSP4Guitar USB Studio (Android) 🎸⚡

![Android](https://img.shields.io/badge/Android-8.0%2B-00FF41?style=for-the-badge&logo=android&logoColor=black)
![C++](https://img.shields.io/badge/C%2B%2B-17-00FF41?style=for-the-badge&logo=c%2B%2B&logoColor=black)
![JUCE](https://img.shields.io/badge/JUCE-Framework-00FF41?style=for-the-badge)
![Audio Engine](https://img.shields.io/badge/Audio-Oboe%20%2F%20AAudio-00FF41?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-00FF41?style=for-the-badge)

**DSP4Guitar USB Studio** transforms your Android device into a portable, professional-grade guitar rig and multi-effect processing studio. Built on top of a high-performance C++ DSP engine and engineered for mobile workflows, it bridges real-time studio audio processing with plug-and-play USB hardware integration.

Designed for guitarists, sound designers, and audio engineers who demand studio-quality tone on the go, the app delivers low-latency processing over **USB Audio Class (UAC)** interfaces via **Google Oboe and AAudio**. Whether practising with an OTG guitar interface, tracking mobile demos, or performing live, DSP4Guitar delivers a zero-latency feel without sacrificing signal fidelity.

All of this is wrapped in a high-contrast **cyberpunk / Matrix-terminal visual identity** — featuring neon-green (`#00FF41`) accents, glowing LED toggles, a scrolling matrix-rain header animation, and custom touchscreen-optimised rotary controls.

---

## ✨ Key Features

* **10-Stage Fixed Signal Chain** — Independently bypassable studio-grade processing blocks tailored for guitar and bass:

  ```
  Bitcrusher → Fuzz → Multiband Comp → Ring Mod → Auto Wah
             → Phaser → Chorus → Tremolo → Delay → Reverb
  ```

* **Plug-and-Play USB Host / OTG** — Direct integration with Class-Compliant (UAC 2.0) USB audio interfaces, bypassing the standard Android OS mixer latency.
* **Native C++ Engine** — Powered by Google Oboe, binding audio threads directly to high-performance CPU cores (P-cores) to prevent thermal throttling and buffer underruns (xruns).
* **Touch-Optimised Cyberpunk UI** — Custom linear vertical/horizontal drag mapping for rotary knobs (no frustrating circular gestures on small screens), double-tap to reset to default, and haptic feedback on parameter detents.
* **Stage Mode & Metering** — Instant high-contrast luminescence boost for outdoor/stage visibility, paired with pre- and post-chain LED VU meters for precise gain staging.
* **Persistent Session State** — Automatically saves and restores preset chains, parameter values, and input/output routing between sessions.

---

## 🎛️ Hardware Compatibility Matrix

DSP4Guitar USB Studio works best with USB Class-Compliant (UAC 2.0) audio interfaces connected via a USB-C On-The-Go (OTG) adapter or a direct USB-C cable.

| Interface Brand / Model | Connection Type | Tested Latency | Status |
| :--- | :--- | :--- | :--- |
| **iRig HD 2 / HD X** | USB-C OTG | ~4–6 ms | 🟢 Fully Supported |
| **Focusrite Scarlett 2i2 (3rd/4th Gen)** | USB-C (Powered Hub) | ~5–8 ms | 🟢 Fully Supported |
| **Zoom GCE-3 / UAC-2** | USB-C OTG | ~4–7 ms | 🟢 Fully Supported |
| **Behringer U-PHORIA UMC202HD** | USB-C OTG | ~6–9 ms | 🟢 Fully Supported |
| **Generic USB-C to 3.5mm Dongles** | USB-C Direct | ~12–20 ms | 🟡 High Latency / Fallback |

> **Note on Power:** High-draw multichannel interfaces (e.g. Scarlett series) may require a powered USB-C hub to prevent battery drain or OS power-limit disconnections.

---

## 🏗️ Architecture & Audio Engine

The application uses a split-architecture model separating the graphical user interface from the real-time audio thread:

```
┌─────────────────────────────────┐
│  UI Layer  (Kotlin / Jetpack)   │  Touch events, animation, presets
└───────────────┬─────────────────┘
                │  Lock-free ring buffer (JNI)
┌───────────────▼─────────────────┐
│  JNI Bridge  (C++ / JNI)        │  Parameter transfer, lifecycle
└───────────────┬─────────────────┘
                │  Exclusive AAudio stream
┌───────────────▼─────────────────┐
│  DSP Engine  (C++ / Oboe)       │  10-stage signal chain @ RT priority
└─────────────────────────────────┘
```

1. **UI Layer (Kotlin / Jetpack Compose):** Handles touch events, matrix animation rendering, preset management, and parameter telemetry.
2. **JNI / Bridge Layer:** Transmits lock-free parameter updates from the UI to the DSP engine without garbage-collection pauses.
3. **Audio Engine Core (C++ / Oboe):** Requests exclusive, low-latency audio streams from Android's AAudio driver (falling back to OpenSL ES on older devices). Audio callbacks execute on dedicated real-time threads.

---

## 🚀 Getting Started & Build Instructions

### Prerequisites

* **Android Studio:** Hedgehog (2023.1.1) or newer
* **Android NDK:** `r26` or newer (installed via SDK Manager)
* **CMake:** `3.22.1` or newer (installed via SDK Manager)
* **Android SDK:** API level 26 (Android 8.0) minimum, API 35 target
* **Git:** with submodule support

### Clone & Setup

```bash
git clone https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android.git
cd DSP4Guitar-USB-Studio-Android
git submodule update --init --recursive   # pulls Oboe and other deps
```

### Build

Open the project in Android Studio and sync Gradle, or build from the command line:

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (requires signing config)
```

The APK is output to `app/build/outputs/apk/`.

### USB OTG Setup (Device)

1. Enable **Developer Options** on your Android device.
2. Connect your USB audio interface via a USB-C OTG adapter.
3. When prompted, grant DSP4Guitar USB permission — this persists across sessions.
4. Select the interface in **Settings → Audio Device** within the app.

---

## 📁 Project Structure

```
DSP4Guitar-USB-Studio-Android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml          USB host + audio permissions
│   │       ├── cpp/
│   │       │   ├── CMakeLists.txt
│   │       │   ├── AudioEngine.h / .cpp     Oboe stream management
│   │       │   ├── DspChain.h / .cpp        10-stage signal chain
│   │       │   ├── effects/                 Individual DSP effect classes
│   │       │   └── dsp4guitar_jni.cpp       JNI entry points
│   │       ├── kotlin/com/dsp4guitar/studio/
│   │       │   ├── MainActivity.kt          Entry point + navigation host
│   │       │   ├── ui/                      Compose screens & components
│   │       │   ├── viewmodel/               ViewModels for each screen
│   │       │   └── usb/                     USB device detection & routing
│   │       └── res/                         Themes, strings, drawables
│   └── build.gradle.kts
├── CMakeLists.txt                           Root CMake (delegates to app/src/main/cpp)
├── build.gradle.kts                         Root Gradle
├── settings.gradle.kts
└── README.md
```

---

## 🎨 UI Design Tokens (Cyberpunk Theme)

| Token | Value | Usage |
| :--- | :--- | :--- |
| `MatrixGreen` | `#00FF41` | Primary accent, active indicators |
| `MatrixGreenDim` | `#00CC34` | Pressed / hover states |
| `Background` | `#0A0A0A` | App background |
| `Surface` | `#111111` | Cards, panels |
| `OnSurface` | `#E0E0E0` | Body text |
| `Warning` | `#FFD700` | High-latency / fallback indicators |
| `Error` | `#FF3131` | Clip / xrun alerts |

---

## 🤝 Contributing

Pull requests are welcome. For major changes please open an issue first to discuss what you would like to change. Please ensure all C++ code follows the [LLVM Coding Standards](https://llvm.org/docs/CodingStandards.html) and all Kotlin code passes `./gradlew ktlintCheck`.

---

## 📄 License

```
MIT License

Copyright (c) 2024 GizzZmo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

