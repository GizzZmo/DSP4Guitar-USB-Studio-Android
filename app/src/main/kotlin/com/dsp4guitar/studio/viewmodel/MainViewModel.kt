package com.dsp4guitar.studio.viewmodel

import androidx.lifecycle.ViewModel
import com.dsp4guitar.studio.audio.AudioEngineJni
import com.dsp4guitar.studio.audio.EffectStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EffectState(
    val stage: EffectStage,
    val isBypassed: Boolean = true,
    val parameters: Map<Int, Float> = emptyMap()
)

data class EngineUiState(
    val isRunning: Boolean = false,
    val inputGainDb: Float = 0f,
    val outputGainDb: Float = 0f,
    val latencyMs: Double = 0.0,
    val effects: List<EffectState> = EffectStage.entries.map { EffectState(it) }
)

class MainViewModel(
    private val engine: AudioEngineJni
) : ViewModel() {

    private val _uiState = MutableStateFlow(EngineUiState())
    val uiState: StateFlow<EngineUiState> = _uiState.asStateFlow()

    fun toggleEffectBypass(stage: EffectStage) {
        val newBypassed = !engine.isEffectBypassed(stage.index)
        engine.setEffectBypass(stage.index, newBypassed)
        _uiState.update { state ->
            state.copy(
                effects = state.effects.map {
                    if (it.stage == stage) it.copy(isBypassed = newBypassed) else it
                }
            )
        }
    }

    fun setEffectParameter(stage: EffectStage, paramId: Int, value: Float) {
        engine.setEffectParameter(stage.index, paramId, value)
        _uiState.update { state ->
            state.copy(
                effects = state.effects.map {
                    if (it.stage == stage) {
                        it.copy(parameters = it.parameters + (paramId to value))
                    } else it
                }
            )
        }
    }

    fun setInputGain(db: Float) {
        engine.setInputGainDb(db)
        _uiState.update { it.copy(inputGainDb = db) }
    }

    fun setOutputGain(db: Float) {
        engine.setOutputGainDb(db)
        _uiState.update { it.copy(outputGainDb = db) }
    }

    fun refreshLatency() {
        _uiState.update { it.copy(latencyMs = engine.getEstimatedLatencyMs()) }
    }
}
