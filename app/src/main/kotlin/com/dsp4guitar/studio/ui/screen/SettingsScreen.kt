package com.dsp4guitar.studio.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsp4guitar.studio.audio.AudioEngineJni
import com.dsp4guitar.studio.ui.theme.MatrixGreen

/**
 * Settings screen — USB device selection, sample rate, buffer size, etc.
 * More settings will be added in future iterations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    audioEngine: AudioEngineJni,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MatrixGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = "USB AUDIO DEVICE",
                style = MaterialTheme.typography.titleLarge,
                color = MatrixGreen
            )
            Text(
                text  = "Connect a USB Class-Compliant (UAC 2.0) audio interface\n" +
                        "via USB-C OTG and it will appear here automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Text(
                text  = "AUDIO ENGINE",
                style = MaterialTheme.typography.titleLarge,
                color = MatrixGreen
            )
            Text(
                text  = "Engine: Google Oboe (AAudio / OpenSL ES fallback)\n" +
                        "Performance mode: LOW_LATENCY\n" +
                        "Sharing mode: EXCLUSIVE",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
