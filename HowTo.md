# How to Dial in Zero-Latency Tone with DSP4Guitar USB Studio 🎛️⚡

Getting real-time, zero-latency guitar processing on Android requires a proper physical connection and correct OS-level audio routing. Follow this step-by-step guide to configure your rig without audio glitches, feedback loops, or latency spikes.

---

## Step 1: The Physical Hardware Setup

Never rely on your Android device's built-in 3.5mm headphone jack or basic Bluetooth audio for real-time playing — Bluetooth introduces 100ms+ of unplayable delay, and standard internal OS audio routing adds unwanted buffering.

1. **Get an OTG Adapter:** Use a high-quality **USB-C On-The-Go (OTG) adapter** or a direct USB-C to USB-C / USB-B cable.
2. **Connect the Interface:** Plug your USB Audio Class 2.0 (UAC 2.0) interface (e.g., iRig HD X, Focusrite Scarlett, Zoom GCE-3) into your Android device.
3. **Power Delivery:** If using a multi-channel interface with phantom power or high current draw, power your interface via an powered USB hub or external DC adapter first so your Android phone doesn't terminate the USB connection to save battery.
4. **Connect Monitoring:** Plug your headphones or studio monitors directly into the **output jack of the USB audio interface** — never into the phone's speaker or Bluetooth.

---

## Step 2: Granting Exclusive OS Permissions

When you launch DSP4Guitar USB Studio with an interface connected, Android will trigger an OS system prompt.

1. **Accept USB Host Access:** When prompted with *"Allow DSP4Guitar Studio to access [Your Interface Name]?"*, check the box for **Always allow** and tap **OK**.
2. **Verify Exclusive Routing:** Look at the top status bar in the app. The indicator should switch from `AUDIO: INTERNAL (Fallback)` to `AUDIO: UAC EXCLUSIVE (Oboe/AAudio)`. 
3. *Troubleshooting Note:* If the indicator remains on Fallback, unplug the USB cable, close the app completely from your Android recents menu, re-plug the cable, and launch the app again from the OS USB prompt.

---

## Step 3: Tuning the Oboe Audio Engine (Buffer & Sample Rate)

To achieve that snappy, analog-feeling response (~4–7 ms latency), you need to balance your device's CPU processing power against the audio buffer size.

1. Tap the **⚙️ Engine Settings** icon in the top right corner of the Matrix header.
2. **Set Sample Rate:** Select **48000 Hz (48 kHz)**. This is the native hardware sample rate for 99% of modern Android chipsets and USB interfaces; using 44.1 kHz on a 48 kHz native device forces the Android OS to run a sample rate converter, adding CPU load and latency.
3. **Select Buffer Size:**
   * **64 Samples (~4.5 ms total latency):** Recommended for flagship Snapdragon 8 Gen 1+ or Google Pixel 7+ devices.
   * **128 Samples (~6.5 ms total latency):** The recommended sweet spot for stable performance across most modern devices.
   * **256 Samples (~11.0 ms total latency):** Use this setting if you hear audio crackling, pops, or dropouts (known as *xruns*) during heavy reverb and multiband compression calculation.
4. **Enable P-Core Binding:** Ensure the toggle for **Bind Audio Thread to Performance Cores** is switched **ON**. This prevents Android from handing your DSP calculations over to low-power efficiency cores.

---

## Step 4: Gain Staging Your Signal Chain

Proper gain staging prevents harsh digital clipping before your guitar signal hits the tone-shaping modules.

1. **Physical Preamp Gain:** Turn all digital effects inside the app to **BYPASS**. Play your guitar at your maximum strumming intensity and turn up the physical gain knob on your USB interface until the app's top **Input VU Meter** peaks solidly in the **Yellow (-6 dB to -3 dB)** zone. If the meter hits solid **Red (0 dB)**, dial the physical knob back.
2. **Dynamic Processing:** Turn on the **Multiband Compressor** first to level out your playing dynamics without squashing your attack.
3. **Drive & Modulation:** Engage the **Bitcrusher** or **Fuzz** modules. Notice that our touch-optimized knobs use **Linear Drag**: touch a knob and slide your finger **up/right** to increase intensity, or **down/left** to decrease. Double-tap any knob to instantly reset it to unity gain / default.
4. **Time-Based Effects:** Add **Delay** and **Reverb** at the end of the chain. If you notice your output VU meter clipping after adding heavy reverb tails, lower the Master Output knob on the right side of the top bar.

---

## Step 5: Saving & Recalling Matrix Presets

Once you have crafted your signature cyberpunk tone:

1. Tap the **💾 PRESETS** terminal button in the footer.
2. Tap an empty slot (e.g., `SLOT_04: [EMPTY]`), type a name (e.g., `NEON_LEAD_808`), and hit **SAVE STATE**.
3. All knob positions, bypass states, and routing configurations are instantly written to local persistent memory and will reload automatically on your next session.
