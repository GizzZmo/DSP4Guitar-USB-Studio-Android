package com.example.dsp.model

object FactoryPresets {

    fun getDefaultChain(): List<EffectUnit> {
        return listOf(
            EffectUnit(
                id = "gate_1",
                type = EffectType.NOISE_GATE,
                name = "Noise Gate",
                enabled = true,
                parameters = listOf(
                    EffectParameter("threshold", "Threshold", -45.0f, -80.0f, -10.0f, "dB"),
                    EffectParameter("release", "Release", 100.0f, 10.0f, 500.0f, "ms")
                )
            ),
            EffectUnit(
                id = "drive_1",
                type = EffectType.OVERDRIVE,
                name = "TS-808 Tube Drive",
                enabled = true,
                parameters = listOf(
                    EffectParameter("drive", "Drive", 0.45f, 0.0f, 1.0f),
                    EffectParameter("tone", "Tone", 0.65f, 0.0f, 1.0f),
                    EffectParameter("level", "Level", 0.8f, 0.0f, 1.0f)
                )
            ),
            EffectUnit(
                id = "amp_1",
                type = EffectType.AMP_SIM,
                name = "Plexi 100W Amp",
                enabled = true,
                parameters = listOf(
                    EffectParameter("gain", "Gain", 0.6f, 0.0f, 1.0f),
                    EffectParameter("bass", "Bass", 0.55f, 0.0f, 1.0f),
                    EffectParameter("middle", "Middle", 0.65f, 0.0f, 1.0f),
                    EffectParameter("treble", "Treble", 0.70f, 0.0f, 1.0f),
                    EffectParameter("presence", "Presence", 0.50f, 0.0f, 1.0f),
                    EffectParameter("master", "Master", 0.85f, 0.0f, 1.0f)
                )
            ),
            EffectUnit(
                id = "cab_1",
                type = EffectType.CAB_IR,
                name = "4x12 V30 Cab IR",
                enabled = true,
                parameters = listOf(
                    EffectParameter("cab_type", "Type", 0.8f, 0.0f, 1.0f),
                    EffectParameter("mic_pos", "Mic Pos", 0.5f, 0.0f, 1.0f)
                )
            ),
            EffectUnit(
                id = "chorus_1",
                type = EffectType.CHORUS,
                name = "Stereo Chorus",
                enabled = false,
                parameters = listOf(
                    EffectParameter("rate", "Rate", 1.2f, 0.1f, 5.0f, "Hz"),
                    EffectParameter("depth", "Depth", 4.0f, 1.0f, 10.0f, "ms"),
                    EffectParameter("mix", "Mix", 0.35f, 0.0f, 1.0f)
                )
            ),
            EffectUnit(
                id = "delay_1",
                type = EffectType.DELAY,
                name = "Tape Echo Delay",
                enabled = true,
                parameters = listOf(
                    EffectParameter("delay_ms", "Time", 320.0f, 50.0f, 1000.0f, "ms"),
                    EffectParameter("feedback", "Feedback", 0.35f, 0.0f, 0.95f),
                    EffectParameter("mix", "Mix", 0.30f, 0.0f, 1.0f)
                )
            ),
            EffectUnit(
                id = "reverb_1",
                type = EffectType.REVERB,
                name = "Studio Reverb",
                enabled = true,
                parameters = listOf(
                    EffectParameter("room_size", "Room", 0.60f, 0.1f, 1.0f),
                    EffectParameter("mix", "Mix", 0.25f, 0.0f, 1.0f)
                )
            )
        )
    }

