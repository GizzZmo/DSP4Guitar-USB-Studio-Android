package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OscilloscopeCanvas
import com.example.ui.components.SpectrumAnalyzerCanvas
import com.example.ui.components.VuMeter
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StudioRecorderScreen(viewModel: MainViewModel) {
    val waveform by viewModel.waveform.collectAsState()
    val spectrum by viewModel.fftSpectrum.collectAsState()
    val inputVu by viewModel.inputVu.collectAsState()
    val outputVu by viewModel.outputVu.collectAsState()
    val isClipping by viewModel.isClipping.collectAsState()

    var isRecording by remember { mutableStateOf(false) }
    var isPlayingBackingTrack by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(12.dp)
    ) {
        // Oscilloscope Waveform Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "REAL-TIME OSCILLOSCOPE WAVEFORM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                OscilloscopeCanvas(
                    waveform = waveform,
                    lineColor = NeonCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spectrum Analyzer Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkMetal),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "32-BAND FFT FREQUENCY SPECTRUM ANALYZER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGlow
                )

                Spacer(modifier = Modifier.height(8.dp))

                SpectrumAnalyzerCanvas(
                    spectrumBands = spectrum,
                    barColor = AmberGlow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Jam Recorder Controls Bar
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
                    VuMeter(level = inputVu, label = "IN", isClipping = false)
                    Spacer(modifier = Modifier.width(16.dp))
                    VuMeter(level = outputVu, label = "OUT", isClipping = isClipping)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Record Guitar Button
                    Button(
                        onClick = { isRecording = !isRecording },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MeterRed else StudioCardBg,
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = "Record",
                            tint = if (isRecording) Color.White else MeterRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play Backing Track Button
                    Button(
                        onClick = { isPlayingBackingTrack = !isPlayingBackingTrack },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingBackingTrack) NeonCyan else StudioCardBg,
                            contentColor = if (isPlayingBackingTrack) StudioBlack else Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingBackingTrack) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Backing Track",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
