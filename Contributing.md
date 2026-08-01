# Contributing to DSP4Guitar USB Studio (Android) 🎸💻

Welcome to the **DSP4Guitar USB Studio** developer collective. Our mission is to deliver professional, zero-latency guitar multi-effect processing to Android devices with a signature high-contrast cyberpunk visual identity. 

To achieve analog-feeling latency (~4–7 ms) over USB Audio Class (UAC) interfaces via Google Oboe and JUCE, our native C++ DSP engine operates under extreme real-time constraints. **A single misplaced line of code in the audio callback can cause buffer underruns (xruns), audio pops, or complete system lockups.**

Before submitting code, you must read, understand, and strictly adhere to the real-time audio engineering commandments and architectural guidelines outlined below.

---

## ⚠️ The Golden Rules of Real-Time Audio

The audio callback (`processBlock` or Oboe audio stream callback) executes on a dedicated high-priority real-time thread. If this thread is delayed by even a few microseconds, the audio buffer will empty before the next frame is computed, causing a loud audible glitch. 

### The "Never" List inside `processBlock`:

1. **NO Memory Allocation or Deallocation:** Never call `new`, `delete`, `malloc`, `free`, or use containers that dynamically resize (e.g., `std::vector::push_back`, `std::string` concatenation, or inserting into `std::map`).
   * *Why:* Heap allocation forces the thread to query the OS memory manager, which can take an unpredictable amount of time and acquire global system locks.
   * *Solution:* Pre-allocate all buffers, delay lines, and workspace arrays inside `prepareToPlay()` or during object construction.
2. **NO Locks or Mutexes:** Never use `std::mutex`, `std::lock_guard`, `CRITICAL_SECTION`, or any blocking synchronization primitives.
   * *Why:* If the real-time audio thread attempts to acquire a mutex currently held by the low-priority GUI thread (e.g., during a screen repaint), **priority inversion** occurs. The audio thread will stall, guaranteeing an xrun.
   * *Solution:* Use lock-free atomic variables (`std::atomic<float>`, `juce::Atomic`) for simple parameter updates, or lock-free FIFO queues (`juce::AbstractFifo`) for complex UI-to-audio telemetry.
3. **NO System Calls or I/O:** Never read/write to files, execute network operations, or call console print statements (`std::cout`, `printf`, `DBG()`, or `LOGD()`).
   * *Why:* Disk and terminal I/O depend on hardware controllers and kernel scheduling, taking milliseconds to resolve.
   * *Solution:* Push logging metrics or waveform data to a lock-free ring buffer, and let a background timer thread read from the buffer and print to the Android logcat.
4. **NO JNI / Java / Kotlin Calls:** Never invoke Java Native Interface (JNI) methods from the real-time C++ thread.
   * *Why:* Traversing the JNI bridge risks triggering the Android ART Garbage Collector (GC), which can suspend execution threads unpredictably.
5. **NO Unbounded Algorithmic Complexity:** The CPU cycles required to execute your DSP code must be deterministic and constant ($O(1)$ or strict bounded $O(N)$ based on buffer size). Never use `while` loops with data-dependent exit conditions.

---

## 🧠 Memory & Parameter Synchronization

Since UI knobs (touch events) and audio processing live on completely separate threads, parameter modulation must be handled lock-free.

### Good vs. Bad Parameter Reading

```cpp
// ❌ BAD: Blocking the audio thread with a lock or direct tree lookup
void MyDspBlock::processBlock (juce::AudioBuffer<float>& buffer, juce::MidiBuffer&)
{
    std::lock_guard<std::mutex> lock (myMutex); // FATAL: Will cause dropouts!
    float gain = valueTreeState.getParameter("GAIN")->getValue(); // FATAL: String lookup & virtual dispatch!
}

// ✅ GOOD: Reading a pre-fetched atomic pointer
void MyDspBlock::processBlock (juce::AudioBuffer<float>& buffer, juce::MidiBuffer&)
{
    // Atomic read is lock-free and resolves in a single CPU cycle
    const float currentGain = gainParameter->get(); 
    
    // Apply gain smoothly using JUCE's smoothed values to prevent zipper noise
    gainSmoother.setTargetValue (currentGain);
    
    for (int channel = 0; channel < buffer.getNumChannels(); ++channel)
    {
        auto* channelData = buffer.getWritePointer (channel);
        for (int sample = 0; sample < buffer.getNumSamples(); ++sample)
        {
            channelData[sample] *= gainSmoother.getNextValue();
        }
    }
}
