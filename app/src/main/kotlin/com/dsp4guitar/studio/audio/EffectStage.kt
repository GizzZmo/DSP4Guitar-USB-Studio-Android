package com.dsp4guitar.studio.audio

/**
 * Mirrors DspChain::Stage in C++.
 * The ordinal of each entry must match the integer index in the native code.
 */
enum class EffectStage(val displayName: String) {
    BITCRUSHER("Bitcrusher"),
    FUZZ("Fuzz"),
    MULTIBAND_COMP("Multiband Comp"),
    RING_MOD("Ring Mod"),
    AUTO_WAH("Auto Wah"),
    PHASER("Phaser"),
    CHORUS("Chorus"),
    TREMOLO("Tremolo"),
    DELAY("Delay"),
    REVERB("Reverb");

    val index: Int get() = ordinal
}
