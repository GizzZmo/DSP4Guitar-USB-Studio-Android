package com.dsp4guitar.studio

import org.junit.Test
import org.junit.Assert.*

class DspChainParameterTest {

    @Test
    fun effectStageOrdinals_areConsecutiveFromZero() {
        val stages = com.dsp4guitar.studio.audio.EffectStage.entries
        stages.forEachIndexed { idx, stage ->
            assertEquals("Stage $stage should have index $idx", idx, stage.index)
        }
    }

    @Test
    fun effectStageCount_isTen() {
        assertEquals(10, com.dsp4guitar.studio.audio.EffectStage.entries.size)
    }
}