    fun getFactoryPresets(): List<PresetData> {
        return listOf(
            PresetData(
                title = "Classic British Lead",
                category = "Rock",
                description = "Warm TS-808 overdrive boost driving a 100W British Tube Amp with tape echo delay",
                effects = getDefaultChain()
            ),
            PresetData(
                title = "Heavy Metal Chug",
                category = "Metal",
                description = "Tight Noise Gate + Extreme Distortion + High Gain Scoop + V30 4x12 Cab",
                effects = listOf(
                    EffectUnit(
                        id = "gate_2",
                        type = EffectType.NOISE_GATE,
                        name = "Tight Gate",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("threshold", "Threshold", -35.0f, -80.0f, -10.0f, "dB"),
                            EffectParameter("release", "Release", 60.0f, 10.0f, 500.0f, "ms")
                        )
                    ),
                    EffectUnit(
                        id = "dist_2",
                        type = EffectType.DISTORTION,
                        name = "High Gain Metal Drive",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("gain", "Gain", 0.85f, 0.0f, 1.0f),
                            EffectParameter("contour", "Scoop", 0.70f, 0.0f, 1.0f),
                            EffectParameter("level", "Level", 0.80f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "amp_2",
                        type = EffectType.AMP_SIM,
                        name = "Modern High Gain Rectifier",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("gain", "Gain", 0.80f, 0.0f, 1.0f),
                            EffectParameter("bass", "Bass", 0.75f, 0.0f, 1.0f),
                            EffectParameter("middle", "Middle", 0.35f, 0.0f, 1.0f),
                            EffectParameter("treble", "Treble", 0.80f, 0.0f, 1.0f),
                            EffectParameter("presence", "Presence", 0.70f, 0.0f, 1.0f),
                            EffectParameter("master", "Master", 0.80f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "cab_2",
                        type = EffectType.CAB_IR,
                        name = "4x12 V30 Metal Cab",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("cab_type", "Type", 0.9f, 0.0f, 1.0f),
                            EffectParameter("mic_pos", "Mic Pos", 0.6f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "eq_2",
                        type = EffectType.GRAPHIC_EQ,
                        name = "V-Curve Scoop EQ",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("eq_60", "60Hz", 1.4f, 0.25f, 2.0f),
                            EffectParameter("eq_150", "150Hz", 1.3f, 0.25f, 2.0f),
                            EffectParameter("eq_400", "400Hz", 0.6f, 0.25f, 2.0f),
                            EffectParameter("eq_1k", "1kHz", 0.5f, 0.25f, 2.0f),
                            EffectParameter("eq_2k4", "2.4kHz", 1.2f, 0.25f, 2.0f),
                            EffectParameter("eq_6k", "6kHz", 1.4f, 0.25f, 2.0f),
                            EffectParameter("eq_15k", "15kHz", 1.2f, 0.25f, 2.0f)
                        )
                    )
                )
            ),
            PresetData(
                title = "80s Lush Stereo Clean",
                category = "Clean",
                description = "Sparkling clean amp tone with deep stereo chorus, ping-pong delay and lush hall reverb",
                effects = listOf(
                    EffectUnit(
                        id = "amp_3",
                        type = EffectType.AMP_SIM,
                        name = "Twin Clean Tube Amp",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("gain", "Gain", 0.25f, 0.0f, 1.0f),
                            EffectParameter("bass", "Bass", 0.60f, 0.0f, 1.0f),
                            EffectParameter("middle", "Middle", 0.50f, 0.0f, 1.0f),
                            EffectParameter("treble", "Treble", 0.75f, 0.0f, 1.0f),
                            EffectParameter("presence", "Presence", 0.60f, 0.0f, 1.0f),
                            EffectParameter("master", "Master", 0.90f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "cab_3",
                        type = EffectType.CAB_IR,
                        name = "2x12 Open Back Cab",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("cab_type", "Type", 0.4f, 0.0f, 1.0f),
                            EffectParameter("mic_pos", "Mic Pos", 0.4f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "chorus_3",
                        type = EffectType.CHORUS,
                        name = "Stereo Chorus",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("rate", "Rate", 1.8f, 0.1f, 5.0f, "Hz"),
                            EffectParameter("depth", "Depth", 6.0f, 1.0f, 10.0f, "ms"),
                            EffectParameter("mix", "Mix", 0.55f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "delay_3",
                        type = EffectType.DELAY,
                        name = "Stereo Delay",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("delay_ms", "Time", 420.0f, 50.0f, 1000.0f, "ms"),
                            EffectParameter("feedback", "Feedback", 0.45f, 0.0f, 0.95f),
                            EffectParameter("mix", "Mix", 0.40f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "reverb_3",
                        type = EffectType.REVERB,
                        name = "Hall Reverb",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("room_size", "Room", 0.85f, 0.1f, 1.0f),
                            EffectParameter("mix", "Mix", 0.45f, 0.0f, 1.0f)
                        )
                    )
                )
            ),
            PresetData(
                title = "Texas Blues Breakup",
                category = "Blues",
                description = "Warm dynamic crunch with TS boost driving a Tweed Combo",
                effects = listOf(
                    EffectUnit(
                        id = "drive_4",
                        type = EffectType.OVERDRIVE,
                        name = "TS Blues Drive",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("drive", "Drive", 0.35f, 0.0f, 1.0f),
                            EffectParameter("tone", "Tone", 0.55f, 0.0f, 1.0f),
                            EffectParameter("level", "Level", 0.85f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "amp_4",
                        type = EffectType.AMP_SIM,
                        name = "59 Tweed Combo",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("gain", "Gain", 0.50f, 0.0f, 1.0f),
                            EffectParameter("bass", "Bass", 0.65f, 0.0f, 1.0f),
                            EffectParameter("middle", "Middle", 0.60f, 0.0f, 1.0f),
                            EffectParameter("treble", "Treble", 0.60f, 0.0f, 1.0f),
                            EffectParameter("presence", "Presence", 0.45f, 0.0f, 1.0f),
                            EffectParameter("master", "Master", 0.80f, 0.0f, 1.0f)
                        )
                    ),
                    EffectUnit(
                        id = "reverb_4",
                        type = EffectType.REVERB,
                        name = "Spring Reverb",
                        enabled = true,
                        parameters = listOf(
                            EffectParameter("room_size", "Room", 0.45f, 0.1f, 1.0f),
                            EffectParameter("mix", "Mix", 0.30f, 0.0f, 1.0f)
                        )
                    )
                )
            )
        )
    }
}
