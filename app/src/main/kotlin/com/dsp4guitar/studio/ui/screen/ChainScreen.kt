package com.dsp4guitar.studio.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsp4guitar.studio.audio.AudioEngineJni
import com.dsp4guitar.studio.ui.components.EffectTile
import com.dsp4guitar.studio.ui.theme.MatrixGreen
import com.dsp4guitar.studio.viewmodel.MainViewModel

/**
 * Main signal-chain screen — shows all 10 effect stages as tiles in a
 * scrollable row, with a top-bar showing latency and a settings button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainScreen(
    audioEngine: AudioEngineJni,
    onOpenSettings: () -> Unit
) {
    val viewModel: MainViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(audioEngine) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    // Refresh latency every recompose (lightweight — reads a single atomic)
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshLatency()
            kotlinx.coroutines.delay(1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "DSP4GUITAR STUDIO",
                            style = MaterialTheme.typography.titleLarge,
                            color = MatrixGreen
                        )
                        Text(
                            "LATENCY: ${"%.1f".format(uiState.latencyMs)} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MatrixGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "SIGNAL CHAIN",
                style = MaterialTheme.typography.titleLarge,
                color = MatrixGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(uiState.effects) { effectState ->
                    EffectTile(
                        name          = effectState.stage.displayName,
                        isBypassed    = effectState.isBypassed,
                        onToggleBypass = { viewModel.toggleEffectBypass(effectState.stage) },
                        modifier      = Modifier.width(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input / Output gain sliders
            GainSection(
                label     = "INPUT GAIN",
                valueDb   = uiState.inputGainDb,
                onChanged = viewModel::setInputGain
            )
            Spacer(modifier = Modifier.height(16.dp))
            GainSection(
                label     = "OUTPUT GAIN",
                valueDb   = uiState.outputGainDb,
                onChanged = viewModel::setOutputGain
            )
        }
    }
}

@Composable
private fun GainSection(
    label: String,
    valueDb: Float,
    onChanged: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "${"%.1f".format(valueDb)} dB",
                style = MaterialTheme.typography.labelSmall,
                color = MatrixGreen
            )
        }
        Slider(
            value        = valueDb,
            onValueChange = onChanged,
            valueRange   = -24f..24f,
            colors       = SliderDefaults.colors(
                thumbColor       = MatrixGreen,
                activeTrackColor = MatrixGreen
            )
        )
    }
}
