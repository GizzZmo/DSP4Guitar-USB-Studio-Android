# DSP4Guitar USB Studio (Android) 🎸⚡

![Android](https://img.shields.io/badge/Android-8.0%2B-00FF41?style=for-the-badge&logo=android&logoColor=black)
![C++](https://img.shields.io/badge/C%2B%2B-17-00FF41?style=for-the-badge&logo=c%2B%2B&logoColor=black)
![JUCE](https://img.shields.io/badge/JUCE-Framework-00FF41?style=for-the-badge)
![Audio Engine](https://img.shields.io/badge/Audio-Oboe%20%2F%20AAudio-00FF41?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-00FF41?style=for-the-badge)

**DSP4Guitar USB Studio** transforms your Android device into a portable, professional-grade guitar rig and multi-effect processing studio. Built on top of the high-performance C++ **DSP4Guitar** engine and engineered for mobile workflows, it bridges real-time studio audio processing with plug-and-play USB hardware integration.

Designed for guitarists, sound designers, and audio engineers who demand studio-quality tone on the go, the app delivers low-latency processing over **USB Audio Class (UAC)** interfaces via **Google Oboe and AAudio**. Whether practicing with an OTG guitar interface, tracking mobile demos, or performing live, DSP4Guitar delivers a zero-latency feel without sacrificing signal fidelity.

All of this is wrapped in a high-contrast **cyberpunk / Matrix-terminal visual identity** — featuring neon-green (`#00FF41`) accents, glowing LED toggles, a scrolling matrix-rain header animation, and custom touchscreen-optimized rotary controls.

---

## ✨ Key Features

* **10-Stage Fixed Signal Chain:** Independently bypassable studio-grade processing blocks tailored for guitar and bass:
  $$\text{Bitcrusher} \rightarrow \text{Fuzz} \rightarrow \text{Multiband Comp} \rightarrow \text{Ring Mod} \rightarrow \text{Auto Wah} \rightarrow \text{Phaser} \rightarrow \text{Chorus} \rightarrow \text{Tremolo} \rightarrow \text{Delay} \rightarrow \text{Reverb}$$
* **Plug-and-Play USB Host / OTG:** Direct integration with Class-Compliant (UAC 2.0) USB audio interfaces, bypassing standard Android OS mixer latency.
* **Native C++ Engine:** Powered by JUCE and Google Oboe, binding audio threads directly to high-performance CPU cores (P-cores) to prevent thermal throttling and buffer underruns (xruns).
* **Touch-Optimized Cyberpunk UI:** Custom linear vertical/horizontal drag mapping for rotary knobs (no frustrating circular drag gestures on small screens), double-tap to reset to unity/default, and haptic feedback on parameter detents.
* **Stage Mode & Metering:** Instant high-contrast luminescence boost for outdoor/stage visibility, paired with pre- and post-chain LED VU meters for precise gain staging.
* **Persistent Session State:** Automatically saves and restores customized preset chains, parameter automation, and input/output routing between sessions.

---

## 🎛️ Hardware Compatibility Matrix

DSP4Guitar USB Studio works best with USB Class-Compliant (UAC 2.0) audio interfaces connected via a USB-C On-The-Go (OTG) adapter or direct USB-C cable.

| Interface Brand / Model | Connection Type | Tested Latency | Compatibility Status |
| :--- | :--- | :--- | :--- |
| **iRig HD 2 / HD X** | USB-C OTG | ~4–6 ms | 🟢 Fully Supported |
| **Focusrite Scarlett 2i2 (3rd/4th Gen)** | USB-C (External Power / Powered Hub) | ~5–8 ms | 🟢 Fully Supported |
| **Zoom GCE-3 / UAC-2** | USB-C OTG | ~4–7 ms | 🟢 Fully Supported |
| **Behringer U-PHORIA UMC202HD** | USB-C OTG | ~6–9 ms | 🟢 Fully Supported |
| **Generic USB-C to 3.5mm Dongles** | USB-C Direct | ~12–20 ms | 🟡 High Latency / Fallback |

> **Note on Power:** High-draw multichannel interfaces (like the Scarlett series) may require a powered USB-C hub to prevent draining your Android device's battery or triggering OS power-limit disconnections.

---

## 🏗️ Architecture & Audio Engine

The application relies on a split-architecture model separating the graphical user interface from the real-time audio thread:

1. **UI Layer (Android / Java / Kotlin / JUCE GUI):** Handles touch events, matrix animation rendering, preset management, and parameter telemetry.
2. **JNI / Bridge Layer:** Transmits lock-free parameter updates from the UI to the DSP engine without causing garbage collection pauses.
3. **Audio Engine Core (C++ / Oboe / JUCE DSP):** Requests exclusive, low-latency audio streams from Android's AAudio driver (falling back to OpenSL ES on older legacy devices). Audio callbacks execute directly on dedicated real-time threads.

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
* **Android Studio:** Koala (2024.1.1) or newer.
* **Android NDK:** Version `r25c` or newer (required for modern Oboe and JUCE C++17 compatibility).
* **CMake:** Version `3.22+` (installed via Android Studio SDK Manager).
* **Git:** For cloning submodules (JUCE and Oboe).

### Local Compilation Steps

1. **Clone the Repository (with Submodules):**
   ```bash
   git clone --recursive [https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android.git](https://github.com/GizzZmo/DSP4Guitar-USB-Studio-Android.git)
   cd DSP4Guitar-USB-Studio-Android
