package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetRepository
import com.example.dsp.engine.UsbAudioEngine
import com.example.dsp.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val audioEngine = UsbAudioEngine(application)
    private val repository: PresetRepository

    private val _effectsChain = MutableStateFlow<List<EffectUnit>>(FactoryPresets.getDefaultChain())
    val effectsChain: StateFlow<List<EffectUnit>> = _effectsChain.asStateFlow()

    private val _selectedEffectId = MutableStateFlow<String?>(_effectsChain.value.firstOrNull()?.id)
    val selectedEffectId: StateFlow<String?> = _selectedEffectId.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val configState: StateFlow<AudioConfig> = audioEngine.configState
    val inputVu: StateFlow<Float> = audioEngine.inputVuLevel
    val outputVu: StateFlow<Float> = audioEngine.outputVuLevel
    val isClipping: StateFlow<Boolean> = audioEngine.isClipping
    val waveform: StateFlow<FloatArray> = audioEngine.waveformState
    val fftSpectrum: StateFlow<FloatArray> = audioEngine.fftSpectrumState
    val tunerState: StateFlow<TunerNoteState> = audioEngine.tunerState

    val userPresetsFlow: Flow<List<PresetData>>

    init {
        val dao = AppDatabase.getDatabase(application).presetDao()
        repository = PresetRepository(dao)

        userPresetsFlow = repository.allPresets.map { entities ->
            entities.map { entity ->
                PresetData(
                    id = entity.id,
                    title = entity.title,
                    category = entity.category,
                    description = entity.description,
                    effects = com.example.data.Converters.jsonToEffects(entity.effectsJson)
                )
            }
        }

        // Sync active effects to engine
        audioEngine.updateActiveEffects(_effectsChain.value)
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectEffect(id: String) {
        _selectedEffectId.value = id
    }

    fun toggleEffect(id: String) {
        val updated = _effectsChain.value.map { unit ->
            if (unit.id == id) unit.copy(enabled = !unit.enabled) else unit
        }
        _effectsChain.value = updated
        audioEngine.updateActiveEffects(updated)
    }

    fun updateParameter(effectId: String, paramKey: String, newValue: Float) {
        val updated = _effectsChain.value.map { unit ->
            if (unit.id == effectId) {
                unit.withParam(paramKey, newValue)
            } else unit
        }
        _effectsChain.value = updated
        audioEngine.updateActiveEffects(updated)
    }

    fun moveEffect(fromIndex: Int, toIndex: Int) {
        val current = _effectsChain.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _effectsChain.value = current
            audioEngine.updateActiveEffects(current)
        }
    }

    fun loadPreset(preset: PresetData) {
        _effectsChain.value = preset.effects
        _selectedEffectId.value = preset.effects.firstOrNull()?.id
        audioEngine.updateActiveEffects(preset.effects)
    }

    fun saveUserPreset(title: String, category: String, description: String) {
        viewModelScope.launch {
            val preset = PresetData(
                title = title,
                category = category,
                description = description,
                effects = _effectsChain.value
            )
            repository.savePreset(preset)
        }
    }

    fun deleteUserPreset(presetId: Long) {
        viewModelScope.launch {
            repository.deletePreset(presetId)
        }
    }

    fun updateAudioConfig(
        sampleRate: Int = configState.value.sampleRate,
        bufferSize: Int = configState.value.bufferSize,
        inputGain: Float = configState.value.inputGain,
        outputVolume: Float = configState.value.outputVolume
    ) {
        val newConfig = configState.value.copy(
            sampleRate = sampleRate,
            bufferSize = bufferSize,
            inputGain = inputGain,
            outputVolume = outputVolume
        )
        audioEngine.updateConfig(newConfig)
    }

    fun refreshUsbInterfaces() {
        audioEngine.detectUsbInterfaces()
    }

    fun toggleAudioEngine(enable: Boolean) {
        if (enable) {
            audioEngine.startAudioProcessing()
        } else {
            audioEngine.stopAudioProcessing()
        }
        audioEngine.updateConfig(configState.value.copy(isEngineRunning = enable))
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopAudioProcessing()
    }
}
