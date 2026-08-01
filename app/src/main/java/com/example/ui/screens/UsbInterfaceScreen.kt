package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun UsbInterfaceScreen(viewModel: MainViewModel) {
    val config by viewModel.configState.collectAsState()

    val sampleRates = listOf(44100, 48000, 96000)
    val bufferSizes = listOf(64, 128, 256, 512)

    val estimatedLatencyMs = (config.bufferSize.toFloat() / config.sampleRate.toFloat()) * 1000.0f * 2.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // USB Interface Hardware Badge Header
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (config.usbConnected) NeonCyan.copy(alpha = 0.2f) else StudioCardBg)
                            .border(1.dp, if (config.usbConnected) NeonCyan else StudioBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Usb,
                            contentDescription = "USB Audio",
                            tint = if (config.usbConnected) NeonCyan else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = config.usbDeviceName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (config.usbConnected) MeterGreen else AmberGlow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.usbConnected) "USB Interface Active (Low Latency)" else "Internal Audio / USB Ready",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshUsbInterfaces() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh USB",
                        tint = AmberGlow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Latency Benchmark Gauge Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Latency",
                            tint = AmberGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESTIMATED ROUNDTRIP LATENCY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = String.format("%.2f ms", estimatedLatencyMs),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (estimatedLatencyMs <= 5.0f) MeterGreen else MeterYellow
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (estimatedLatencyMs / 20.0f).coerceIn(0f, 1f) },
                    color = if (estimatedLatencyMs <= 5.0f) MeterGreen else MeterYellow,
                    trackColor = StudioBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Engine Parameters (Sample Rate & Buffer Size)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sample Rate Picker
                Text(
                    text = "SAMPLE RATE (Hz)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleRates.forEach { sr ->
                        FilterChip(
                            selected = config.sampleRate == sr,
                            onClick = { viewModel.updateAudioConfig(sampleRate = sr) },
                            label = { Text("${sr / 1000} kHz", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberGlow,
                                selectedLabelColor = StudioBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Buffer Size Picker
                Text(
                    text = "DSP BUFFER SIZE (Samples)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bufferSizes.forEach { buf ->
                        FilterChip(
                            selected = config.bufferSize == buf,
                            onClick = { viewModel.updateAudioConfig(bufferSize = buf) },
                            label = { Text("$buf smp", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = StudioBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Gain & Master Volume Sliders
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Input Gain Slider
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "USB INPUT GAIN BOOST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1fx", config.inputGain),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlow
                    )
                }
                Slider(
                    value = config.inputGain,
                    onValueChange = { viewModel.updateAudioConfig(inputGain = it) },
                    valueRange = 0.0f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGlow,
                        activeTrackColor = AmberGlow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Master Volume Slider
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MASTER OUTPUT VOLUME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1fx", config.outputVolume),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
                Slider(
                    value = config.outputVolume,
                    onValueChange = { viewModel.updateAudioConfig(outputVolume = it) },
                    valueRange = 0.0f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    )
                )
            }
        }
    }
}
